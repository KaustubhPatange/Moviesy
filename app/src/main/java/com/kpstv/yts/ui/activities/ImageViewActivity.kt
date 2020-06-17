package com.kpstv.yts.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.yts.R
import com.kpstv.yts.extensions.utils.GlideApp
import kotlinx.android.synthetic.main.activity_image_view.*

class ImageViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        GlideApp.with(applicationContext).load(intent.extras?.getString("imageUrl")).into(photo_view)
    }
}
