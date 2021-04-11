package com.kpstv.navigation

/**
 * The host must implement this interface to propagate Navigator
 * to the child fragments. This ensures the correct behavior of
 * back press.
 */
interface NavigatorTransmitter {
    fun getNavigator(): Navigator
}