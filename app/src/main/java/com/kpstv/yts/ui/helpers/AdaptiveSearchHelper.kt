package com.kpstv.yts.ui.helpers

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.kpstv.yts.R
import com.kpstv.yts.databinding.CustomAdaptiveSearchBinding
import com.kpstv.yts.extensions.hide
import com.kpstv.yts.extensions.show
import com.kpstv.yts.extensions.utils.AppUtils

class AdaptiveSearchHelper(
    private val context: Context,
    viewLifecycleOwner: LifecycleOwner
) {

    constructor(activity: AppCompatActivity) : this(activity, activity)

    companion object {
        private const val SHOW_ADAPTIVE_TIP_PREF = "show_adaptive_tip_pref"

        private val textExcluder = arrayOf(
            "(full movie)(.*)?", "(movie)(.*)?", "(download)(.*)?"
        )
    }

    private lateinit var binding: CustomAdaptiveSearchBinding
    private val filters = MutableLiveData<Pair<Boolean, String>?>()
    private val toShowTip: Boolean

    init {
        toShowTip = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(SHOW_ADAPTIVE_TIP_PREF, true)
        filters.observe(viewLifecycleOwner, Observer {
            if (it?.first == true) {
                showFilterLayout(it.second)
            } else
                binding.root.hide()
        })
    }

    /**
     * Bind layout before doing any jobs.
     */
    fun bindLayout(binding: CustomAdaptiveSearchBinding) {
        this.binding = binding
        binding.root.hide()

        if (!toShowTip)
            binding.adaptiveTip.hide()

        binding.btnClose.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(context).edit {
                putBoolean(SHOW_ADAPTIVE_TIP_PREF, false)
            }
            binding.adaptiveTip.hide()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showFilterLayout(query: String) = with(context) {
        binding.tvResultsFor.text = AppUtils.getHtmlText(
            "<i>${getString(R.string.results_for)} <b>${query}</b></i>"
        )
        binding.root.show()
    }

    /**
     * Filter the text
     */
    fun querySearch(text: String): String {
        var query = text
        textExcluder.forEach {
            if (it.toRegex().containsMatchIn(query)) {
                query = query.replace(it.toRegex(), "")
            }
        }
        filters.postValue(Pair(query != text, query))
        return query.trim()
    }
}