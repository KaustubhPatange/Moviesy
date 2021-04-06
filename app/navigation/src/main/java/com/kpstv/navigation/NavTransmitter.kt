package com.kpstv.navigation

/**
 * Parent context must implement this interface to propagate Navigator
 * to child fragments.
 *
 * @throws NotImplementedError
 */
interface NavigatorTransmitter {
    fun getNavigator(): Navigator
}