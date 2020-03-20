package com.kpstv.yts.data.db.localized

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kpstv.yts.data.converters.*
import com.kpstv.yts.models.Movie
import com.kpstv.yts.models.data.data_main
import com.kpstv.yts.models.data.data_tmdb
import com.kpstv.yts.models.response.Model

@Database(
    entities = [
        data_main::class,
        Model.response_favourite::class,
        Model.response_download::class,
        Movie::class],
    version = 1
)
@TypeConverters(
    MovieShortConverter::class,
    CastConverter::class,
    GenreConverter::class,
    TMdbConverter::class,
    TorrentConverter::class
)
abstract class MainDatabase : RoomDatabase() {
    abstract fun getMainDao(): MainDao
    abstract fun getFavDao(): FavouriteDao
    abstract fun getDownloadDao(): DownloadDao
    abstract fun getMovieDao(): MovieDao

    companion object {
        @Volatile
        private var instance: MainDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance
            ?: synchronized(LOCK) {
                instance ?: buildDatabase(
                    context
                ).also { instance = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context,
                MainDatabase::class.java,
                "main.db"
            )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
    }
}