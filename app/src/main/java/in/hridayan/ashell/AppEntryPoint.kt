package `in`.hridayan.ashell

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.crashreporter.domain.repository.CrashRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun crashRepository(): CrashRepository
}