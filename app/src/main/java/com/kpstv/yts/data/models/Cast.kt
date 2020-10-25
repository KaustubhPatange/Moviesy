package com.kpstv.yts.data.models

import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType
import com.kpstv.yts.AppInterface
import java.io.Serializable

@AutoGenerateListConverter(using = ConverterType.GSON)
data class Cast(
    val name: String, val character_name: String, val url_small_image: String,
    val imdb_code: String
) : Serializable {
    companion object {
        fun from(data: TmDbCast) =
            Cast(
                data.name, data.character,
                "${AppInterface.TMDB_IMAGE_PREFIX}${data.profilePath}",
                data.personId.toString()
            )
    }
}