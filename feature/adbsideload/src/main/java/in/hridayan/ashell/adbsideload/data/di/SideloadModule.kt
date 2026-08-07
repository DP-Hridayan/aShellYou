package `in`.hridayan.ashell.adbsideload.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.adbsideload.data.repository.SideloadRepositoryImpl
import `in`.hridayan.ashell.adbsideload.domain.repository.SideloadRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SideloadModule {

    @Provides
    @Singleton
    fun provideSideloadRepository(@ApplicationContext context: Context): SideloadRepository {
        return SideloadRepositoryImpl(context)
    }
}
