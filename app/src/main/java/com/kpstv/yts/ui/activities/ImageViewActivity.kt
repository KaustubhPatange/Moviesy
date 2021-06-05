package com.kpstv.yts.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kpstv.common_moviesy.extensions.*
import com.kpstv.yts.databinding.ActivityImageViewBinding
import com.kpstv.yts.extensions.utils.GlideApp

class ImageViewActivity : AppCompatActivity() {

    companion object {
        private const val CURRENT_IMAGE_URL = "com.kpstv.yts.current_image_url"
        private const val HIGH_IMAGE_URL = "com.kpstv.yts.high_image_url"

        fun launch(context: Context, mediumCoverUrl: String, highResImageUrl: String, options: Bundle? = null) {
            context.startActivity(Intent(context, ImageViewActivity::class.java).apply {
                putExtra(CURRENT_IMAGE_URL, mediumCoverUrl)
                putExtra(HIGH_IMAGE_URL, highResImageUrl)
            }, options)
        }
    }

    private val binding by viewBinding(ActivityImageViewBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        makeFullScreen()
        transparentStatusBar()
        transparentNavigationBar()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.root.applyTopInsets(pad = true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        GlideApp.with(this).load(intent.extras?.getString(CURRENT_IMAGE_URL))
            .into(binding.photoView)
        GlideApp.with(this).asBitmap().load(intent.extras?.getString(HIGH_IMAGE_URL))
            .into(object: CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.photoView.setImageBitmap(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }
}
