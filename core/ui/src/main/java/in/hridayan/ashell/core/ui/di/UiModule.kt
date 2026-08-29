package `in`.hridayan.ashell.core.ui.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.presentation.theme.data.CustomColorSchemeDao
import `in`.hridayan.ashell.core.presentation.theme.data.CustomThemeDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UiModule {

    @Provides
    @Singleton
    fun provideCustomThemeDatabase(app: Application): CustomThemeDatabase {
        return Room.databaseBuilder(
            app,
            CustomThemeDatabase::class.java,
            CustomThemeDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideCustomThemeDao(db: CustomThemeDatabase): CustomColorSchemeDao {
        return db.customColorSchemeDao
    }
}
