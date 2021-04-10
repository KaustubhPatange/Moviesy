package com.kpstv.navigation

/**
 * The host must implement this interface to propagate Navigator
 * to child fragments.
 */
interface NavigatorTransmitter {
    fun getNavigator(): Navigator
}