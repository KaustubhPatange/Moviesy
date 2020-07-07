package com.kpstv.yts.data.db.localized

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kpstv.yts.data.converters.TMdbConverter
import com.kpstv.yts.models.data.data_tmdb

@Database(
    entities = [data_tmdb::class],
    version = 1
)
@TypeConverters(
    TMdbConverter::class
)
abstract class SuggestionDatabase : RoomDatabase() {
    abstract fun getTMdbDao(): SuggestionDao

    /*companion object {
        @Volatile
        private var instance: SuggestionDatabase? = null
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
                SuggestionDatabase::class.java,
                "suggestionMovie.db"
            )
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
    }*/
}