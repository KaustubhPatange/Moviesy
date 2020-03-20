package com.kpstv.yts.interfaces.listener

interface ObservableListener {
    fun onSuccess(obj: Any)
    fun onError(it: Throwable)
}