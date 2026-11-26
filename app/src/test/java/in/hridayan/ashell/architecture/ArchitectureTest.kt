package `in`.hridayan.ashell.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureTest {

    @Test
    fun `feature modules do not depend on each other`() {
        FEATURE_MODULES.forEach { feature ->
            val otherFeatures = FEATURE_MODULES.filter { it != feature }

            Konsist.scopeFromProject()
                .files
                .withPackage("in.hridayan.ashell.$feature..")
                .assertTrue { file ->
                    val imports = file.imports.map { it.name }
                    otherFeatures.none { other ->
                        imports.any { it.startsWith("in.hridayan.ashell.$other") }
                    }
                }
        }
    }

    @Test
    fun `core modules do not depend on feature modules`() {
        Konsist.scopeFromProject()
            .files
            .withPackage("in.hridayan.ashell.core..")
            .filterNot { isAppModuleFile(it.path) }
            .assertTrue { file ->
                val imports = file.imports.map { it.name }
                FEATURE_MODULES.none { feature ->
                    imports.any { it.startsWith("in.hridayan.ashell.$feature") }
                }
            }
    }

    @Test
    fun `domain layer does not depend on compose ui`() {
        mainDomainFiles()
            .filterNot { file -> endsWithAny(file.path, DOMAIN_COMPOSE_UI_ALLOWLIST) }
            .assertTrue { file ->
                file.imports.none { importDecl ->
                    DOMAIN_FORBIDDEN_UI_PREFIXES.any { prefix ->
                        importDecl.name.startsWith(prefix)
                    }
                }
            }
    }

    @Test
    fun `production code does not use GlobalScope`() {
        mainSourceFiles().assertTrue { file ->
            file.imports.none { it.name == GLOBAL_SCOPE_IMPORT } &&
                !GLOBAL_SCOPE_USAGE_REGEX.containsMatchIn(file.text)
        }
    }

    @Test
    fun `production code does not introduce new runBlocking calls`() {
        mainSourceFiles()
            .filterNot { file -> endsWithAny(file.path, RUN_BLOCKING_ALLOWLIST) }
            .assertTrue { file ->
                file.imports.none { it.name == RUN_BLOCKING_IMPORT } &&
                    !RUN_BLOCKING_USAGE_REGEX.containsMatchIn(file.text)
            }
    }

    @Test
    fun `domain layer does not introduce android log or println calls`() {
        mainDomainFiles()
            .filterNot { file -> endsWithAny(file.path, DOMAIN_LOGGING_ALLOWLIST) }
            .assertTrue { file ->
                file.imports.none { it.name == ANDROID_LOG_IMPORT } &&
                    !DOMAIN_UNSAFE_LOGGING_REGEX.containsMatchIn(file.text)
            }
    }

    private fun mainSourceFiles() = Konsist.scopeFromProject()
        .files
        .filter { file ->
            normalizePath(file.path).contains(MAIN_SOURCE_MARKER)
        }

    private fun mainDomainFiles() = mainSourceFiles()
        .filter { file ->
            normalizePath(file.path).contains(DOMAIN_PATH_MARKER)
        }

    private fun isAppModuleFile(path: String): Boolean =
        normalizePath(path).contains(APP_MODULE_MARKER)

    private fun endsWithAny(path: String, suffixes: Set<String>): Boolean {
        val normalized = normalizePath(path)
        return suffixes.any { normalized.endsWith(it) }
    }

    private fun normalizePath(path: String): String = path.replace('\\', '/')

    private companion object {
        const val MAIN_SOURCE_MARKER = "/src/main/"
        const val DOMAIN_PATH_MARKER = "/domain/"
        const val APP_MODULE_MARKER = "/app/"
        const val GLOBAL_SCOPE_IMPORT = "kotlinx.coroutines.GlobalScope"
        const val RUN_BLOCKING_IMPORT = "kotlinx.coroutines.runBlocking"
        const val ANDROID_LOG_IMPORT = "android.util.Log"

        val GLOBAL_SCOPE_USAGE_REGEX = Regex("""\bGlobalScope\b""")
        val RUN_BLOCKING_USAGE_REGEX = Regex("""\brunBlocking\s*[{(<]""")
        val DOMAIN_UNSAFE_LOGGING_REGEX = Regex("""\b(Log\.[devwi]|println|printStackTrace)\s*\(""")

        val FEATURE_MODULES = listOf(
            "adbsideload",
            "ai",
            "commandexamples",
            "crashreporter",
            "guides",
            "home",
            "logcat",
            "mirror",
            "onboarding",
            "portforward",
            "qstiles",
            "settings",
            "shell",
        )

        val DOMAIN_FORBIDDEN_UI_PREFIXES = listOf(
            "androidx.compose.ui.",
            "androidx.compose.material",
        )

        val DOMAIN_COMPOSE_UI_ALLOWLIST = setOf(
            "core/common/src/main/java/in/hridayan/ashell/core/common/domain/model/AppFont.kt",
            "feature/qstiles/src/main/java/in/hridayan/ashell/qstiles/domain/model/TileActiveState.kt",
        )

        val RUN_BLOCKING_ALLOWLIST = setOf(
            "feature/shell/src/main/java/in/hridayan/ashell/shell/wifi_adb_shell/domain/repository/AiConnectionStateProviderImpl.kt",
        )

        val DOMAIN_LOGGING_ALLOWLIST = setOf(
            "feature/ai/src/main/java/in/hridayan/ashell/ai/domain/usecase/GenerateAiChatResponseUseCase.kt",
            "feature/settings/src/main/java/in/hridayan/ashell/settings/domain/usecase/GenerateCustomThemeUseCase.kt",
            "feature/shell/src/main/java/in/hridayan/ashell/shell/domain/utils/ShellFileUtils.kt",
        )
    }
}
