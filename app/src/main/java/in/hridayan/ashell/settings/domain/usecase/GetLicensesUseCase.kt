package `in`.hridayan.ashell.settings.domain.usecase

import `in`.hridayan.ashell.settings.domain.model.LibraryItem
import `in`.hridayan.ashell.settings.domain.repository.LicensesRepository
import javax.inject.Inject

/**
 * Use case: fetch all open-source libraries from the repository.
 *
 * Keeping this as a separate use case follows Clean Architecture
 * and makes the logic independently testable.
 */
class GetLicensesUseCase @Inject constructor(
    private val repository: LicensesRepository,
) {
    suspend operator fun invoke(): List<LibraryItem> = repository.getLibraries()
}
