package com.kpstv.yts.ui.navigation

/**
 * Parent context must implement this interface to propagate Navigator
 * to child fragments.
 *
 * @throws NotImplementedError
 */
interface NavigatorTransmitter {
    fun getNavigator(): Navigator
}