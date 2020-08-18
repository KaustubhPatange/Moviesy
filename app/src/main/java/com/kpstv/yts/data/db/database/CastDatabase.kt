package com.kpstv.yts.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kpstv.yts.data.converters.TMdbCastConverter
import com.kpstv.yts.data.converters.TMdbCastMovieConverter
import com.kpstv.yts.data.db.localized.TmDbCastMovieDao
import com.kpstv.yts.data.db.localized.TmDbCastDao
import com.kpstv.yts.data.models.TmDbCastDomain
import com.kpstv.yts.data.models.TmDbCastMovie

@Database(
    entities = [TmDbCastDomain::class, TmDbCastMovie::class],
    version = 1
)
@TypeConverters(
    TMdbCastConverter::class,
    TMdbCastMovieConverter::class
)
abstract class CastDatabase : RoomDatabase() {
    abstract fun getCastDao(): TmDbCastDao
    abstract fun getCastMovieDao(): TmDbCastMovieDao
}