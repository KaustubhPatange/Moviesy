package com.kpstv.yts.extensions.utils

class LangCodeUtils {
    companion object {
        private val langList = mapOf(
            "en" to "English",
            "mr" to "Marathi",
            "fr" to "French",
            "de" to "German",
            "ja" to "Japanese",
            "ko" to "Korean",
            "ru" to "Russian",
            "es" to "Spanish",
            "hi" to "Hindi",
            "th" to "Thai",
            "pt" to "Portuguese",
            "it" to "Italian",
            "ar" to "Arabic",
            "zh" to "Chinese",
            "he" to "Hebrew",
            "cn" to "Chinese",
            "vi" to "Vietnamese"
        )

        fun parse(code: String) =
            langList[code] ?: code
    }
}