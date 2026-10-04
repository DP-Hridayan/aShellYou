package `in`.hridayan.ashell.shell.file_browser.domain.usecase

import `in`.hridayan.ashell.shell.file_browser.domain.model.ConflictDecision
import `in`.hridayan.ashell.shell.file_browser.domain.model.ConflictResolution
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileConflict
import `in`.hridayan.ashell.shell.file_browser.domain.model.OperationType
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteFailure
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteFailureReason
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteProgress
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteRequest
import `in`.hridayan.ashell.shell.file_browser.domain.model.PasteSummary
import `in`.hridayan.ashell.shell.file_browser.domain.model.PathInfo
import `in`.hridayan.ashell.shell.file_browser.domain.repository.FileBrowserRepository
import `in`.hridayan.ashell.shell.file_browser.domain.util.KeepBothNames
import `in`.hridayan.ashell.shell.file_browser.domain.util.RemotePaths
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

private const val STAGING_PREFIX = ".ashell-staging-"
private const val BACKUP_PREFIX = ".ashell-backup-"
private const val MAX_KEEP_BOTH_CANDIDATES = 500
private const val KEEP_BOTH_BATCH_SIZE = 25

/**
 * Copies or moves items into a folder on the device, asking the user about every name conflict.
 *
 * Every check asks the device at the moment it matters instead of trusting a cached listing, so the
 * outcome does not depend on which folder the UI shows, on letter case rules of the storage, or on
 * items created earlier in the same paste. Paths are compared after resolving symlinked folders, so
 * pasting into the source's own folder through an alias such as `/sdcard` is still recognised.
 *
 * Commands that change the device run to completion even when the caller is cancelled. Stopping
 * between the steps of a replace would leave the existing item under a temporary name.
 */
class PasteFilesUseCase @Inject constructor(
    private val repository: FileBrowserRepository
) {

    /**
     * @param resolveConflict asked for each conflict no remembered choice covers; returning null
     * stops the paste after the items already handled.
     * @param isCancelled polled between items, so the item in progress always finishes.
     * @param onProgress reports each item of [PasteRequest.sourcePaths] as it starts.
     */
    suspend operator fun invoke(
        request: PasteRequest,
        resolveConflict: suspend (FileConflict) -> ConflictDecision?,
        isCancelled: () -> Boolean = { false },
        onProgress: (PasteProgress) -> Unit = {}
    ): PasteSummary {
        val session = PasteSession(request.operationType, resolveConflict, isCancelled)
        val destination = resolveDestination(request.destinationDir)

        pasteLevel(request.sourcePaths, destination, session) { index, name ->
            onProgress(PasteProgress(index + 1, request.sourcePaths.size, name))
        }

        return session.summary()
    }

    private suspend fun resolveDestination(directory: String): Destination {
        val realDirectory = repository.resolveDirectory(directory).getOrDefault(directory)
        return Destination(directory, realDirectory)
    }

    private suspend fun pasteLevel(
        sourcePaths: List<String>,
        destination: Destination,
        session: PasteSession,
        onItemStarted: (Int, String) -> Unit = { _, _ -> }
    ) {
        val items = inspectLevel(sourcePaths, destination).getOrElse { error ->
            sourcePaths.forEach { session.fail(it, PasteFailureReason.COMMAND_FAILED, error.message) }
            return
        }

        items.forEachIndexed { index, item ->
            if (session.shouldStop()) return
            onItemStarted(index, item.name)
            val remainingConflicts = items.drop(index + 1).count { it.targetExistedAtStart }
            pasteItem(item, destination, remainingConflicts, session)
        }
    }

    private suspend fun inspectLevel(
        sourcePaths: List<String>,
        destination: Destination
    ): Result<List<LevelItem>> {
        val targets = sourcePaths.map {
            RemotePaths.childPath(destination.directory, RemotePaths.fileName(it))
        }

        return repository.inspect(sourcePaths + targets).map { infos ->
            sourcePaths.indices.map { index ->
                LevelItem(
                    source = sourcePaths[index],
                    target = targets[index],
                    sourceInfo = infos[index],
                    targetExistedAtStart = infos[index + sourcePaths.size].exists
                )
            }
        }
    }

    private suspend fun pasteItem(
        item: LevelItem,
        destination: Destination,
        remainingConflicts: Int,
        session: PasteSession
    ) {
        if (!item.sourceInfo.exists) {
            session.fail(item.source, PasteFailureReason.SOURCE_MISSING)
            return
        }

        val sourceReal = item.sourceInfo.realPath ?: item.source
        val targetReal = RemotePaths.childPath(destination.realDirectory, item.name)
        val pastesIntoItself = item.sourceInfo.isDirectory &&
                RemotePaths.isSameOrInside(destination.realDirectory, sourceReal)

        when {
            pastesIntoItself -> session.fail(item.source, PasteFailureReason.INTO_ITSELF)
            sourceReal == targetReal -> pasteOntoItself(item, destination, session)
            else -> pasteToTarget(item, destination, sourceReal, targetReal, remainingConflicts, session)
        }
    }

    private suspend fun pasteOntoItself(
        item: LevelItem,
        destination: Destination,
        session: PasteSession
    ) {
        if (session.operationType == OperationType.COPY) {
            keepBoth(item, destination, session)
        } else {
            session.skip()
        }
    }

    private suspend fun pasteToTarget(
        item: LevelItem,
        destination: Destination,
        sourceReal: String,
        targetReal: String,
        remainingConflicts: Int,
        session: PasteSession
    ) {
        val target = repository.inspect(listOf(item.target)).map { it.single() }.getOrElse {
            session.fail(item.source, PasteFailureReason.COMMAND_FAILED, it.message)
            return
        }

        if (!target.exists) {
            transfer(item.source, item.target, session)
            return
        }

        val replaceDeletesSource = RemotePaths.isSameOrInside(sourceReal, targetReal)
        val conflict = FileConflict(
            sourcePath = item.source,
            destPath = item.target,
            operationType = session.operationType,
            destIsDirectory = target.isDirectory,
            sourceIsDirectory = item.sourceInfo.isDirectory,
            fileName = item.name,
            remainingConflicts = remainingConflicts,
            canReplace = !replaceDeletesSource
        )
        val resolution = session.decide(conflict) ?: return

        applyResolution(resolution, item, destination, session)
    }

    private suspend fun applyResolution(
        resolution: ConflictResolution,
        item: LevelItem,
        destination: Destination,
        session: PasteSession
    ) {
        when (resolution) {
            ConflictResolution.SKIP -> session.skip()
            ConflictResolution.KEEP_BOTH -> keepBoth(item, destination, session)
            ConflictResolution.REPLACE -> replace(item, destination, session)
            ConflictResolution.MERGE -> merge(item, session)
        }
    }

    private suspend fun keepBoth(item: LevelItem, destination: Destination, session: PasteSession) {
        val freeName = findFreeName(destination.directory, item.name, item.sourceInfo.isDirectory)
            .getOrElse { error ->
                val reason = if (error is NoFreeNameException) {
                    PasteFailureReason.NO_FREE_NAME
                } else {
                    PasteFailureReason.COMMAND_FAILED
                }
                session.fail(item.source, reason, error.message)
                return
            }

        transfer(item.source, RemotePaths.childPath(destination.directory, freeName), session)
    }

    private suspend fun findFreeName(
        directory: String,
        name: String,
        isDirectory: Boolean
    ): Result<String> {
        val batches = KeepBothNames.candidates(name, isDirectory)
            .take(MAX_KEEP_BOTH_CANDIDATES)
            .chunked(KEEP_BOTH_BATCH_SIZE)

        for (batch in batches) {
            val infos = repository.inspect(batch.map { RemotePaths.childPath(directory, it) })
                .getOrElse { return Result.failure(it) }
            val freeName = batch.zip(infos).firstOrNull { (_, info) -> !info.exists }?.first
            if (freeName != null) return Result.success(freeName)
        }

        return Result.failure(NoFreeNameException())
    }

    private suspend fun transfer(source: String, target: String, session: PasteSession) {
        withContext(NonCancellable) { runTransfer(session.operationType, source, target) }
            .onSuccess { session.complete() }
            .onFailure { session.fail(source, PasteFailureReason.COMMAND_FAILED, it.message) }
    }

    private suspend fun runTransfer(
        operationType: OperationType,
        source: String,
        target: String
    ): Result<Unit> = if (operationType == OperationType.MOVE) {
        repository.move(source, target)
    } else {
        repository.copy(source, target)
    }

    private suspend fun replace(item: LevelItem, destination: Destination, session: PasteSession) {
        val staging = RemotePaths.childPath(destination.directory, temporaryName(STAGING_PREFIX))
        val backup = RemotePaths.childPath(destination.directory, temporaryName(BACKUP_PREFIX))

        withContext(NonCancellable) { swapIn(item, staging, backup, session.operationType) }
            .onSuccess { session.complete() }
            .onFailure { session.fail(item.source, PasteFailureReason.COMMAND_FAILED, it.message) }
    }

    /**
     * Brings the source in under [staging] first and only then swaps it with the existing item, so
     * the existing item is never deleted before its replacement is in place, and a failure at any
     * step puts both back where they were.
     *
     * A move that fails while staging is left alone: across file systems it may have copied
     * everything and deleted part of the source, making the staged copy the only complete one.
     */
    private suspend fun swapIn(
        item: LevelItem,
        staging: String,
        backup: String,
        operationType: OperationType
    ): Result<Unit> {
        runTransfer(operationType, item.source, staging).onFailure { error ->
            if (operationType == OperationType.COPY) repository.deleteFile(staging)
            return Result.failure(error)
        }

        repository.move(item.target, backup).onFailure { error ->
            undoStaging(item.source, staging, operationType)
            return Result.failure(error)
        }

        repository.move(staging, item.target).onFailure { error ->
            repository.move(backup, item.target)
            undoStaging(item.source, staging, operationType)
            return Result.failure(error)
        }

        repository.deleteFile(backup)
        return Result.success(Unit)
    }

    private suspend fun undoStaging(source: String, staging: String, operationType: OperationType) {
        if (operationType == OperationType.MOVE) {
            repository.move(staging, source)
        } else {
            repository.deleteFile(staging)
        }
    }

    private fun temporaryName(prefix: String): String =
        prefix + UUID.randomUUID().toString().replace("-", "")

    /**
     * Pastes the folder's children into the existing folder one level down, where each child gets
     * its own conflict check. A move then removes the source folder, which only succeeds once
     * nothing is left in it, so skipped or failed children keep it alive.
     */
    private suspend fun merge(item: LevelItem, session: PasteSession) {
        val children = repository.listFiles(item.source).getOrElse {
            session.fail(item.source, PasteFailureReason.COMMAND_FAILED, it.message)
            return
        }.filterNot { it.isParentDirectory }.map { it.path }

        if (children.isEmpty()) {
            session.complete()
        } else {
            pasteLevel(children, resolveDestination(item.target), session)
        }

        if (session.operationType == OperationType.MOVE) {
            withContext(NonCancellable) { repository.removeEmptyDirectory(item.source) }
        }
    }

    /**
     * The folder one level of a paste writes into.
     *
     * [directory] is used to build paths, keeping them in the form the user sees; [realDirectory]
     * is only compared against, to recognise the same location reached through a symlink.
     */
    private data class Destination(
        val directory: String,
        val realDirectory: String
    )

    /**
     * One source of a paste level paired with the path it would land on.
     *
     * [targetExistedAtStart] only feeds the "apply to all" count; whether the target exists is
     * asked again right before pasting, because earlier items can create or remove it.
     */
    private data class LevelItem(
        val source: String,
        val target: String,
        val sourceInfo: PathInfo,
        val targetExistedAtStart: Boolean
    ) {
        val name: String
            get() = RemotePaths.fileName(source)
    }

    private class NoFreeNameException : Exception()

    /** Counters, the remembered "apply to all" choice and the stop flag of one paste. */
    private class PasteSession(
        val operationType: OperationType,
        private val resolveConflict: suspend (FileConflict) -> ConflictDecision?,
        private val isCancelled: () -> Boolean
    ) {
        private var completed = 0
        private var skipped = 0
        private val failures = mutableListOf<PasteFailure>()
        private var applyToAll: ConflictResolution? = null
        private var stopped = false

        fun shouldStop(): Boolean {
            if (isCancelled()) stopped = true
            return stopped
        }

        fun complete() {
            completed++
        }

        fun skip() {
            skipped++
        }

        fun fail(sourcePath: String, reason: PasteFailureReason, message: String? = null) {
            failures += PasteFailure(sourcePath, reason, message)
        }

        /** @return the resolution to apply, or null when the user stopped the paste. */
        suspend fun decide(conflict: FileConflict): ConflictResolution? {
            applyToAll?.takeIf(conflict::allows)?.let { return it }

            val decision = resolveConflict(conflict)
            if (decision == null) {
                stopped = true
                return null
            }

            if (decision.applyToAll) applyToAll = decision.resolution
            return decision.resolution.takeIf(conflict::allows) ?: ConflictResolution.SKIP
        }

        fun summary(): PasteSummary = PasteSummary(
            completedCount = completed,
            skippedCount = skipped,
            failures = failures.toList(),
            cancelled = stopped
        )
    }
}
