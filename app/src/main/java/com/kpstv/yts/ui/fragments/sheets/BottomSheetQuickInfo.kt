package com.kpstv.yts.ui.fragments.sheets

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.yts.R
import com.kpstv.yts.models.MovieShort
import com.kpstv.yts.models.response.Model
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getBulletSymbol
import com.kpstv.yts.extensions.utils.CustomBottomItem
import com.kpstv.yts.extensions.utils.GlideApp
import com.kpstv.yts.ui.viewmodels.MainViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.bottom_sheet_quickinfo.*
import kotlinx.android.synthetic.main.bottom_sheet_quickinfo.view.*

class BottomSheetQuickInfo(private val viewModel: MainViewModel) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.bottom_sheet_quickinfo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movie = arguments?.getSerializable("model") as MovieShort

        GlideApp.with(context?.applicationContext!!).asBitmap().load(movie.bannerUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    shimmerImageView.setImage(resource)
                }
            })

        view.item_title.text = movie.title
        view.item_subText.text = "${movie.year} ${getBulletSymbol()} ${movie.runtime} mins"

        /** Injecting view options */
        viewModel.isFavourite({ b ->
            var title = "Add to watchlist"
            var icon = R.drawable.ic_favorite_no

            if (b) {
                title = "Remove from watchlist"
                icon = R.drawable.ic_favorite_yes
            }

            val watchlistLayout = CustomBottomItem(context!!)
            watchlistLayout.setUp(icon, title, view.addLayout)
            watchlistLayout.onClickListener = {
                if (b) {
                    viewModel.removeFavourite(movie.movieId!!)
                    Toasty.info(context!!, getString(R.string.remove_watchlist)).show()
                } else {
                    viewModel.addToFavourite(Model.response_favourite.from(movie))
                    Toasty.info(context!!, getString(R.string.add_watchlist)).show()
                }
                dismiss()
            }

        }, movie.movieId!!)
    }
}