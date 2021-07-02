package com.kpstv.yts.vpn

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.yts.AppInterface
import com.kpstv.yts.data.converters.AppDatabaseConverter
import com.kpstv.yts.data.models.AppDatabase
import com.kpstv.yts.extensions.utils.RetrofitUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject

class VPNViewModel @ViewModelInject constructor(
    private val retrofitUtils: RetrofitUtils
): ViewModel() {
    private var country: String = ""
    private var ip: String = ""
    private val vpnConfigurations: ArrayList<AppDatabase.VpnConfiguration> = arrayListOf()

    private val _showHover = MutableStateFlow(false)
    val showHover: StateFlow<Boolean> = _showHover

    private val _connectionStatus: MutableStateFlow<VpnConnectionStatus> =
        MutableStateFlow(VpnConnectionStatus.Unknown())
    val connectionStatus: StateFlow<VpnConnectionStatus> = _connectionStatus

    private val _connectionDetails = MutableStateFlow(ConnectionDetail())
    val connectionDetail: StateFlow<ConnectionDetail> = _connectionDetails

    private val _dialogUiState: MutableStateFlow<VpnDialogUIState> =
        MutableStateFlow(VpnDialogUIState.Loading)
    val dialogUiState: StateFlow<VpnDialogUIState> = _dialogUiState

    var currentServer: AppDatabase.VpnConfiguration? = null

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
        viewModelScope.launch {
            initializeConfigs()
            initializeVPNConfigs()
        }
    }

    fun connect(server: AppDatabase.VpnConfiguration) {
        currentServer = server
        viewModelScope.launch {
            _connectionStatus.emit(VpnConnectionStatus.Downloading())
            val config = fetchServerConfig(server.ovpn)
            if (config != null) {
                _connectionStatus.emit(VpnConnectionStatus.Downloaded(server = server, config = config))
                _dialogUiState.emit(VpnDialogUIState.Loading)
            } else {
                _connectionStatus.emit(VpnConnectionStatus.Invalid())
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _connectionStatus.emit(VpnConnectionStatus.StopVpn())
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

    private suspend fun initializeConfigs(): String? {
        val response = retrofitUtils.makeHttpCallAsync("http://ip-api.com/json")
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
        return null
    }

    private suspend fun initializeVPNConfigs() {
        val response = retrofitUtils.makeHttpCallAsync("https://pastebin.com/raw/sZMaeX89") // TODO: AppInterface.APP_DATABASE_URL
        if (response.isSuccessful) {
            val json = response.body?.string()
            response.close() // Always close
            AppDatabaseConverter.toAppDatabaseFromString(json)?.let { data ->
                vpnConfigurations.clear()
                vpnConfigurations.addAll(data.vpnConfigurations)
                if (data.vpnAffectedCountries.contains(country)) {
                    _showHover.emit(true)
                }
                _dialogUiState.emit(VpnDialogUIState.Detail(data.vpnConfigurations))
            }
        }
    }

    private suspend fun fetchServerConfig(url: String): String? {
        val response = retrofitUtils.makeHttpCallAsync(url)
        if (response.isSuccessful) {
            val body = response.body?.string()
            response.close() // Always close the stream
            return body
        }
        return null
    }

    data class ConnectionDetail(
        val duration: String = "00:00:00",
        val lastPacketReceive: String = "0",
        val bytesIn: String = " ",
        val bytesOut: String = " "
    )
}