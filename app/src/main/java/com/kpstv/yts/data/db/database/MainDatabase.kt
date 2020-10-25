package com.kpstv.yts.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kpstv.yts.data.converters.*
import com.kpstv.yts.data.db.localized.*
import com.kpstv.yts.data.models.Movie
import com.kpstv.yts.data.models.data.data_history
import com.kpstv.yts.data.models.data.data_main
import com.kpstv.yts.data.models.response.Model

@Database(
    entities = [
        data_main::class,
        Model.response_favourite::class,
        Model.response_download::class,
        Model.response_pause::class,
        Movie::class,
        data_history::class],
    version = DatabaseMigration.DB_VERSION
)
@TypeConverters(
    MovieShortConverter::class,
    GenreConverter::class,
    TMdbConverter::class,
    TorrentListConverter::class,
    com.kpstv.yts.data.models.TorrentJobConverter::class,
    com.kpstv.yts.data.models.CastListConverter::class,
    com.kpstv.yts.data.models.CrewListConverter::class,
    com.kpstv.yts.data.models.TorrentConverter::class
)
abstract class MainDatabase : RoomDatabase() {
    abstract fun getMainDao(): MainDao
    abstract fun getFavDao(): FavouriteDao
    abstract fun getDownloadDao(): DownloadDao
    abstract fun getMovieDao(): MovieDao
    abstract fun getPauseDao(): PauseDao
    abstract fun getHistoryDao(): HistoryDao
}