package com.kpstv.yts.data.converters

object QueryConverter {
    @JvmStatic
    fun fromMapToString(map: Map<String, String>): String {
        val keys = ArrayList(map.keys)
        val values = ArrayList(map.values)
        val builder = StringBuilder()

        for (c in map.keys.indices) {
            builder.append(keys[c]).append("=").append(values[c])
            if (c != map.keys.size - 1) {
                builder.append("&")
            }
        }
        return builder.toString()
    }

    @JvmStatic
    fun toMapfromString(queryString: String): Map<String, String> {
        val map = HashMap<String, String>()
        val splitVal = queryString.split("&")
        splitVal.forEach {
            map[it.split("=")[0]] = it.split("=")[1]
        }
        return map
    }
}