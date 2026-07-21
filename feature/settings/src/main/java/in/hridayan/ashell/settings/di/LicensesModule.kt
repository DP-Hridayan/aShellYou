package `in`.hridayan.ashell.settings.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.settings.data.repository.LicensesRepositoryImpl
import `in`.hridayan.ashell.settings.domain.repository.LicensesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LicensesModule {

    @Provides
    @Singleton
    fun provideLicensesRepository(
        @ApplicationContext context: Context,
    ): LicensesRepository = LicensesRepositoryImpl(context)
}
