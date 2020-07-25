package com.kpstv.yts.data.db.localized

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kpstv.yts.data.converters.TMdbConverter
import com.kpstv.yts.data.models.data.data_tmdb

@Database(
    entities = [data_tmdb::class],
    version = 1
)
@TypeConverters(
    TMdbConverter::class
)
abstract class SuggestionDatabase : RoomDatabase() {
    abstract fun getTMdbDao(): SuggestionDao
}