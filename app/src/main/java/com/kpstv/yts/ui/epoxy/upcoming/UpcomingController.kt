package com.kpstv.yts.ui.epoxy.upcoming

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.epoxy.customTip
import com.kpstv.yts.ui.epoxy.progressBar
import java.util.concurrent.CopyOnWriteArrayList

class UpcomingController(private val context: Context, private val goToDetail: (movieUrl: String) -> Unit) : EpoxyController() {

    private val appPreference by context.defaultPreference()

    private val upcomingModels: CopyOnWriteArrayList<MovieShort> = CopyOnWriteArrayList()

    fun submitUpcomingModels(models: List<MovieShort>) {
        upcomingModels.clear()
        upcomingModels.addAll(models)
        requestModelBuild()
    }

    override fun buildModels() = with(context) context@{
        val goToDetail = goToDetail
        val appPreference =  appPreference

        if (!appPreference.isUpcomingTipShown()) {
            customTip {
                id("upcoming-tip")
                title(getString(R.string.upcoming_tip_title))
                message(getString(R.string.upcoming_tip_text))
                clickListener {
                    appPreference.setUpcomingTipShown(true)
                    this@UpcomingController.requestModelBuild()
                }
            }
        }

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