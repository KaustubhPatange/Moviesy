package com.kpstv.yts.data.models

import com.google.gson.annotations.SerializedName

data class AppDatabase(
    val misc: Misc,
    val tmdb: Tmdb,
    val yts: Yts,
    val update: Update,
    @SerializedName("vpn_affected_countries")
    val vpnAffectedCountries: List<String>,
    @SerializedName("vpn_configurations")
    val vpnConfigurations: List<VpnConfiguration>
) {
    data class Update(
        val deprecatedVersionCode: Int,
        val versionCode: Int,
        val url: String
    )

    data class Misc(
        val suggestionApi: String
    )

    data class Tmdb(
        val apiKey: String,
        val base: String,
        val image: String
    )

    data class Yts(
        val base: String,
        val yify: String
    )

    data class VpnConfiguration(
        val country: String,
        val ovpn: String,
        val ovpnPassword: String,
        val ovpnUsername: String,
        val ip: String
    )
}