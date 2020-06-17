package com.kpstv.yts.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.yts.models.Torrent
import com.kpstv.yts.models.TorrentJob

class ServiceManager {

    private val _pendingJobs = MutableLiveData<ArrayList<Torrent>>()
    private val _torrentJob = MutableLiveData<TorrentJob>()

    val pendingJobs: LiveData<ArrayList<Torrent>>
        get() = _pendingJobs

    val torrentJob: LiveData<TorrentJob>
        get() = _torrentJob

    fun getTorrentJob() =
        _torrentJob.value

    fun removeTorrentJob() =
        _torrentJob.postValue(null)

    fun postTorrentJob(config: TorrentJob) =
        _torrentJob.postValue(config)

    fun getPendingJobs() =
        pendingJobs.value ?: ArrayList()

    fun addPendingJobs(config: Torrent) =
        _pendingJobs.postValue(getPendingJobs().apply { add(config) })

    fun removePendingJob(config: Torrent) =
        _pendingJobs.postValue(getPendingJobs().apply { remove(config) })

    fun removePendingJob(position: Int) =
        _pendingJobs.postValue(getPendingJobs().apply { removeAt(position) })

    fun clearPendingJobs() =
        _pendingJobs.postValue(ArrayList())

}