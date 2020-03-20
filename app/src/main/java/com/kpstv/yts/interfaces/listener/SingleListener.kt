package com.kpstv.yts.interfaces.listener

interface SingleListener {
    fun onClick(click: (Any, Int) -> Unit)
}