package com.kpstv.yts.vpn.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kpstv.yts.vpn.VpnConfiguration

@Database(
    entities = [VpnConfiguration::class],
    version = 1
)
abstract class VPNDatabase : RoomDatabase() {
    abstract fun getVPNDao(): VPNDao
}