package com.kpstv.yts.ui.controllers.upcoming

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.controllers.progressBar
import java.util.concurrent.CopyOnWriteArrayList

class UpcomingController(private val context: Context, private val goToDetail: (movieUrl: String) -> Unit) : EpoxyController() {

    private val upcomingModels: CopyOnWriteArrayList<MovieShort> = CopyOnWriteArrayList()

    fun submitUpcomingModels(models: List<MovieShort>) {
        upcomingModels.clear()
        upcomingModels.addAll(models)
        requestModelBuild()
    }

    override fun buildModels() = with(context) context@{
        val goToDetail = goToDetail

//        upcomingTitle {
//            id("upcoming-movies")
//            title(getString(R.string.upcoming_movies))
//        }

        if (upcomingModels.isNotEmpty()) {
            for (upcomingModel in upcomingModels) {
                upcomingAvailable {
                    id("upcoming-${upcomingModel.title}")
                    title(upcomingModel.title)
                    year(upcomingModel.year!!)
                    imageUrl(upcomingModel.bannerUrl)
                    rating(upcomingModel.rating)
                    qualityString(upcomingModel.progress!!.forQuality)
                    progress(upcomingModel.progress.progress)
                    if (upcomingModel.url != null) {
                        clickListener { goToDetail(upcomingModel.url) }
                    }
                    if (upcomingModel.imdbCode != null) {
                        comingSoonListener {
                            val imdbUrl = AppUtils.getImdbUrl(upcomingModel.imdbCode)
                            AppUtils.launchUrl(this@context, imdbUrl)
                        }
                    }
                }
            }
        } else progressBar { id("upcoming-progress-bar") }
    }
}