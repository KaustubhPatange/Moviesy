package com.kpstv.yts.vpn

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.VpnService
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpstv.yts.R
import com.kpstv.yts.defaultPreference
import com.kpstv.yts.vpn.views.HelperView
import com.kpstv.yts.vpn.views.HoverContainer
import com.kpstv.yts.vpn.views.HoverContainerController
import de.blinkt.openvpn.DisconnectVPNActivity
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlin.reflect.KClass

class VPNHelper(
    private val activity: FragmentActivity,
    private val onHoverClick: (KClass<out Fragment>) -> Unit
) {
    private val vpnViewModel by activity.viewModels<VPNViewModel>()
    private val hoverController: HoverContainerController

    private var isVpnStarted: Boolean = false
    private var currentServer: VpnConfiguration? = null
    private var currentConfig: String? = null

    private val preferences by activity.defaultPreference()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            stopVpn()
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver)
            super.onDestroy(owner)
        }
    }

    private val vpnResultContract = activity.registerForActivityResult(VPNServiceContract()) { ok ->
        if (ok) { startVpn() }
    }

    init {
        activity.lifecycle.addObserver(lifecycleObserver)
        hoverController = HoverContainer.attachToActivity(activity).apply {
            addOnHoverClickListener {
                onHoverClick.invoke(VPNDialogFragment::class)
            }
            setOnHoverDismissed {
                vpnViewModel.toggleHover()
            }
            setDrawable(R.drawable.open_vpn)
            setHoverBorderColor(vpnViewModel.connectionStatus.value.color)
            setHoverBackgroundColor(Color.WHITE)
            removeHover()
        }
    }

    fun initializeAndObserve() = with(activity) {
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
        // observe show and remove hover
        lifecycleScope.launchWhenStarted {
            vpnViewModel.showHover.collect { toShow ->
                if (toShow && hoverController.isHoverClosed()) {
                    hoverController.resetHover()
                } else {
                    hoverController.removeHover()
                }

                // show helper view
                if (toShow && !preferences.isVpnHelpShown()) {
                    delay(2000) // a delay to let view settle on the screen
                    showHelperView()
                }
            }
        }
        // observe connection status
        lifecycleScope.launchWhenCreated {
            vpnViewModel.connectionStatus.collect { state ->
                if (state !is VpnConnectionStatus.Unknown)
                    hoverController.setHoverBorderColor(state.color)
                if (state is VpnConnectionStatus.StopVpn) {

                    activity.startActivity(Intent(activity, DisconnectVPNActivity::class.java))
                }
                if (state is VpnConnectionStatus.Disconnected) {
                    OpenVPNService.setDefaultStatus()
                }
                if (state is VpnConnectionStatus.Downloaded) {
                    // new server
                    if (isVpnStarted) stopVpn()
                    prepareVpn(state.server, state.config)
                }
            }
        }
        // observe vpn state
        lifecycleScope.launchWhenStarted {
            vpnViewModel.vpnHelperState.collect { state ->
                if (state is VpnHelperState.DisableVpn) {
                    preferences.setVpnHelpShown(false)
                }
            }
        }

        vpnViewModel.initialize()
    }

    private fun stopVpn(): Boolean {
        try {
            OpenVPNThread.stop()
            isVpnStarted = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun prepareVpn(server: VpnConfiguration, config: String) {
        this.currentServer = server
        this.currentConfig = config
        if (!isVpnStarted) {
            val intent = VpnService.prepare(activity)
            if (intent != null) {
                vpnResultContract.launch(intent)
            } else startVpn()
        } else if (stopVpn()) {
            Toasty.info(activity, activity.getString(R.string.vpn_disconnect)).show()
        }
    }

    private fun startVpn() {
        try {
            val server = currentServer ?: throw Exception("Error: Server is null")
            val config = currentConfig ?: throw Exception("Error: Server config is null")
            OpenVpnApi.startVpn(activity, config, server.country, "vpn", "vpn")
            isVpnStarted = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toasty.error(activity, activity.getString(R.string.vpn_error)).show()
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) return
            vpnViewModel.dispatchConnectionState(intent.getStringExtra("state") ?: "")

            val duration = intent.getStringExtra("duration") ?: "00:00:00"
            val lastPacketReceive = intent.getStringExtra("lastPacketReceive") ?: "0"
            val bytesIn = intent.getStringExtra("byteIn") ?: " "
            val bytesOut = intent.getStringExtra("byteOut") ?: " "

            val detail = VPNViewModel.ConnectionDetail(
                duration = duration,
                lastPacketReceive = lastPacketReceive,
                bytesIn = bytesIn,
                bytesOut = bytesOut
            )
            vpnViewModel.dispatchConnectionDetail(detail)
        }
    }

    private var helperView: HelperView? = null
    private fun showHelperView() {
        if (helperView != null) return
        val location = hoverController.getHoverLocation()
        helperView = HelperView.showForTarget(
             activity = activity,
             rect = location,
             title = activity.getString(R.string.vpn_help_title),
             subText = activity.getString(R.string.vpn_help_subtext)
        ).apply {
            setOnTargetTouchListener {
                onHoverClick.invoke(VPNDialogFragment::class)
            }
            setOnDismissListener {
                helperView = null
                preferences.setVpnHelpShown(true)
            }
        }
    }

    class VPNServiceContract : ActivityResultContract<Intent, Boolean>() {
        override fun createIntent(context: Context, input: Intent): Intent {
            return input
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }
}

sealed class VpnConnectionStatus(open val color: Int) {
    data class Downloading(override val color: Int = Color.CYAN) : VpnConnectionStatus(color)
    data class Downloaded(override val color: Int = Color.YELLOW, val server: VpnConfiguration, val config: String) : VpnConnectionStatus(color)
    data class LoadingConfiguration(override val color: Int = Color.CYAN) : VpnConnectionStatus(color)
    data class Unknown(override val color: Int = Color.RED) : VpnConnectionStatus(color)
    data class Disconnected(override val color: Int = Color.RED) : VpnConnectionStatus(color)
    data class Connected(override val color: Int = Color.GREEN) : VpnConnectionStatus(color)
    data class Waiting(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
    data class Authenticating(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
    data class Reconnecting(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
    data class GetConfig(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
    data class NoNetwork(override val color: Int = Color.RED) : VpnConnectionStatus(color)
    data class Invalid(override val color: Int = Color.RED) : VpnConnectionStatus(color)
    data class StopVpn(override val color: Int = Color.TRANSPARENT) : VpnConnectionStatus(color)
}

sealed class VpnDialogUIState {
    object Loading : VpnDialogUIState()
    data class Detail(val vpnConfigurations: List<VpnConfiguration>) : VpnDialogUIState()
    data class Connected(val ip: String) : VpnDialogUIState()
}

sealed class VpnHelperState {
    object None : VpnHelperState()
    object DisableVpn : VpnHelperState()
}