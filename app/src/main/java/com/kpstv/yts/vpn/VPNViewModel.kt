package com.kpstv.yts.vpn

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.yts.AppInterface
import com.kpstv.yts.data.converters.AppDatabaseConverter
import com.kpstv.yts.extensions.utils.RetrofitUtils
import com.kpstv.yts.vpn.db.VPNRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject

class VPNViewModel @ViewModelInject constructor(
    private val repository: VPNRepository,
    private val retrofitUtils: RetrofitUtils
): ViewModel() {
    private var country: String = ""
    private var ip: String = ""

    private val vpnConfigurations: ArrayList<VpnConfiguration> = arrayListOf()

    private val _showHover = MutableStateFlow(false)
    val showHover: StateFlow<Boolean> = _showHover

    private val _vpnHelperStateFlow : MutableStateFlow<VpnHelperState> = MutableStateFlow(VpnHelperState.None)
    val vpnHelperState: StateFlow<VpnHelperState> = _vpnHelperStateFlow.asStateFlow()

    private val _connectionStatus: MutableStateFlow<VpnConnectionStatus> =
        MutableStateFlow(VpnConnectionStatus.LoadingConfiguration())
    val connectionStatus: StateFlow<VpnConnectionStatus> = _connectionStatus

    private val _connectionDetails = MutableStateFlow(ConnectionDetail())
    val connectionDetail: StateFlow<ConnectionDetail> = _connectionDetails

    private val _dialogUiState: MutableStateFlow<VpnDialogUIState> =
        MutableStateFlow(VpnDialogUIState.Loading)
    val dialogUiState: StateFlow<VpnDialogUIState> = _dialogUiState

    var currentServer: VpnConfiguration? = null

    init {
        viewModelScope.launch {
            connectionStatus.collect { state ->
                when(state) {
                    is VpnConnectionStatus.Unknown -> { }
                    is VpnConnectionStatus.StopVpn -> { }
                    is VpnConnectionStatus.Disconnected -> _dialogUiState.emit(VpnDialogUIState.Detail(vpnConfigurations))
                    is VpnConnectionStatus.Connected -> _dialogUiState.emit(VpnDialogUIState.Connected(currentServer?.ip ?: ip))
                    else -> _dialogUiState.emit(VpnDialogUIState.Loading)
                }
            }
        }
    }

    fun initialize() {
        CoroutineScope(viewModelScope.coroutineContext + Dispatchers.IO).launch {
            initializeConfigs()
            initializeVPNConfigs()
        }
    }

    fun connect(server: VpnConfiguration) {
        currentServer = server
        viewModelScope.launch {
            _connectionStatus.emit(VpnConnectionStatus.Downloading())
            _connectionStatus.emit(VpnConnectionStatus.Downloaded(server = server, config = server.config))
            _dialogUiState.emit(VpnDialogUIState.Loading)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _connectionStatus.emit(VpnConnectionStatus.StopVpn())
        }
    }

    fun resetDialogState() {
        viewModelScope.launch {
            if (vpnConfigurations.isNotEmpty())
                _dialogUiState.emit(VpnDialogUIState.Detail(vpnConfigurations))
            else
                _dialogUiState.emit(VpnDialogUIState.Loading)
        }
    }

    fun dispatchConnectionState(state: String) {
        // map from string to state
        val connectionState = when(state) {
            "DISCONNECTED" -> VpnConnectionStatus.Disconnected()
            "CONNECTED" -> VpnConnectionStatus.Connected()
            "WAIT" -> VpnConnectionStatus.Waiting()
            "AUTH" -> VpnConnectionStatus.Authenticating()
            "RECONNECTING" -> VpnConnectionStatus.Reconnecting()
            "NONETWORK" -> VpnConnectionStatus.NoNetwork()
            "GET_CONFIG" -> VpnConnectionStatus.GetConfig()
            else -> VpnConnectionStatus.Unknown()
        }
        _connectionStatus.tryEmit(connectionState)
    }

    fun toggleHover() {
        _showHover.tryEmit(!_showHover.value)
    }

    fun dispatchConnectionDetail(detail: ConnectionDetail) {
        _connectionDetails.tryEmit(detail)
    }

    private suspend fun initializeConfigs() {
        try {
            val response = retrofitUtils.makeHttpCallAsync("http://ip-api.com/json").getOrNull() ?: return
            if (response.isSuccessful) {
                val body = response.body?.string() ?: "{}"
                response.close() // close
                val json = JSONObject(body)
                if (json.has("query")) {
                    ip = json.getString("query")
                }
                if (json.has("countryCode")) {
                    country = json.getString("countryCode")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private suspend fun initializeVPNConfigs() {
        val appDatabaseResponse = retrofitUtils.makeHttpCallAsync(AppInterface.APP_DATABASE_URL).getOrNull() ?: return
        if (appDatabaseResponse.isSuccessful) {
            val json = appDatabaseResponse.body?.string()
            appDatabaseResponse.close() // Always close
            AppDatabaseConverter.toAppDatabaseFromString(json)?.let { data ->
                if (data.vpnAffectedCountries.contains(country)) {
                    _showHover.emit(true)
                } else {
                    _vpnHelperStateFlow.tryEmit(VpnHelperState.DisableVpn)
                }
            }
        }

        val configurations = repository.fetch()
        _connectionStatus.emit(VpnConnectionStatus.Disconnected())
        vpnConfigurations.clear()
        vpnConfigurations.addAll(configurations)
        _dialogUiState.emit(VpnDialogUIState.Detail(configurations))
    }

    data class ConnectionDetail(
        val duration: String = "00:00:00",
        val lastPacketReceive: String = "0",
        val bytesIn: String = " ",
        val bytesOut: String = " "
    )
}