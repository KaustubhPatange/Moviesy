package com.kpstv.yts.activities

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.danimahardhika.cafebar.CafeBar
import com.kpstv.yts.AppInterface.Companion.EMPTY_QUEUE
import com.kpstv.yts.AppInterface.Companion.MODEL_UPDATE
import com.kpstv.yts.AppInterface.Companion.PENDING_JOB_UPDATE
import com.kpstv.yts.AppInterface.Companion.REMOVE_CURRENT_JOB
import com.kpstv.yts.AppInterface.Companion.formatDownloadSpeed
import com.kpstv.yts.R
import com.kpstv.yts.adapters.JobQueueAdapter
import com.kpstv.yts.dialogs.AlertNoIconDialog
import com.kpstv.yts.models.Torrent
import com.kpstv.yts.models.TorrentJob
import com.kpstv.yts.utils.AppUtils
import com.kpstv.yts.utils.AppUtils.Companion.getMagnetUrl
import kotlinx.android.synthetic.main.activity_download.*
import kotlinx.android.synthetic.main.item_torrent_download.*

@SuppressLint("SetTextI18n")
class DownloadActivity : AppCompatActivity() {

    private val TAG = "DownloadActivity"
    private lateinit var adapter: JobQueueAdapter
    private lateinit var currentModel: TorrentJob

    private val SHOW_LOG_FROM_THIS_CLASS=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        setSupportActionBar(toolbar)

        title = "Download Queue"

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        emptyQueue()

        recyclerView_download.layoutManager = LinearLayoutManager(this)

        handleReceiver(intent?.action,intent)

        registerLocalBroadcast()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun registerLocalBroadcast() {
        val filter = IntentFilter(MODEL_UPDATE)
        filter.addAction(PENDING_JOB_UPDATE)
        filter.addAction(EMPTY_QUEUE)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter)
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            handleReceiver(intent?.action,intent)
        }
    }

    private fun handleReceiver(action: String?,intent: Intent?) {
        when (action) {
            MODEL_UPDATE -> {

                DA_LOG("--- MODAL_UPDATE ---")

                val model: TorrentJob = intent?.getSerializableExtra("model") as TorrentJob
                if (!::currentModel.isInitialized) {
                    updateCurrentModel(intent,model)
                }else if (currentModel.title == model.title) {
                    updateCurrentModel(intent,model,true)
                }else updateCurrentModel(intent,model)
                layout_jobCurrentQueue.visibility = View.VISIBLE
            }
            PENDING_JOB_UPDATE -> {

                DA_LOG("--- PENDING_JOB_UPDATE ---")

                pendingJobUpdate(intent)
            }
            EMPTY_QUEUE -> {

                DA_LOG("--- EMPTY_QUEUE ---")

                emptyQueue()
            }
        }
    }


    private fun updateCurrentModel(intent: Intent?,torrentJob: TorrentJob, justUpdateStatus: Boolean=false) {
        layout_JobEmptyQueue.visibility = View.GONE

        item_status.text = torrentJob.status
        item_progress.text = "${torrentJob.progress}%"
        item_seeds_peers.text = "${torrentJob.seeds}/${torrentJob.peers}"

        /** Calculate total size based on progress */

        var totalSize = torrentJob.totalSize;
        totalSize ?: kotlin.run { totalSize = 0 }

        val currentSize = (torrentJob.progress.toLong() * (torrentJob.totalSize as Long))/(100).toLong()

        item_current_size.text = AppUtils.getSizePretty(currentSize)

        item_download_speed.text = formatDownloadSpeed(torrentJob.downloadSpeed)
        item_progressBar.progress = torrentJob.progress
        item_total_size.text = AppUtils.getSizePretty(torrentJob.totalSize)

        pendingJobUpdate(intent)

        if (!justUpdateStatus) {
            currentModel = torrentJob

            Glide.with(applicationContext).asBitmap().load(currentModel.bannerUrl).into(
               object: CustomTarget<Bitmap>(){
                   override fun onLoadCleared(placeholder: Drawable?) {
                   }

                   override fun onResourceReady(resource: Bitmap,transition: Transition<in Bitmap>?) {
                       item_image.setImageBitmap(resource)
                   }
               }
            )

            item_title.text = currentModel.title

            item_more_imageView.setOnClickListener {
                val menu = PopupMenu(this,item_more_imageView)
                menu.inflate(R.menu.item_torrrent_menu)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_copy_magnet -> {
                            val service = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            service.setPrimaryClip(ClipData.newPlainText(
                                "Magnet Url",
                                getMagnetUrl(
                                    currentModel.magnetHash,
                                    currentModel.title.toLowerCase().replace(" ","-")
                                    )
                                )
                            )
                            CafeBar.Builder(this@DownloadActivity)
                                .floating(true)
                                .content("Copied to clipboard")
                                .duration(CafeBar.Duration.SHORT)
                                .show()
                        }
                        R.id.action_cancel -> {
                            val i = Intent(REMOVE_CURRENT_JOB)
                            i.putExtra("model",currentModel)

                            AlertNoIconDialog.Companion.Builder(this).apply {
                                setTitle(getString(R.string.ask_title))
                                setMessage(getString(R.string.ask_delete))
                                setPositiveButton(getString(R.string.yes), object: AlertNoIconDialog.DialogListener{
                                    override fun onClick() {
                                        i.putExtra("deleteFile",true)
                                        actionCancel(i)
                                    }
                                })
                                setNegativeButton(getString(R.string.no), object: AlertNoIconDialog.DialogListener{
                                    override fun onClick() {
                                        i.putExtra("deleteFile",false)
                                        actionCancel(i)
                                    }
                                })
                            }.show()
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                menu.show()
            }
        }
    }

    private fun pendingJobUpdate(intent: Intent?) {

        val jobs = intent?.getSerializableExtra("models") ?: return

        val models: ArrayList<Torrent> = jobs as ArrayList<Torrent>

        DA_LOG("### JOB QUEUE: ${models.size}")

        if (models.size>0) {

            if (::adapter.isInitialized && adapter.itemCount == models.size) return

            adapter = JobQueueAdapter(this@DownloadActivity,models)

            adapter.setCloseClickListener(object: JobQueueAdapter.CloseClickListener{
                override fun onClick(model: Torrent, pos: Int) {
                    models.removeAt(pos)
                    adapter.notifyItemRemoved(pos)

                  /*  // TODO: Check if you need this... AHH.. WE DON't NEED it
                    val i = Intent(REMOVE_JOB)
                    i.putExtra("model",model)
                    LocalBroadcastManager.getInstance(this@DownloadActivity).sendBroadcast(intent)*/
                }
            })

            recyclerView_download.adapter = adapter
            layout_jobPendingQueue.visibility = View.VISIBLE
        } else layout_jobPendingQueue.visibility = View.GONE
    }

    private fun actionCancel(i: Intent) {
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(i)
        layout_jobCurrentQueue.visibility = View.GONE
        layout_JobEmptyQueue.visibility = View.VISIBLE
    }

    private fun emptyQueue() {

        DA_LOG("## EMPTYING QUEUE")

        layout_JobEmptyQueue.visibility = View.VISIBLE
        layout_jobPendingQueue.visibility = View.GONE
        layout_jobCurrentQueue.visibility = View.GONE
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy()
    }

    private fun DA_LOG(message: String) {
        if (SHOW_LOG_FROM_THIS_CLASS)
            Log.e(TAG,message)
    }
}
