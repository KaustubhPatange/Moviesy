package com.kpstv.yts.ui.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.core.view.doOnLayout
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.*
import com.kpstv.common_moviesy.extensions.colorFrom
import com.kpstv.yts.R
import com.kpstv.yts.databinding.CustomContinueWatcherMainBinding
import com.kpstv.yts.extensions.SimpleCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import java.io.File

class ContinueWatcherHelper(private val context: Context, private val lifecycleOwner: LifecycleOwner) {
    private var saveView: View? = null
    private val dataStoreHelper = WatchDataStoreHelper(context)
    var isSaved: Boolean = false

    private val lock = Any()

    private val lifecycleObserver = object: DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            dataStoreHelper.cancel()
            lifecycleOwner.lifecycle.removeObserver(this)
            super.onDestroy(owner)
        }
    }

    init {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }

    @SuppressLint("SetTextI18n")
    fun inflate(parent: ViewGroup, addToTop: Boolean = true, onPlayButtonClicked: (Int) -> Unit): View {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_continue_watcher_main, parent, false)
        val binding = CustomContinueWatcherMainBinding.bind(view).apply {
            root.alpha = 0f
            root.setOnLongClickListener {
                lifecycleOwner.lifecycleScope.launch { dataStoreHelper.clear() }
                true
            }
        }
        lifecycleOwner.lifecycleScope.launch {
            dataStoreHelper.watcher.collect { watcher ->
                if (watcher == null) {
                    binding.root.animate().alpha(0f)
                        .setDuration(200)
                        .withEndAction {
                            parent.removeView(binding.root)
                        }.start()
                }
                watcher?.let {
                    val bitmap = BitmapFactory.decodeStream(imageCacheFile.inputStream())
                    val pixel = bitmap.getPixel(0,bitmap.height - 50)
                    if (ColorUtils.calculateLuminance(pixel) < 0.25) {
                        binding.tvTitle.setTextColor(context.colorFrom(R.color.text_light))
                    } else {
                        binding.tvTitle.setTextColor(context.colorFrom(R.color.text_dark))
                    }
                    binding.tvTitle.text = watcher.title
                    binding.tvLeftAt.text = "${context.getString(R.string.cw_left_at)} ${watcher.lastPosition / (1000 * 60)} mins"
                    binding.ivMain.setImageBitmap(bitmap)
                    binding.btnPlay.setOnClickListener {
                        lifecycleOwner.lifecycleScope.launch {
                            dataStoreHelper.clear()
                            onPlayButtonClicked.invoke(watcher.movieId)
                        }
                    }
                    parent.addView(binding.root, if (addToTop) 0 else -1)
                    if (binding.root.alpha == 0f) {
                        binding.root.animate().alpha(1f).start()
                    }
                }
            }
        }
        return binding.root
    }

    fun save(bitmap: Bitmap, rootView: ViewGroup, watcher: Watcher, onComplete: SimpleCallback = {}) = synchronized(lock) {
        val saveView = LayoutInflater.from(context).inflate(R.layout.custom_continue_watcher_save, null).apply {
            alpha = 0f
        }
        rootView.addView(saveView)
        this.saveView = saveView

        saveView.doOnLayout {
            saveView.animate().alpha(0.8f).setDuration(100).withEndAction {
                saveSilent(bitmap, watcher)
                Handler(Looper.getMainLooper()).post(onComplete)
            }.start()
        }
    }

    fun saveSilent(bitmap: Bitmap, watcher: Watcher) {
        val stream = context.contentResolver.openOutputStream(imageCacheFile.toUri())
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        if (lifecycleOwner.lifecycleScope.isActive) {
            lifecycleOwner.lifecycleScope.launch {
                dataStoreHelper.update(watcher)
            }
        }
        isSaved = true
    }

    data class Watcher(val movieId: Int, val title: String, val lastPosition: Int)

    internal class WatchDataStoreHelper(context: Context) {
        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(WATCHER_PB) },
            scope = scope
        )

        val watcher: Flow<Watcher?> = dataStore.data.map { preferences ->
            val movieId = preferences[movieIdKey] ?: return@map null
            val movieTitle = preferences[movieTitleKey] ?: return@map null
            val lastPosition = preferences[lastPositionKey] ?: return@map null
            Watcher(movieId, movieTitle, lastPosition)
        }

        suspend fun clear() = dataStore.edit { it.clear() }

        suspend fun update(watcher: Watcher) {
            dataStore.edit { prefs ->
                prefs[movieIdKey] = watcher.movieId
                prefs[movieTitleKey] = watcher.title
                prefs[lastPositionKey] = watcher.lastPosition
            }
        }

        fun cancel() = scope.cancel()

        private val movieIdKey = intPreferencesKey("movie_id")
        private val movieTitleKey = stringPreferencesKey("movie_title")
        private val lastPositionKey = intPreferencesKey("last_position")
    }

    private val imageCacheFile = File(context.cacheDir, LAST_WATCH_IMAGE_PNG)

    companion object {
        private const val LAST_WATCH_IMAGE_PNG = "last_watch_image.png"
        private const val WATCHER_PB = "watcher"
    }
}