package `in`.hridayan.ashell.qstiles.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.qstiles.data.dao.TileLogDao
import `in`.hridayan.ashell.qstiles.data.database.TileLogDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TileLogModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TileLogDatabase {
        return Room.databaseBuilder(
            context,
            TileLogDatabase::class.java,
            "tile_logs_db"
        ).build()
    }

    @Provides
    fun provideDao(db: TileLogDatabase): TileLogDao {
        return db.dao()
    }
}