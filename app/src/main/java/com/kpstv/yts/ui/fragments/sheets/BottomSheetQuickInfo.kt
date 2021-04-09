package com.kpstv.yts.ui.fragments.sheets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kpstv.yts.R
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.data.models.response.Model
import com.kpstv.yts.databinding.BottomSheetQuickinfoBinding
import com.kpstv.yts.extensions.views.ExtendedBottomSheetDialogFragment
import com.kpstv.yts.extensions.utils.AppUtils.Companion.getBulletSymbol
import com.kpstv.yts.extensions.common.CustomBottomItem
import com.kpstv.yts.extensions.utils.GlideApp
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.helpers.ThemeHelper.registerForThemeChange
import com.kpstv.yts.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty

@AndroidEntryPoint
class BottomSheetQuickInfo : ExtendedBottomSheetDialogFragment(R.layout.bottom_sheet_quickinfo) {

    private val viewModel by viewModels<MainViewModel>()
    private val binding by viewBinding(BottomSheetQuickinfoBinding::bind)

    private lateinit var movie: MovieShort

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerForThemeChange()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        movie = arguments?.getSerializable("model") as? MovieShort ?: return

        GlideApp.with(requireContext().applicationContext).asBitmap().load(movie.bannerUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.shimmerImageView.setImageBitmap(resource)
                }
            })

        binding.itemTitle.text = movie.title
        binding.itemSubText.text = "${movie.year} ${getBulletSymbol()} ${movie.runtime} mins"

        setupMenus()
    }

    private fun setupMenus() {
        val watchlistItem = CustomBottomItem(requireContext()).apply {
            setUp(R.drawable.ic_favorite_no, getString(R.string.add_to_watch), binding.addLayout)
        }
        viewModel.isMovieFavourite(movie.movieId!!) { isFavourite ->
            if (isFavourite) {
                watchlistItem.updateTitle(getString(R.string.remove_from_watchlist))
                watchlistItem.updateIcon(R.drawable.ic_favorite_yes)
            }
            watchlistItem.setOnClick {
                if (isFavourite) {
                    viewModel.removeFavourite(movie.movieId!!)
                    Toasty.info(requireContext(), getString(R.string.remove_watchlist)).show()
                } else {
                    viewModel.addToFavourite(Model.response_favourite.from(movie))
                    Toasty.info(requireContext(), getString(R.string.add_watchlist)).show()
                }
                dismiss()
            }
        }

        CustomBottomItem(requireContext()).apply {
            setUp(R.drawable.ic_share, getString(R.string.share), binding.addLayout)
            setOnClick {
                AppUtils.shareUrl(requireActivity(), movie.url!!)
                dismiss()
            }
        }
    }
}