package com.kpstv.yts.data.db.localized

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kpstv.yts.data.converters.*
import com.kpstv.yts.models.Movie
import com.kpstv.yts.models.data.data_main
import com.kpstv.yts.models.response.Model

@Database(
    entities = [
        data_main::class,
        Model.response_favourite::class,
        Model.response_download::class,
        Model.response_pause::class,
        Movie::class],
    version = 1
)
@TypeConverters(
    MovieShortConverter::class,
    CastConverter::class,
    GenreConverter::class,
    TMdbConverter::class,
    TorrentListConverter::class,
    TorrentJobConverter::class,
    TorrentConverter::class
)
abstract class MainDatabase : RoomDatabase() {
    abstract fun getMainDao(): MainDao
    abstract fun getFavDao(): FavouriteDao
    abstract fun getDownloadDao(): DownloadDao
    abstract fun getMovieDao(): MovieDao
    abstract fun getPauseDao(): PauseDao
}