package com.kpstv.yts.data.models

import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType
import com.kpstv.yts.AppInterface.Companion.TMDB_IMAGE_PREFIX

@AutoGenerateListConverter(using = ConverterType.GSON)
data class Crew(
    val name: String,
    val job: String,
    val imageUrl: String,
    val department: String,
    val genderId: Int,
    val creditId: String
) {
    companion object {
        fun from(data: TmDbCrew) =
            Crew(
                name = data.name,
                imageUrl = "${TMDB_IMAGE_PREFIX}${data.profilePath}",
                creditId = data.creditId,
                genderId = data.gender,
                job = data.job,
                department = data.department
            )
    }
}