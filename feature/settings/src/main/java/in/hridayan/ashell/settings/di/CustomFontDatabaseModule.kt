package `in`.hridayan.ashell.settings.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.core.common.domain.model.AppFont
import `in`.hridayan.ashell.settings.data.local.database.CustomFontDao
import `in`.hridayan.ashell.settings.data.local.database.CustomFontDatabase
import `in`.hridayan.ashell.settings.data.repository.CustomFontRepositoryImpl
import `in`.hridayan.ashell.settings.domain.repository.CustomFontRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CustomFontDatabaseModule {

    @Binds
    @Singleton
    abstract fun bindCustomFontRepository(
        impl: CustomFontRepositoryImpl
    ): CustomFontRepository

    companion object {

        @Provides
        @Singleton
        fun provideCustomFontDatabase(@ApplicationContext context: Context): CustomFontDatabase {
            return Room.databaseBuilder(
                context,
                CustomFontDatabase::class.java,
                CustomFontDatabase.DATABASE_NAME
            )
                .fallbackToDestructiveMigration(false)
                .addCallback(object : androidx.room.RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            "INSERT INTO sqlite_sequence (name, seq) VALUES ('custom_fonts', ${AppFont.CUSTOM_FONT_ID_OFFSET - 1})"
                        )
                    }
                })
                .build()
        }

        @Provides
        fun provideCustomFontDao(database: CustomFontDatabase): CustomFontDao =
            database.customFontDao()
    }
}
