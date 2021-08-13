package com.kpstv.yts.vpn.db

import com.kpstv.yts.extensions.utils.RetrofitUtils
import com.kpstv.yts.vpn.VpnConfiguration
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VPNRepository @Inject constructor(
    private val vpnDao: VPNDao,
    private val retrofitUtils: RetrofitUtils
) {
    companion object {
        private val ipRegex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}".toRegex()
        private val dateFormatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)

        fun formatExpireTime(expireTime: Long): String {
            return try {
                dateFormatter.parse(expireTime.toString())?.toGMTString() ?: expireTime.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                "Unknown"
            }
        }
    }

    suspend fun fetch(): List<VpnConfiguration> {
        val local = fetchFromLocal()
        val offsetDate = dateFormatter.format(Calendar.getInstance().time).toLong()
        if (local.isNotEmpty() && offsetDate < local.first().expireTime) {
            return local
        }
        val network = fetchFromNetwork()
        val sorted = network.sortedByDescending { it.speed.toFloat() }.subList(0, minOf(3, network.size)).map {
            it.copy(premium = true)
        }.union(network).distinctBy { it.ip }
        vpnDao.insertAll(sorted)
        return sorted
    }

    private suspend fun fetchFromLocal(): List<VpnConfiguration> {
        return vpnDao.getAll()
    }

    private suspend fun fetchFromNetwork(): List<VpnConfiguration> {
        val vpnConfigurations = arrayListOf<VpnConfiguration>()

        try {
            val vpnResponse = retrofitUtils.makeHttpCallAsync("https://www.vpngate.net/en")
            if (vpnResponse.isSuccessful) {

                val offsetDateTime = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 7) }.time
                val expiredTime = dateFormatter.format(offsetDateTime).toLong()

                val body = vpnResponse.body?.string()
                vpnResponse.close() // close Stream

                val doc = Jsoup.parse(body)

                val table = doc.getElementById("vpngate_inner_contents_td").children().findLast { it.id() == "vg_hosts_table_id" }?.child(0) ?: return vpnConfigurations
                vpnConfigurations.clear()

                val maxIteration = minOf(7, table.childrenSize())
                for (i in 1 until table.childrenSize()) {

                    if (vpnConfigurations.size >= maxIteration) return vpnConfigurations

                    val tr = table.child(i)

                    if (tr.getElementsByClass("vg_table_header").size == 0) {
                        val imageUrl = tr.child(0).child(0).attr("src").replace("../", "https://www.vpngate.net/")
                        val country = tr.child(0).text()

                        if (country == "Reserved") continue

                        // no more than 3 countries....
                        if (vpnConfigurations.count { it.country == country } == 3) continue

                        val ip = ipRegex.find(tr.child(1).html())?.value ?: ""

                        val sessions = tr.child(2).child(0).child(0).text()
                        val uptime = tr.child(2).child(2).text()

                        val speed = tr.child(3).child(0).child(0).text()

                        if (speed == "0.00 Mbps") continue

                        val ovpnConfigElement = tr.child(6)
                        if (ovpnConfigElement.childrenSize() == 0) continue

                        val score = tr.child(tr.childrenSize() - 1).child(0).child(0).text()
                            .replace(",","").toLong()

                        val configUrl = "https://www.vpngate.net/en/" + ovpnConfigElement.child(0).attr("href")

                        val configResponse = retrofitUtils.makeHttpCallAsync(configUrl)
                        if (configResponse.isSuccessful) {
                            val configBody = configResponse.body?.string()
                            configResponse.close() // Always close stream

                            val hrefElements = Jsoup.parse(configBody).getElementsByAttribute("href")
                            val ovpnConfig =
                                hrefElements.find { it.attr("href").contains(".ovpn") }?.attr("href") ?: continue

                            val configDataResponse = retrofitUtils.makeHttpCallAsync("https://www.vpngate.net/$ovpnConfig")
                            if (configDataResponse.isSuccessful) {
                                val data = configDataResponse.body?.string() ?: continue
                                configDataResponse.close() // Always close stream
                                val vpnConfig = VpnConfiguration(
                                    formatCountry(country), imageUrl, ip, sessions, uptime, speed.replace("Mbps", "").trim(),
                                    data, score,
                                    expiredTime
                                )
                                vpnConfigurations.add(vpnConfig)
                            } else continue
                        } else {
                            continue
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return vpnConfigurations
    }

    private fun formatCountry(country: String): String {
        var ct = country
        list.forEach { ct = ct.replace(it, "") }
        return ct.trim()
    }

    private val list = listOf("Federation", "Republic of")
}