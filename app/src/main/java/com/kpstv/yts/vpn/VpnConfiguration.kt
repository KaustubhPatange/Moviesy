package com.kpstv.yts.vpn

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_vpnconfigs")
data class VpnConfiguration(
    val country: String,
    val countryFlagUrl: String,
    val ip: String,
    val sessions: String,
    val upTime: String,
    val speed: String,
    val config: String,
    val score: Long,
    val expireTime: Long,
    val premium: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}