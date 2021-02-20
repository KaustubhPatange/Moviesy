package com.kpstv.yts.ui.fragments.sheets

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.common_moviesy.extensions.*
import com.kpstv.common_moviesy.extensions.utils.FileUtils
import com.kpstv.yts.AppInterface.Companion.SUBTITLE_LOCATION
import com.kpstv.yts.AppInterface.Companion.YIFY_BASE_URL
import com.kpstv.yts.R
import com.kpstv.yts.data.models.Subtitle
import com.kpstv.yts.databinding.BottomSheetSubtitlesBinding
import com.kpstv.yts.extensions.AdapterOnSingleClick
import com.kpstv.yts.data.models.Result
import com.kpstv.yts.extensions.unzip
import com.kpstv.yts.extensions.utils.*
import com.kpstv.yts.extensions.views.ExtendedBottomSheetDialogFragment
import com.kpstv.yts.ui.dialogs.AlertNoIconDialog
import com.kpstv.yts.ui.helpers.InterstitialAdHelper
import com.kpstv.yts.ui.helpers.SubtitleHelper
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottom_sheet_subtitles.*
import kotlinx.android.synthetic.main.item_subtitles.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.File
import java.util.concurrent.Callable
import javax.inject.Inject

/**
 * The class was made when I was just starting with Kotlin.
 * At that time I wasn't familiar with coroutines so you may see
 * some reactiveX extensions.
 */

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class BottomSheetSubtitles : ExtendedBottomSheetDialogFragment(R.layout.bottom_sheet_subtitles) {
    private val binding by viewBinding(BottomSheetSubtitlesBinding::bind)

    @Inject
    lateinit var flagUtils: FlagUtils

    @Inject
    lateinit var ytsParser: YTSParser

    @Inject
    lateinit var interstitialAdHelper: InterstitialAdHelper

    private val TAG = "BottomSheetSubtiles"

    private var subtitleModels = listOf<Subtitle>()
    private lateinit var adapter: SubtitleAdapter
    private lateinit var subtitleFetch: Disposable
    private lateinit var imdb_code: String
    private var hasEnglish = false
    private var hasSpanish = false
    private var hasArabic = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewSubtitles.layoutManager = LinearLayoutManager(context)
        binding.subtitleMainLayout.hide()
        imdb_code = tag as String

        /** Fetching subtitles */

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            fetchSubtitles()
        }
    }

    private suspend fun fetchSubtitles() {
        val result = ytsParser.fetchSubtitles(imdb_code)
        when(result) {
            is SubtitleResult.Success -> {
                progressBar.visibility = View.GONE

                if (result.list.isNotEmpty()) {
                    subtitleModels = result.list

                    adapter = SubtitleAdapter(requireContext(), flagUtils, subtitleModels) { subtitle, i ->
                        /** @Admob Show ad first and then download subtitles */
                        interstitialAdHelper.showAd {
                            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                                parseSubtitle(subtitle, i)
                            }
                        }
                    }

                    recyclerView_subtitles.setHasFixedSize(true)
                    recyclerView_subtitles.adapter = adapter

                    binding.subtitleMainLayout.show()

                    hasArabic = result.hasArabic
                    hasEnglish = result.hasEnglish
                    hasSpanish = result.hasSpanish

                    handleFilter()
                } else {
                    Toasty.error(requireContext(), getString(R.string.no_subtitles)).show()
                    dismiss()
                }
            }
            is SubtitleResult.Error -> {
                AlertNoIconDialog.Companion.Builder(requireContext()).apply {
                    setTitle(getString(R.string.error))
                    setMessage("Failed to fetch subtitles due to: ${result.ex.message}")
                    setPositiveButton(getString(R.string.yes)) { dismiss() }
                }.show()
            }
        }
    }

    private fun handleFilter() {

        if (subtitleModels.size < 10) {
            binding.filterLayout.visibility = View.GONE
            binding.separatorView.visibility = View.GONE
            return
        }

        if (!hasArabic) chip_arabic.visibility = View.GONE
        if (!hasEnglish) chip_english.visibility = View.GONE
        if (!hasSpanish) chip_spanish.visibility = View.GONE

        val onCheckListener = CompoundButton.OnCheckedChangeListener { v, b ->
            if (b) filterRecyclerView(v.text.toString())
        }

        chip_arabic.setOnCheckedChangeListener(onCheckListener)
        chip_english.setOnCheckedChangeListener(onCheckListener)
        chip_spanish.setOnCheckedChangeListener(onCheckListener)
        chip_all.setOnCheckedChangeListener(onCheckListener)
    }

    private fun filterRecyclerView(language: String) {
        when (language) {
            "All" -> {
                adapter.models = subtitleModels
                adapter.notifyDataSetChanged()
            }
            else -> {
                val sublist = subtitleModels.filter { s -> s.country == language }
                adapter.models = sublist
                adapter.notifyDataSetChanged()
            }
        }
        recyclerView_subtitles.smoothScrollToPosition(0)
    }

    private suspend fun parseSubtitle(model: Subtitle, i: Int) {
        model.isDownload = true
        adapter.notifyItemChanged(i)

        val result = ytsParser.parseSubtitleLink(model.fetchEndpoint)
        when(result) {
            is Result.Success -> {
                Log.e(TAG, "DownloadLink ${result.data}")
                downloadSubtitle(model, result.data, i)
            }
            is Result.Error -> {
                model.isDownload = false
                adapter.notifyItemChanged(i)

                AlertNoIconDialog.Companion.Builder(context).apply {
                    setTitle(getString(R.string.error))
                    setMessage("Failed to download subtitles due to: ${result.ex.message}")
                    setPositiveButton(getString(R.string.alright)) { dismiss() }
                }.show()
            }
            else -> {}
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        Log.e(TAG, "onDismiss() called")
        if (::subtitleFetch.isInitialized && !subtitleFetch.isDisposed) subtitleFetch.dispose()
        super.onDismiss(dialog)
    }

    private fun downloadSubtitle(model: Subtitle, downloadLink: String, i: Int) {

        val tempLocation = File(context?.externalCacheDir, imdb_code)
        if (!tempLocation.exists()) tempLocation.mkdirs()

        val temporarySaveZipLocation = File(tempLocation, "${model.text}.zip")
        if (temporarySaveZipLocation.exists()) temporarySaveZipLocation.delete()

        /** Registering broadcast receiver for listening afterDownload complete event */

        val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (temporarySaveZipLocation.exists()) {

                    val nameOfDownload = model.getDownloadFileName()

                    SUBTITLE_LOCATION.mkdirs()

                    temporarySaveZipLocation.unzip(tempLocation)

                    if (temporarySaveZipLocation.exists()) temporarySaveZipLocation.delete()

                    if (tempLocation.listFiles()?.isNotEmpty() == true) {
                        tempLocation.listFiles()[0].renameTo(
                            File(
                                SUBTITLE_LOCATION,
                                nameOfDownload
                            )
                        )

                    } else throw Throwable("File does not exist")

                    Toast.makeText(context, "${model.text} download complete!", Toast.LENGTH_SHORT)
                        .show()
                    model.isDownload = false
                    adapter.notifyItemChanged(i)

                    FileUtils.deleteRecursive(tempLocation)

                    context?.unregisterReceiver(this)
                } else {
                    Toasty.error(context!!, "Failed to download subtitles!").show()
                }
            }
        }

        requireContext().registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        /*** Making a request to download manager */

        val request = DownloadManager.Request(Uri.parse(downloadLink))
        request.setTitle("Downloading Subtitle")
        request.setDescription(model.text)
        request.setDestinationUri(Uri.fromFile(temporarySaveZipLocation))

        val manager = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }


    class SubtitleAdapter(
        val context: Context,
        private val flagUtils: FlagUtils,
        var models: List<Subtitle>,
        val listener: AdapterOnSingleClick<Subtitle>
    ) : RecyclerView.Adapter<SubtitleAdapter.SubtitleHolder>() {

        override fun onBindViewHolder(holder: SubtitleHolder, i: Int) {
            val model = models[i]
            GlideApp.with(context.applicationContext).load(flagUtils.getFlagUrl(model.country))
                .into(holder.flagImage)

            holder.title.text = model.text
            holder.subText.text = "${model.country} ${AppUtils.getBulletSymbol()} ${model.uploader}"

            /** Setting holder likes */

            when {
                model.likes > 0 -> {
                    holder.itemLikes.text = model.likes.toString()
                    holder.itemLikes.setBackgroundColor(context.colorFrom(R.color.green))
                    holder.itemLikes.visibility = View.VISIBLE
                }
                model.likes < 0 -> {
                    holder.itemLikes.text = "${model.likes}"
                    holder.itemLikes.setBackgroundColor(context.colorFrom(R.color.red))
                    holder.itemLikes.visibility = View.VISIBLE
                }
                else -> {
                    holder.itemLikes.visibility = View.GONE
                }
            }

            if (model.isDownload) {
                holder.progressBar.visibility = View.VISIBLE
                holder.itemLikes.visibility = View.GONE
            } else {
                holder.progressBar.visibility = View.GONE
                holder.itemLikes.visibility = View.VISIBLE
            }

            if (SubtitleHelper.doesSubtitleExist(model.getDownloadFileName())) {
                holder.title.setTextColor(context.colorFrom(R.color.premium))
            } else {
                holder.title.setTextColor(context.getColorAttr(R.attr.colorText))
            }

            holder.mainCard.setOnClickListener {
                listener.invoke(model, i)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleHolder {
            return SubtitleHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_subtitles, parent, false
                )
            )
        }

        override fun getItemCount(): Int {
            return models.size
        }

        class SubtitleHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mainCard = view.mainCard
            val flagImage = view.item_flag
            val title = view.item_text
            val subText = view.item_subText
            val itemLikes = view.item_likes
            val progressBar = view.item_progressBar
        }
    }
}
