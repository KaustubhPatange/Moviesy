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
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.bottom_sheet_subtitles.*
import kotlinx.android.synthetic.main.item_subtitles.view.*
import java.io.File
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

    private val currentDownloads = hashMapOf<Long, Int>() // Download Id to subtitle index from subtitleModels.

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewSubtitles.layoutManager = LinearLayoutManager(context)
        binding.subtitleMainLayout.hide()
        imdb_code = tag as String

        /** Fetching subtitles */

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            fetchSubtitles()
        }

        requireContext().registerReceiver(downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private suspend fun fetchSubtitles() {
        val result = ytsParser.fetchSubtitles(imdb_code)
        when(result) {
            is SubtitleResult.Success -> {
                progressBar.visibility = View.GONE

                if (result.list.isNotEmpty()) {
                    subtitleModels = result.list

                    adapter = SubtitleAdapter(
                        context = requireContext(),
                        flagUtils = flagUtils,
                        models = subtitleModels,
                        listener = { subtitle, i ->
                            /** @Admob Show ad first and then download subtitles */
                            interstitialAdHelper.showAd(viewLifecycleOwner) {
                                parseSubtitle(subtitle, i)
                            }
                        },
                        longListener = { subtitle, _ ->
                            AppUtils.shareUrl(requireActivity(), subtitle.fetchEndpoint)
                        }
                    )

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

    private fun downloadSubtitle(model: Subtitle, downloadLink: String, index: Int) {
        val tempLocation = File(context?.externalCacheDir, imdb_code)
        if (!tempLocation.exists()) tempLocation.mkdirs()

        val temporarySaveZipLocation = File(tempLocation, "${model.getMD5Text()}.zip")
        if (temporarySaveZipLocation.exists()) temporarySaveZipLocation.delete()

        /*** Making a request to download manager */

        val request = DownloadManager.Request(Uri.parse(downloadLink))
        request.setTitle("Downloading Subtitle")
        request.setDescription(model.text)
        request.setDestinationUri(Uri.fromFile(temporarySaveZipLocation))

        val manager = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val requestId = manager.enqueue(request)

        currentDownloads[requestId] = index
    }

    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (this@BottomSheetSubtitles.isViewDestroying()) return
            val requestId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1L
            if (requestId == -1L) return

            val index = currentDownloads.remove(requestId) ?: return
            val model = subtitleModels[index]

            val tempLocation = File(context?.externalCacheDir, imdb_code)
            val temporarySaveZipLocation = File(tempLocation, "${model.getMD5Text()}.zip")
            if (temporarySaveZipLocation.exists()) {
                val nameOfDownload = model.getDownloadFileName()

                SUBTITLE_LOCATION.mkdirs()

                temporarySaveZipLocation.unzip(tempLocation)

                if (temporarySaveZipLocation.exists()) temporarySaveZipLocation.delete()

                if (tempLocation.listFiles()?.isNotEmpty() == true) {
                    tempLocation.listFiles()?.get(0)?.renameTo(
                        File(
                            SUBTITLE_LOCATION,
                            nameOfDownload
                        )
                    )

                } else throw Throwable("File does not exist")

                Toast.makeText(context, "${model.getDownloadFileName()} download complete!", Toast.LENGTH_SHORT)
                    .show()
                model.isDownload = false
                adapter.notifyItemChanged(index)

                FileUtils.deleteRecursive(tempLocation)
            } else {
                Toasty.error(context!!, "Failed to download subtitles!").show()
            }
        }
    }

    override fun onDestroyView() {
        requireContext().unregisterReceiver(downloadCompleteReceiver)
        if (currentDownloads.isNotEmpty()) {
            val manager = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            manager.remove(*currentDownloads.keys.toLongArray())
        }
        super.onDestroyView()
    }

    class SubtitleAdapter(
        val context: Context,
        private val flagUtils: FlagUtils,
        var models: List<Subtitle>,
        val listener: AdapterOnSingleClick<Subtitle>,
        val longListener: AdapterOnSingleClick<Subtitle>,
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
                holder.itemLikes.visibility = View.INVISIBLE
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

            holder.mainCard.setOnLongClickListener call@{
                longListener.invoke(model, i)
                return@call true
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
