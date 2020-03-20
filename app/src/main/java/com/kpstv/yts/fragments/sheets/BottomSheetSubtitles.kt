package com.kpstv.yts.fragments.sheets

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.yts.AppInterface.Companion.SUBTITLE_LOCATION
import com.kpstv.yts.AppInterface.Companion.YIFY_BASE_URL
import com.kpstv.yts.R
import com.kpstv.yts.dialogs.AlertNoIconDialog
import com.kpstv.yts.interfaces.listener.SingleClickListener
import com.kpstv.yts.models.Subtitle
import com.kpstv.yts.utils.*
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottom_sheet_subtitles.*
import kotlinx.android.synthetic.main.bottom_sheet_subtitles.view.*
import kotlinx.android.synthetic.main.item_subtitles.view.*
import org.jsoup.Jsoup
import java.io.File
import java.util.concurrent.Callable

@SuppressLint("SetTextI18n")
class BottomSheetSubtitles : BottomSheetDialogFragment() {

    private val TAG = "BottomSheetSubtiles"
    private lateinit var v: View
    private lateinit var adapter: SubtitleAdapter
    private lateinit var subtitleFetch: Disposable
    private lateinit var imdb_code: String
    var hasEnglish = false
    var hasSpanish = false
    var hasArabic = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.bottom_sheet_subtitles, container, false)

        v.recyclerView_subtitles.layoutManager = LinearLayoutManager(context)
        v.mainLayout.visibility = View.GONE

        /** Fetching subtitles */

        imdb_code = tag as String

        fetchSubtitles()

        return v
    }


    private var subtitleModels = ArrayList<Subtitle>()
    private fun fetchSubtitles() {

        subtitleFetch = Observable.fromCallable<String> {
            return@fromCallable Jsoup.connect("${YIFY_BASE_URL}/movie-imdb/${imdb_code}").get().html()
                // btn-icon download-subtitle --> Own text
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                progressBar.visibility = View.GONE
                val subtitles = Jsoup.parse(it).getElementsByClass("high-rating")
                subtitleModels.clear()
                for(element in subtitles) {
                    val a = element.select("a")[0]

                    val country = element.getElementsByClass("sub-lang")[0].ownText()
                    if (country == "Arabic") hasArabic = true
                    if (country == "English") hasEnglish = true
                    if (country == "Spanish") hasSpanish = true

                    subtitleModels.add(
                        Subtitle(
                            country,
                            a.ownText(),
                            element.getElementsByClass("label")[0].ownText().toInt(),
                            element.getElementsByClass("uploader-cell")[0].ownText(),
                            a.attr("href").toString()
                        ))
                }
                if (subtitleModels.size>0) {
                    adapter =
                        SubtitleAdapter(
                            context as Context,
                            subtitleModels
                        )
                    adapter.setOnSingleClickListener(object:
                        SingleClickListener {
                        override fun onClick(obj: Any, i: Int) {
                            parseSubtitle(obj as Subtitle,i)
                        }
                    })
                    recyclerView_subtitles.setHasFixedSize(true)
                    recyclerView_subtitles.adapter = adapter

                    v.mainLayout.visibility = View.VISIBLE

                    handleFilter()

                }else {
                   Toasty.error(context!!, "No subtitles found").show()
                    dismiss()
                }


            }, {

                AlertNoIconDialog.Companion.Builder(context).apply {
                    setTitle("Error")
                    setMessage("Failed to fetch subtitles due to: ${it.message}")
                    setPositiveButton(getString(R.string.yes),object: AlertNoIconDialog.DialogListener{
                        override fun onClick() {
                            dismiss()
                        }
                    })
                }.show()
            })
    }

    private fun handleFilter() {

        if (subtitleModels.size < 10) {
            v.filterLayout.visibility = View.GONE
            v.separatorView.visibility = View.GONE
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

    private fun parseSubtitle(model: Subtitle, i: Int) {
        /** Update view to show progressbar */
        model.isDownload = true
        adapter.notifyItemChanged(i)

        /** Async task to fetch download link */

        subtitleFetch = Observable.fromCallable(Callable<String>{
           return@Callable Jsoup.connect("${YIFY_BASE_URL}${model.fetchEndpoint}").get().html()
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                /** Downloading subtitles using DownloadManager with a broadcast */

                val downloadLink = Jsoup.parse(it).getElementsByClass("download-subtitle")[0]
                    .attr("href").toString();

                Log.e(TAG, "DownloadLink $downloadLink")

                downloadSubtitle(model,downloadLink,i)

            },{
                model.isDownload = false
                adapter.notifyItemChanged(i)

                it.printStackTrace()
                AlertNoIconDialog.Companion.Builder(context).apply {
                    setTitle("Error")
                    setMessage("Failed to download subtitles due to: ${it.message}")
                    setPositiveButton(getString(R.string.alright),object: AlertNoIconDialog.DialogListener{
                        override fun onClick() {
                            dismiss()
                        }
                    })
                }.show()
            })

    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy() called")
        if (::subtitleFetch.isInitialized &&!subtitleFetch.isDisposed) subtitleFetch.dispose()
        subtitleModels.clear()
        super.onDestroy()
    }

    private fun downloadSubtitle(model: Subtitle, downloadLink: String, i: Int) {

       // val saveLocation = File(SUBTITLE_LOCATION,"${model.text}-${model.country}.srt")
        val tempLocation = File(context?.externalCacheDir,imdb_code)
        if (!tempLocation.exists()) tempLocation.mkdirs()

        val temporarySaveLocation = File(tempLocation,"${model.text}.zip")
        if (temporarySaveLocation.exists()) temporarySaveLocation.delete()

        /** Registering broadcast receiver for listening afterDownload complete event */

        val onComplete: BroadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (temporarySaveLocation.exists()) {

                    val nameofDownload =  "${model.text}${downloadLink.substring(downloadLink.indexOf("-"))
                        .replace(".zip","")}.srt"

                    SUBTITLE_LOCATION.mkdirs()

                    ZipUtility.extract(temporarySaveLocation, tempLocation)

                    if (temporarySaveLocation.exists()) temporarySaveLocation.delete()

                    if (tempLocation.listFiles().isNotEmpty()) {
                        tempLocation.listFiles()[0].renameTo(File(SUBTITLE_LOCATION,nameofDownload))

                    }else throw Throwable("File does not exist")

                    Toast.makeText(context, "${model.text} download complete!", Toast.LENGTH_SHORT).show()
                    model.isDownload = false
                    adapter.notifyItemChanged(i)

                    AppUtils.deleteRecursive(tempLocation)

                    context?.unregisterReceiver(this)
                }else {
                    Toasty.error(context!!, "Failed to download subtitles!").show()
                }
            }
        }

        context?.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        /*** Making a request to download manager */

        val request = DownloadManager.Request(Uri.parse(downloadLink))
        request.setTitle("Downloading Subtitle")
        request.setDescription(model.text)
        request.setDestinationUri(Uri.fromFile(temporarySaveLocation))

        val manager = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }



    class SubtitleAdapter(val context: Context, var models: List<Subtitle>) : RecyclerView.Adapter<SubtitleAdapter.SubtitleHolder>() {

        private lateinit var listener: SingleClickListener

        override fun onBindViewHolder(holder: SubtitleHolder, i: Int) {
            val model = models[i]
            Glide.with(context.applicationContext).load(FlagUtils.getFlagUrl(model.country)).into(holder.flagImage)

            holder.title.text = model.text
            holder.subText.text = "${model.country} ${AppUtils.getBulletSymbol()} ${model.uploader}"

            /** Setting holder likes */

            when {
                model.likes > 0 -> {
                    holder.itemLikes.text = model.likes.toString()
                    holder.itemLikes.setBackgroundColor(ContextCompat.getColor(context,R.color.green))
                    holder.itemLikes.visibility = View.VISIBLE
                }
                model.likes < 0 -> {
                    holder.itemLikes.text = "${model.likes}"
                    holder.itemLikes.setBackgroundColor(ContextCompat.getColor(context,R.color.red))
                    holder.itemLikes.visibility = View.VISIBLE
                }
                else -> {
                    holder.itemLikes.visibility = View.GONE
                }
            }

            if (model.isDownload) {
                holder.progressBar.visibility = View.VISIBLE
                holder.itemLikes.visibility = View.GONE
            }else {
                holder.progressBar.visibility = View.GONE
                holder.itemLikes.visibility = View.VISIBLE
            }

            holder.mainCard.setOnClickListener {
                listener.onClick(model,i)
            }
        }

        fun setOnSingleClickListener(listener: SingleClickListener) {
            this.listener = listener
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
