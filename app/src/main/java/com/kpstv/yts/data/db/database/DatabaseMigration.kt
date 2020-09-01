package com.kpstv.yts.data.db.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigration {
    const val DB_VERSION = 2

    val MIGRATIONS: Array<Migration>
        get() = arrayOf(
            migration12()
        )

    private fun migration12(): Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE table_movie ADD COLUMN crew TEXT")
        }
    }
}