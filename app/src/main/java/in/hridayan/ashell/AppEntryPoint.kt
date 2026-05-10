package `in`.hridayan.ashell

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.crashreporter.domain.repository.CrashRepository
import `in`.hridayan.ashell.qstiles.data.provider.TileComponentManager

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun crashRepository(): CrashRepository
    fun tileComponentManager(): TileComponentManager
}
