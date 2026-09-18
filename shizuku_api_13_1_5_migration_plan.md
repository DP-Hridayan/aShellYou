# Migration plan: Shizuku API 13.1.0 → 13.1.5 (UserService replaces `newProcess`)

Date: 2026-09-08. Companion to `implementation_plan.md` (Shizuku+ findings). This plan supersedes
its Phase 3 and makes the migration mandatory: `dev.rikka.shizuku:api:13.1.5` makes
`Shizuku.newProcess` `private`, so the current code stops compiling the moment the version is bumped.

## 1. What changes between 13.1.0 and 13.1.5

Verified by diffing the Maven Central sources jars (`api`, `provider`, `aidl`, `shared`).

| Change | Version | Effect on aShellYou |
| --- | --- | --- |
| `Shizuku.newProcess(...)` becomes `private` | 13.1.1 | **Compile break** in 3 files (see §2) |
| Listener lists switched from `CopyOnWriteArrayList` to synchronized `ArrayList` | 13.1.1/13.1.2 | none, API unchanged |
| `unbindUserService(remove=false)` now really removes the callback; server asked to drop the `ShizukuServiceConnection` when server ≥ 13.4 | 13.1.3/13.1.4 | relevant to the new UserService code, handled below |
| `ShizukuProvider.requestBinderForNonProviderProcess` registers receiver with `RECEIVER_NOT_EXPORTED` on Android 13+ | 13.1.5 | none (app is single-process, `multiprocess=false`) |
| AIDL transaction codes / descriptors | unchanged | none; Shizuku+ compatibility analysis in `implementation_plan.md` still holds |
| Desugaring requirement (minSdk 23) | 13.1.0 | none, `minSdk = 28` |

Upstream rationale (README changelog 13.1.1): UserService "can replace `newProcess` in all cases",
`newProcess` is text-based, has no tty, and is scheduled for removal in API 14.

## 2. Code that breaks

| File | Call | Behaviour today |
| --- | --- | --- |
| [ShellCommandExecutor.kt:151](feature/shell/src/main/java/in/hridayan/ashell/shell/local_adb_shell/data/shell/ShellCommandExecutor.kt#L151) | `Shizuku.newProcess(arrayOf("sh","-c",cmd), env, dir)` | Local ADB Shizuku mode |
| [ShizukuLogcatEmitter.kt:27](feature/logcat/src/main/java/in/hridayan/ashell/logcat/data/emitter/ShizukuLogcatEmitter.kt#L27) | `Shizuku.newProcess(arrayOf("logcat","-v","threadtime"), null, null)` | Logcat screen, Shizuku mode |
| [ShizukuExecutor.kt:43](feature/qstiles/src/main/java/in/hridayan/ashell/qstiles/data/executor/ShizukuExecutor.kt#L43) | `Shizuku.newProcess(arrayOf("sh","-c",cmd), null, null)` | Quick Settings tiles, Shizuku mode |

Everything else the app uses (`pingBinder`, `checkSelfPermission`, `requestPermission`,
`add/removeRequestPermissionResultListener`, `ShizukuProvider` in the manifest) is unchanged.

Modules that declare the dependency: `core/common`, `feature/shell`, `feature/logcat`,
`feature/qstiles` (all via `libs.shizuku.api` + `libs.shizuku.provider`, version key `shizuku` in
`gradle/libs.versions.toml`).

## 3. Target design

One privileged helper process, started by Shizuku as a UserService, owned by a new core module and
shared by the three features.

### 3.1 New module `core/shizuku`

Android library, `buildFeatures { aidl = true }`, depends on `libs.shizuku.api`,
`libs.shizuku.provider`, Hilt, coroutines. No Compose, no feature dependency (architecture rule:
core never depends on features). Package `in.hridayan.ashell.core.shizuku`.

```
core/shizuku/
  src/main/aidl/in/hridayan/ashell/core/shizuku/IShellUserService.aidl
  src/main/aidl/in/hridayan/ashell/core/shizuku/IShellProcess.aidl
  src/main/java/in/hridayan/ashell/core/shizuku/
    domain/
      ShizukuCommandRunner.kt        interface, returns Result<Process>
      ShizukuServiceState.kt         sealed: Idle, Binding, Ready(uid), Unavailable(reason)
      ShizukuServiceError.kt         sealed: BinderMissing, PermissionDenied, BindTimeout, BinderDied, ProcessStartFailed
    service/
      ShellUserService.kt            IShellUserService.Stub, runs in the privileged process
      ShellProcessBinder.kt          IShellProcess.Stub wrapping java.lang.Process
      PipePump.kt                    copies a stream into a ParcelFileDescriptor pipe
    data/
      ShizukuUserServiceConnector.kt @Singleton bind / await / rebind
      ShizukuCommandRunnerImpl.kt    runner on top of the connector
      ShizukuRemoteShellProcess.kt   java.lang.Process adapter over IShellProcess
    di/ShizukuModule.kt
  consumer-rules.pro
```

### 3.2 AIDL

```aidl
// IShellProcess.aidl
package in.hridayan.ashell.core.shizuku;

interface IShellProcess {
    ParcelFileDescriptor getInputStream() = 1;
    ParcelFileDescriptor getErrorStream() = 2;
    ParcelFileDescriptor getOutputStream() = 3;
    int waitFor() = 4;
    int exitValue() = 5;
    boolean alive() = 6;
    void destroy() = 7;
}
```

```aidl
// IShellUserService.aidl
package in.hridayan.ashell.core.shizuku;

import in.hridayan.ashell.core.shizuku.IShellProcess;

interface IShellUserService {
    void destroy() = 16777114;
    IShellProcess newProcess(in String[] cmd, in String[] env, in String dir) = 1;
    int getUid() = 2;
}
```

`destroy() = 16777114` is the reserved id Shizuku calls when it stops the service; the
implementation kills every child process and calls `System.exit(0)`. The shape intentionally
mirrors `moe.shizuku.server.IRemoteProcess` so `ShizukuRemoteShellProcess` is a straight port of
`ShizukuRemoteProcess` (which the app can no longer construct).

### 3.3 Privileged side (`ShellUserService`)

- Two constructors: no-arg and `@Keep constructor(context: Context)` (Shizuku v13 prefers the
  latter). No Hilt, no Android framework services; this process is not an app process.
- `newProcess` → `ProcessBuilder(cmd)` with `environment()` reset to `env` when non-null and
  `directory(dir)` when non-null. Returns `ShellProcessBinder`.
- `ShellProcessBinder` lazily creates pipes with `ParcelFileDescriptor.createPipe()` and starts a
  `PipePump` thread per stream (same technique as Shizuku's server `RemoteProcessHolder`).
  `destroy()` kills the process and closes both pipe ends.
- Track live processes in a `ConcurrentHashMap` so `destroy()` of the service can reap them.
- `getUid()` returns `Os.getuid()` so the UI can show shell (2000) vs root (0).

### 3.4 App side

`ShizukuUserServiceConnector` (`@Singleton`):
- `UserServiceArgs(ComponentName(packageName, ShellUserService::class.java.name))`
  `.daemon(false)` (dies with the app, same lifetime semantics as `newProcess`)
  `.processNameSuffix(PROCESS_NAME_SUFFIX)`
  `.tag(SERVICE_TAG)` (class names are unstable under R8; tag is required)
  `.version(versionCode)` (from `PackageManager.getPackageInfo`, so the service restarts after app updates)
  `.debuggable(isDebuggable)` (from `ApplicationInfo.FLAG_DEBUGGABLE`, no BuildConfig in core).
- `state: StateFlow<ShizukuServiceState>`.
- `suspend fun service(): Result<IShellUserService>`: returns cached proxy if `pingBinder()` is
  true, otherwise binds and suspends on a `CompletableDeferred` with `BIND_TIMEOUT_MS` (constant).
  Pre-checks `Shizuku.pingBinder()` and `checkSelfPermission()` and maps failures to
  `ShizukuServiceError`.
- `Shizuku.addBinderDeadListener` resets state to `Idle`; `onServiceDisconnected` does the same.
- `unbindUserService(args, conn, remove = false)` only on explicit shutdown; 13.1.5 fixed the
  duplicate-callback bug that made this unsafe before.
- Optional warm-up: `warmUp()` called when the user picks Shizuku mode or when permission is
  granted, so the first command does not pay the `app_process` start cost.

`ShizukuCommandRunnerImpl`:
- `suspend fun start(cmd: Array<String>, env: Array<String>?, dir: String?): Result<Process>`
  → `service().mapCatching { ShizukuRemoteShellProcess(it.newProcess(cmd, env, dir)) }`.
- Never throws; `RemoteException`, `NullPointerException`, `IllegalStateException` all become
  `ShizukuServiceError.ProcessStartFailed(cause)`.

`ShizukuRemoteShellProcess : Process`: `AutoCloseInputStream` / `AutoCloseOutputStream` over the
returned descriptors, `waitFor`, `exitValue`, `destroy`, `isAlive`, plus `linkToDeath` to mark the
process dead if the helper dies.

### 3.5 Feature wiring

- `feature/shell` `ShellCommandExecutor`: `runShizuku` becomes
  `runner.start(arrayOf("sh","-c",cmd), envArray, safeDir)` → on success `emitAll(exec(process))`
  (the existing generic reader), on failure emit one error `OutputLine`. Delete
  `execShizukuProcess` and the `shizukuProcess` field; `stop()` only tracks `currentProcess`.
  `ShellRepositoryImpl.executeShizukuCommand` adds `.catch { }` and drops the `?:` null branch.
  `ShellViewModel.runCommand` wraps `collect` in `try/catch` (rethrow `CancellationException`) so
  every mode resets `ShellState.Free` on failure.
- `feature/logcat` `ShizukuLogcatEmitter`: `runner.start(arrayOf("logcat","-v","threadtime"), null, null)`;
  `isAvailable()` = `Shizuku.pingBinder()`; destroy the process in `finally` so cancelling the flow
  stops logcat in the helper.
- `feature/qstiles` `ShizukuExecutor`: same call; keep its existing `CommandResult` mapping. Tiles
  may be the first Shizuku user after a cold start, so surface `ShizukuServiceError.BindTimeout`
  as `TileErrorType.EXECUTION_FAILED` with a localized message.
- Hilt: `ShizukuModule` binds `ShizukuCommandRunner` → `ShizukuCommandRunnerImpl` in
  `SingletonComponent`; `ShellUseCaseModule.provideShellCommandExecutor` gains the runner
  parameter. `ShizukuPermissionHandler` stays in `feature/shell` (out of scope).

### 3.6 Build and packaging

- `gradle/libs.versions.toml`: `shizuku = "13.1.5"`.
- `core/shizuku/build.gradle.kts`: `buildFeatures { aidl = true }`, `minSdk = 28`, Java 17.
- `settings.gradle.kts`: include `:core:shizuku`; add `implementation(project(":core:shizuku"))`
  to `feature/shell`, `feature/logcat`, `feature/qstiles`.
- `core/shizuku/consumer-rules.pro`:
  ```
  -keep class in.hridayan.ashell.core.shizuku.service.ShellUserService { public <init>(...); }
  -keep class in.hridayan.ashell.core.shizuku.IShellUserService* { *; }
  -keep class in.hridayan.ashell.core.shizuku.IShellProcess* { *; }
  ```
  (the app's existing `@androidx.annotation.Keep` rules also cover the `Context` constructor).
- Strings in `core/resources/strings.xml` with `comment` attributes:
  `shizuku_service_starting`, `shizuku_service_start_timeout`, `shizuku_service_died`,
  `shizuku_process_start_failed`.
- Developer note for `README`/`CONTRIBUTING`: Android Studio → Run/Debug configuration →
  "Always install with package manager" must be on, otherwise the UserService loads stale code.

## 4. Phases (per `.agents/rules/agent-workflow.md`)

Order chosen so the app compiles at every step and the final version bump is the proof that no
`newProcess` call is left.

### Phase 1 — Domain and data (core/shizuku), still on 13.1.0

1. Create module, AIDL files, `ShellUserService`, `ShellProcessBinder`, `PipePump`.
2. `ShizukuServiceState`, `ShizukuServiceError`, `ShizukuCommandRunner`.
3. `ShizukuUserServiceConnector`, `ShizukuCommandRunnerImpl`, `ShizukuRemoteShellProcess`, Hilt module.
4. TDD on JVM-testable parts: `PipePump` (copies bytes, closes on EOF), `ShizukuCommandRunnerImpl`
   error mapping with a fake `IShellUserService` implementing the AIDL interface (not the `Stub`,
   so no `Binder` in unit tests), connector state machine with a fake bind function.
5. `./gradlew :core:shizuku:assembleDebug`.

### Phase 2 — Wire `feature/shell`

1. `ShellCommandExecutor.runShizuku` → runner; delete `execShizukuProcess`.
2. `ShellRepositoryImpl.executeShizukuCommand` `.catch`; `ShellViewModel.runCommand` try/catch.
3. Tests: executor emits error line when runner fails; ViewModel ends in `ShellState.Free`.
4. `./gradlew assembleDebug`; manual run on stock Shizuku (ADB) for `id`, `cd /data && ls`,
   `dumpsys deviceidle whitelist`, cancel mid-run.

### Phase 3 — Wire `feature/logcat` and `feature/qstiles`

1. `ShizukuLogcatEmitter` and `ShizukuExecutor` → runner.
2. Verify logcat streaming stops on screen exit (helper process has no leftover `logcat`).
3. Verify tile execution from cold start (bind latency, timeout message).

### Phase 4 — Version bump and cleanup

1. `shizuku = "13.1.5"`; remove every `@Suppress("DEPRECATION")` that guarded `newProcess`;
   compile must pass with zero references to `Shizuku.newProcess` or `ShizukuRemoteProcess`.
2. Add `af.shizuku.plus.api` to `<queries>` (optional, from the previous plan).
3. `walkthrough.md`.

## 5. Behavioural differences to be aware of

| Topic | `newProcess` | UserService |
| --- | --- | --- |
| First command latency | ~50 ms | `app_process` start + dex load, typically 0.5–2 s once per app lifetime; mitigated by warm-up and a "starting" line |
| Process lifetime | killed when caller dies | `daemon(false)`: helper dies with the app; children reaped in `destroy()` |
| Identity | shell/root | same (helper runs as the Shizuku server uid) |
| Shizuku+ SU-bridge rewriting of `id`, `whoami`, `getenforce`, `service call`, `pm`, `settings` | applied | bypassed (commands run inside our helper) |
| Shizuku+ transaction-code bug (≤ r2436) | broken | unaffected: `addUserService` (12) and `attachUserService` (102) go through the generated stub |
| App update while service running | n/a | `version` mismatch → Shizuku restarts the helper; Shizuku+ also watches APK changes (`ShizukuUserServiceManager`) |
| `Context` inside helper | n/a | limited; do not touch `ContentResolver`/receivers there |

## 6. Risks and mitigations

- Helper start cost on low-end devices: warm-up on mode selection; timeout constant; clear
  message rather than a hang.
- R8 renaming the service class: `tag` set explicitly, keep rules in consumer proguard.
- Android Studio deploy optimisation shipping stale dex to the helper: documented switch.
- Shizuku+ Magisk-mocking env injection (`MAGISK_VER*`) reaching the helper: harmless; we reset the
  environment per command when `env` is given.
- Zombie children if the helper is SIGKILLed: acceptable, same as today; `destroy()` covers the
  normal path.

## 7. Verification matrix

- `./gradlew assembleDebug` after each phase; unit tests in `core/shizuku` and `feature/shell`.
- Stock Shizuku 13.x via ADB (uid 2000) and via root (uid 0).
- Shizuku+ Plus flavour + Compat Hub, Shizuku+ drop-in, both on the latest pre-release.
- Scenarios: Local ADB Shizuku mode commands, long-running `logcat`, cancel, Logcat screen,
  QS tile from cold start, app update with the helper alive, Shizuku service restart while the
  app is open (binder-dead → rebind on next command), permission revoked mid-session.

## 8. Sources

- Maven Central `dev.rikka.shizuku:{api,provider,aidl,shared}:13.1.0` vs `13.1.5` sources jars
- RikkaApps/Shizuku-API README (UserService section, changelog 13.1.1–13.1.5), demo `UserService.java`,
  `IUserService.aidl`, `proguard-rules.pro`
- thejaustin/ShizukuPlus `server/.../ShizukuUserServiceManager.java` (UserService support present)
- `implementation_plan.md` §2–3 for the transaction-code analysis
