package com.kpstv.yts.ui.fragments

import android.animation.ValueAnimator
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.kpstv.common_moviesy.extensions.applyBottomInsets
import com.kpstv.common_moviesy.extensions.applyTopInsets
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.ValueFragment
import com.kpstv.yts.R
import com.kpstv.yts.adapters.CustomPagedAdapter
import com.kpstv.yts.data.converters.QueryConverter
import com.kpstv.yts.databinding.FragmentMoreBinding
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.extensions.utils.AppUtils
import com.kpstv.yts.ui.viewmodels.MoreViewModel
import com.kpstv.yts.ui.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.custom_alert_buttons.view.*
import kotlinx.android.synthetic.main.custom_alert_filter.view.*
import kotlinx.android.synthetic.main.item_chip.view.*

// Some of the old implementation do exist.

@AndroidEntryPoint
class MoreFragment : ValueFragment(R.layout.fragment_more) {
    private val binding by viewBinding(FragmentMoreBinding::bind)
    private val viewModel by viewModels<MoreViewModel>()
    private val navViewModel by activityViewModels<StartViewModel>()

    /** This query map will be used to reset the values
     */
    private var genre: String? = null
    private var movieBase: MovieBase? = null
    private var alertDialog: AlertDialog? = null
    private var query: Map<String, String>? = null
    private var endPoint: String? = null
    private var queryMap: Map<String, String>? = null
    private var updateHandler = Handler()
    private lateinit var adapter: CustomPagedAdapter
    private lateinit var dialogView: View

    private lateinit var fragArgs: Args

    private val TAG = javaClass.simpleName

    override val backStackName: String = AppUtils.getUniqueBackStackName()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragArgs = getKeyArgs()

        setToolbar()
        binding.amChipLayout.applyBottomInsets(pad = true)
        binding.swipeRefreshLayout.isEnabled = false
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3) // Can you make this dynamic

        movieBase = MovieBase.valueOf(fragArgs.movieBaseString)
        endPoint = fragArgs.endPoint

        viewModel.buildNewConfig(endPoint, movieBase)

        /** If the base is YTS we will show we will set query coming from intent
         */
        if (movieBase == MovieBase.YTS) {
            val keys = fragArgs.keyArrayList
            val values = fragArgs.valueArrayList

            val map = HashMap<String, String>()
            (0 until keys.size).forEach { i ->
                map[keys[i]] = values.get(i)
            }
            query = map

            setupDialogView()
            findGenre(query)
            initQueries(query)
        } else {
            setupRecyclerView()
        }
    }

    private fun setToolbar() {
        binding.toolbar.applyTopInsets()
        binding.toolbar.title = fragArgs.title
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener { goBack() }
    }

    private fun setupDialogView() {
        dialogView = LayoutInflater.from(requireContext()).inflate(
            R.layout.custom_alert_filter, null
        )
        dialogView.alertPositiveText.text = getString(R.string.apply)
        dialogView.alertPositiveText.setOnClickListener {
            val map = HashMap<String, String>()
            chipGroupToMap(dialogView.qualityChipGroup, "quality", map)
            chipGroupToMap(dialogView.sortChipGroup, "sort_by", map)
            chipGroupToMap(dialogView.OrderChipGroup, "order_by", map)
            initQueries(map)

            alertDialog?.dismiss()
        }
        dialogView.alertCardNegative.setOnClickListener {
            alertDialog?.dismiss()
        }
        alertDialog = AlertDialog.Builder(requireContext()).apply {
            setView(dialogView)
            setCancelable(false)
        }.create()

        binding.itemFilterMore.setOnClickListener {
            showAlertDialog()
        }
    }

    private fun chipGroupToMap(chipGroup: ChipGroup, type: String, outMap: HashMap<String, String>) {
        chipGroup.forEach {
            val chip = it as Chip
            if (chip.isChecked) {
                outMap[type] = getQueryTextfromText(chip.text.toString())
            }
        }
    }

    private fun showAlertDialog() {
        val values: List<String> = queryMap?.values!!.toList()

        preSelectChipGroup(dialogView.qualityChipGroup, "quality", values)
        preSelectChipGroup(dialogView.sortChipGroup, "sort_by", values)
        preSelectChipGroup(dialogView.OrderChipGroup, "order_by", values)

        alertDialog?.show()
    }

    private fun preSelectChipGroup(chipGroup: ChipGroup, type: String, values: List<String>) {
        if (queryMap?.containsKey(type) == true)
            chipGroup.forEach {
                val chip = it as Chip
                chip.isChecked = false
                values.indices.forEach { i ->
                    if (chip.text == getTextfromQuery(values[i]))
                        chip.isChecked = true
                }
            }
        else
            chipGroup.clearCheck()
    }

    private fun findGenre(map: Map<String, String>?) {
        val genreQuery = map?.filterKeys { it == "genre" }
        if (genreQuery != null)
            genre = genreQuery["genre"]
    }

    private fun initQueries(map: Map<String, String>?) {
        binding.amChipGroup.removeAllViews()
        Log.e(TAG, "=> InitQueries")
        val keys = ArrayList(map?.keys!!)
        val values = ArrayList(map.values)
        queryMap = map

        for (c in 0 until keys.size) {
            if (keys[c] == "sort_by" || keys[c] == "order_by" || keys[c] == "quality")
                createChip(values[c])
        }
        setupRecyclerView()
    }

    private fun createChip(title: String) {
        val chipView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_chip, null)
        chipView.chip.apply {
            text = getTextfromQuery(title)
            setOnCloseIconClickListener {
                binding.amChipGroup.removeView(chipView)
                updateQueries()
            }
        }
        binding.amChipGroup.addView(chipView)
    }

    private fun updateQueries() {
        if (movieBase == MovieBase.YTS) {
            if (binding.amChipGroup.childCount <= 0) {
                Toasty.error(requireContext(), "Cannot remove last item").show()
                initQueries(query)
                return
            } else {
                Log.e(TAG, "=> UpdateQueries")
                val builder = YTSQuery.ListMoviesBuilder()
                binding.amChipGroup.children.forEach {
                    val chip = it.chip
                    when (chip.text) {
                        getString(R.string.date) -> builder.setSortBy(YTSQuery.SortBy.date_added)
                        getString(R.string.download) -> builder.setSortBy(YTSQuery.SortBy.download_count)
                        getString(R.string.popular) -> builder.setSortBy(YTSQuery.SortBy.like_count)
                        getString(R.string.rating) -> builder.setSortBy(YTSQuery.SortBy.rating)
                        getString(R.string.seeders) -> builder.setSortBy(YTSQuery.SortBy.seeds)
                        getString(R.string.year) -> builder.setSortBy(YTSQuery.SortBy.year)
                        getString(R.string.asc) -> builder.setOrderBy(YTSQuery.OrderBy.ascending)
                        getString(R.string.desc) -> builder.setOrderBy(YTSQuery.OrderBy.descending)
                        getString(R.string._3d) -> builder.setQuality(YTSQuery.Quality.q3D)
                        getString(R.string._4k) -> builder.setQuality(YTSQuery.Quality.q2160p)
                        getString(R.string._1080p) -> builder.setQuality(YTSQuery.Quality.q1080p)
                        getString(R.string._720p) -> builder.setQuality(YTSQuery.Quality.q720p)
                    }
                }
                queryMap = builder.build()
            }
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        /** Calling insert genre here which will automatically inject
         *  genre parameter in the map.
         */
        insertGenre(queryMap ?: HashMap())
        binding.swipeRefreshLayout.isRefreshing = true

        if (movieBase == MovieBase.YTS)
            viewModel.buildNewConfig(endPoint, movieBase, queryMap)

        if (queryMap != null)
            Log.e(TAG, "=> Query: ${QueryConverter.fromMapToString(queryMap!!)}")

        adapter = CustomPagedAdapter(navViewModel, movieBase!!)

        viewModel.itemPagedList?.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.recyclerView.adapter = adapter

        /** Setting up handler for callbacks */
        updateHandler.postDelayed(updateTask, 1000)
    }

    private fun insertGenre(map: Map<String, String>) {
        if (!map.keys.contains("genre") && genre != null) {
            val hashMap = HashMap(map)
            hashMap["genre"] = genre
            queryMap = hashMap
        }
    }

    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            try {
                if (binding.recyclerView.adapter?.itemCount ?: 0 <= 0) {
                    updateHandler.postDelayed(this, 1000)
                } else {
                    if (movieBase == MovieBase.YTS)
                        animateShowChipLayout()

                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                Toasty.error(requireContext(), "Error: ${e.message}").show()
            }
        }
    }

    private fun getTextfromQuery(text: String): String =
        when (text) {
            "2160p" -> getString(R.string._4k)
            "desc" -> getString(R.string.desc)
            "asc" -> getString(R.string.asc)
            YTSQuery.SortBy.date_added.name -> getString(R.string.date)
            YTSQuery.SortBy.download_count.name -> getString(R.string.download)
            YTSQuery.SortBy.year.name -> getString(R.string.year)
            YTSQuery.SortBy.seeds.name -> getString(R.string.seeders)
            YTSQuery.SortBy.rating.name -> getString(R.string.rating)
            YTSQuery.SortBy.like_count.name -> getString(R.string.popular)
            else -> text
        }

    private fun getQueryTextfromText(text: String): String =
        when (text) {
            getString(R.string.date) -> YTSQuery.SortBy.date_added.name
            getString(R.string.download) -> YTSQuery.SortBy.download_count.name
            getString(R.string.popular) -> YTSQuery.SortBy.like_count.name
            getString(R.string.rating) -> YTSQuery.SortBy.rating.name
            getString(R.string.seeders) -> YTSQuery.SortBy.seeds.name
            getString(R.string.year) -> YTSQuery.SortBy.year.name
            getString(R.string.asc) -> "asc"
            getString(R.string.desc) -> "desc"
            getString(R.string._4k) -> "2160p"
            else -> text
        }

    private fun animateShowChipLayout() = with(binding.amChipLayout) {
        if (visibility == View.VISIBLE) return@with

        val parent = parent as ViewGroup

        if (!isLaidOut) {
            measure(
                View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.AT_MOST)
            )
            layout(parent.left, parent.height - measuredHeight, parent.width, parent.height)
        }

        val drawable = BitmapDrawable(context.resources, drawToBitmap())
        drawable.setBounds(left, parent.height, right, parent.height + height)
        parent.overlay.add(drawable)

        ValueAnimator.ofInt(parent.height, top).apply {
            duration = 250
            addUpdateListener {
                val value = it.animatedValue as Int
                drawable.setBounds(0, value, parent.width, parent.height + value)
            }
            doOnEnd {
                parent.overlay.remove(drawable)
                visibility = View.VISIBLE
            }
            start()
        }
    }

    override fun onDestroyView() {
        updateHandler.removeCallbacks(updateTask)
        super.onDestroyView()
    }

    @Parcelize
    data class Args(
        val title: String,
        val endPoint: String = "",
        val movieBaseString: String,
        val keyArrayList: ArrayList<String> = arrayListOf(),
        val valueArrayList: ArrayList<String> = arrayListOf()
    ) : BaseArgs(), Parcelable
}