package com.kpstv.yts.vpn

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kpstv.common_moviesy.extensions.enableDelayedTransition
import com.kpstv.common_moviesy.extensions.hide
import com.kpstv.common_moviesy.extensions.show
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.R
import com.kpstv.yts.data.models.AppDatabase
import com.kpstv.yts.databinding.CustomDialogVpnBinding
import com.kpstv.yts.databinding.CustomDialogVpnItemBinding
import com.kpstv.yts.extensions.load
import com.kpstv.yts.extensions.utils.FlagUtils
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class VPNDialogFragment : DialogFragment(R.layout.custom_dialog_vpn) {
    private val vpnViewModel by activityViewModels<VPNViewModel>()
    private val binding by viewBinding(CustomDialogVpnBinding::bind)

    private lateinit var adapter: Adapter

    @Inject lateinit var flagUtils: FlagUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.theme?.applyStyle(R.style.DialogFragmentStyle, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUIState()
        observeConnectionStatus()
        observerConnectionDetail()

        setButtonListeners()
        setRecyclerView()
    }

    private fun setRecyclerView() {
        adapter = Adapter(
            flagUtils = flagUtils,
            onItemClick = { server ->
                vpnViewModel.connect(server)
                Toasty.info(requireContext(), getString(R.string.attempt_connect_vpn)).show()
            }
        )
        binding.detailLayout.vpnRecyclerView.adapter = adapter
    }

    private fun setButtonListeners() {
        binding.detailLayout.btnListClose.setOnClickListener { dismiss() }
        binding.connectedLayout.btnDetailClose.setOnClickListener { dismiss() }
        binding.loadingLayout.btnStop.setOnClickListener {
            vpnViewModel.disconnect()
        }
        binding.connectedLayout.btnDisconnect.setOnClickListener {
            vpnViewModel.disconnect()
        }
    }

    private fun observeUIState() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            vpnViewModel.dialogUiState.collect { uiState ->
                when(uiState) {
                    is VpnDialogUIState.Loading -> {
                        binding.detailLayout.root.hide()
                        binding.connectedLayout.root.hide()
                        binding.loadingLayout.root.show()
                    }
                    is VpnDialogUIState.Detail -> {
                        binding.connectedLayout.root.hide()
                        binding.loadingLayout.root.hide()
                        binding.detailLayout.root.show()

                        adapter.submitList(uiState.vpnConfigurations)
                    }
                    is VpnDialogUIState.Connected -> {
                        binding.loadingLayout.root.hide()
                        binding.detailLayout.root.hide()
                        binding.connectedLayout.root.show()

                        vpnViewModel.currentServer?.let { config ->
                            binding.connectedLayout.ivCountry.load(flagUtils.getMatchingFlagUrl(config.country))
                        }
                        binding.connectedLayout.tvIp.text = getString(R.string.vpn_ip, uiState.ip)
                    }
                }
            }
        }
    }

    private fun observeConnectionStatus() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vpnViewModel.connectionStatus.collect { state ->
                val tvStatus = binding.loadingLayout.tvConnectionStatus
                if (state !is VpnConnectionStatus.Unknown) tvStatus.setTextColor(state.color)
                when(state) {
                    is VpnConnectionStatus.Authenticating -> tvStatus.text = getString(R.string.status_auth)
                    is VpnConnectionStatus.Connected -> tvStatus.text = getString(R.string.status_connected)
                    is VpnConnectionStatus.Disconnected -> tvStatus.text = getString(R.string.status_disconnected)
                    is VpnConnectionStatus.Downloading -> tvStatus.text = getString(R.string.status_download)
                    is VpnConnectionStatus.Downloaded -> tvStatus.text = getString(R.string.status_try_connect)
                    is VpnConnectionStatus.NoNetwork -> tvStatus.text = getString(R.string.status_nonetwork)
                    is VpnConnectionStatus.Reconnecting -> tvStatus.text = getString(R.string.status_reconnect)
                    is VpnConnectionStatus.Waiting -> tvStatus.text = getString(R.string.status_wait)
                    is VpnConnectionStatus.Invalid -> tvStatus.text = getString(R.string.status_invalid)
                    is VpnConnectionStatus.GetConfig -> tvStatus.text = getString(R.string.status_get_config)
                    is VpnConnectionStatus.Unknown -> {}
                    else -> tvStatus.text = getString(R.string.status_unknown)
                }
            }
        }
    }

    private fun observerConnectionDetail() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vpnViewModel.connectionDetail.collect { detail ->
                binding.connectedLayout.tvDuration.text = getString(R.string.vpn_duration, detail.duration)
                binding.connectedLayout.tvPacket.text = getString(R.string.vpn_packet, detail.lastPacketReceive)
                binding.connectedLayout.tvBytesIn.text = getString(R.string.vpn_bytes_in, detail.bytesIn)
                binding.connectedLayout.tvBytesOut.text = getString(R.string.vpn_duration, detail.bytesOut)
            }
        }
    }

    class Adapter(private val flagUtils: FlagUtils, private val onItemClick: (AppDatabase.VpnConfiguration) -> Unit)
        : ListAdapter<AppDatabase.VpnConfiguration, Adapter.MainHolder>(diffUtil) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
            return MainHolder(CustomDialogVpnItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: MainHolder, position: Int) {
            val item = getItem(position)
            with(holder.binding) {
                tvCountry.text = item.country
                tvIp.text = item.ip

                val flagUrl = flagUtils.getMatchingFlagUrl(item.country)
                if (flagUrl != null) icon.load(flagUrl)

                root.setOnClickListener { onItemClick.invoke(item) }
            }
        }

        class MainHolder(val binding: CustomDialogVpnItemBinding): RecyclerView.ViewHolder(binding.root)
        companion object {
            private val diffUtil = object: DiffUtil.ItemCallback<AppDatabase.VpnConfiguration>() {
                override fun areItemsTheSame(oldItem: AppDatabase.VpnConfiguration, newItem: AppDatabase.VpnConfiguration) = oldItem === newItem
                override fun areContentsTheSame(oldItem: AppDatabase.VpnConfiguration, newItem: AppDatabase.VpnConfiguration) = oldItem == newItem
            }
        }
    }
}