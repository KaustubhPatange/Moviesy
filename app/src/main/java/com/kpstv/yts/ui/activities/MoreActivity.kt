package com.kpstv.yts.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.kpstv.yts.AppInterface.Companion.setAppThemeNoAction
import com.kpstv.yts.R
import com.kpstv.yts.adapters.CustomPagedAdapter
import com.kpstv.yts.data.converters.QueryConverter
import com.kpstv.yts.extensions.MovieBase
import com.kpstv.yts.extensions.YTSQuery
import com.kpstv.yts.data.models.MovieShort
import com.kpstv.yts.databinding.ActivityMoreBinding
import com.kpstv.common_moviesy.extensions.viewBinding
import com.kpstv.yts.ui.viewmodels.MoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.custom_alert_buttons.view.*
import kotlinx.android.synthetic.main.custom_alert_filter.view.*
import kotlinx.android.synthetic.main.item_chip.view.*

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class MoreActivity : AppCompatActivity() {

    private val viewModel by viewModels<MoreViewModel>()
    private val binding by viewBinding(ActivityMoreBinding::inflate)

    private val TAG = "MoreActivity"

    companion object {
        var endPoint: String? = null
        var base: MovieBase? = null
        var queryMap: Map<String, String>? = null
        var genre: String? = null
    }

    /** This query map will be used to reset the values
     */

    private var localGenre: String? = null
    private var localBase: MovieBase? = null
    private var alertDialog: AlertDialog? = null
    private var query: Map<String, String>? = null
    private var localQuery: Map<String, String>? = null
    private var localEndpoint: String? = null
    private var updateHandler = Handler()
    private lateinit var adapter: CustomPagedAdapter
    private lateinit var dialogView: View

    /** Since our endpoint, base, queryMap static objects gets updated whenever
     *  new instance of this activity comes alive. We lost all the properties
     *  which is needed for this current instance.
     *
     *  In order to prevent the loss we create local objects of those static
     *  properties so that in onPause() we will save it and in onResume()
     *  we will retrieve it
     */
    override fun onPause() {

        Log.e(TAG, "=> onPause()")

        localEndpoint =
            endPoint
        localBase = base
        localQuery = queryMap
        localGenre = genre;
        super.onPause()
    }

    override fun onResume() {

        Log.e(TAG, "=> onResume()")

        if (localBase != null) {
            endPoint = localEndpoint
            base = localBase
            queryMap = localQuery
            genre = localGenre
        }
        super.onResume()
    }

    /** Main start point for an activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setAppThemeNoAction(this)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /** Setting title for the activity and updating views
         */
        title = intent?.extras?.getString("title")

        binding.swipeRefreshLayout.isEnabled = false
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.amChipLayout.visibility = View.GONE

        /** Updating the base, endpoint static object
         */
        endPoint = intent?.extras?.getString("endPoint")
        base = MovieBase.valueOf(intent?.extras?.getString("baseValue")!!)

        viewModel.buildNewConfig()

        /** If the base is YTS we will show we will set query coming from intent
         */
        if (base == MovieBase.YTS) {
            val keys = intent?.extras?.getStringArrayList("keys")
            val values = intent?.extras?.getStringArrayList("values")

            val map = HashMap<String, String>()
            (0 until keys?.size!!).forEach { i ->
                map[keys[i]] = values?.get(i)!!
            }
            query = map

            setupDialogView()

            findGenre(query)

            initQueries(query)
        } else {
            setupRecyclerView()
        }
    }

    /**
     * Since this filter dialog only supports filtering on sort_by
     * and order_by, by updating the query we lose the genre filter
     * if applied. This function will store the filter into a companion
     * string object and add it whenever query is changed.
     *
     * eg: For GenreFragment (categories)
     */
    private fun findGenre(map: Map<String, String>?) {
        val genreQuery = map?.filterKeys { it == "genre" }
        if (genreQuery != null)
            genre = genreQuery["genre"]
    }

    /**
     * As above this will insert the genre into the map.
     */
    private fun insertGenre(map: Map<String, String>) {
        if (!map.keys.contains("genre") && genre != null) {
            val hashMap = HashMap(map)
            hashMap["genre"] = genre
            queryMap = hashMap
        }
    }

    /** This is used to parse query and update the filter layout by
     *  adding chips and also responsible for setting up recyclerView
     */
    private fun initQueries(map: Map<String, String>?) {

        binding.amChipGroup.removeAllViews()

        Log.e(TAG, "=> InitQueries")

        val keys = ArrayList(map?.keys!!)
        val values = ArrayList(map.values)

        queryMap = map

        for (c in 0 until keys.size) {
            if (keys[c] == "sort_by" || keys[c] == "order_by")
                createChip(values[c])
        }
        setupRecyclerView()
    }

    /** A function used to create Chip from item_chip layout with proper
     *  text and adding it into chipGroup
     */
    private fun createChip(title: String) {

        Log.e(TAG, "=> Adding chip: $title")

        val chipView = LayoutInflater.from(this)
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

    /** This is called when the chip from chipGroup is removed.
     *  It updates the static queryMap object from the visible chip in chipGroup
     */
    private fun updateQueries() {
        if (base == MovieBase.YTS) {
            if (binding.amChipGroup.childCount <= 0) {
                Toasty.error(applicationContext, "Cannot remove last item").show()

                initQueries(query)

                return
            } else {
                Log.e(TAG, "=> UpdateQueries")
                val builder = YTSQuery.ListMoviesBuilder()
                binding.amChipGroup.children.forEach {
                    val chip = it.chip
                    when (chip.text) {
                        getString(R.string.date) ->
                            builder.setSortBy(YTSQuery.SortBy.date_added)
                        getString(R.string.download) ->
                            builder.setSortBy(YTSQuery.SortBy.download_count)
                        getString(R.string.popular) ->
                            builder.setSortBy(YTSQuery.SortBy.like_count)
                        getString(R.string.rating) ->
                            builder.setSortBy(YTSQuery.SortBy.rating)
                        getString(R.string.seeders) ->
                            builder.setSortBy(YTSQuery.SortBy.seeds)
                        getString(R.string.year) ->
                            builder.setSortBy(YTSQuery.SortBy.year)
                        getString(R.string.asc) ->
                            builder.setOrderBy(YTSQuery.OrderBy.ascending)
                        getString(R.string.desc) ->
                            builder.setOrderBy(YTSQuery.OrderBy.descending)
                    }
                }
                queryMap = builder.build()
            }
            setupRecyclerView()
        }
    }

    /**
     *  This is used to setup viewModel and recyclerView.
     *
     *  Endless recyclerView is implemented using Pagination which returns
     *  live page data.
     *
     *  Whenever a configuration is changed we need to reload the data from
     *  source factory hence we use viewModel.buildNewConfig to reload it.
     *
     */
    private fun setupRecyclerView() {

        /** Calling insert genre here which will automatically inject
         *  genre parameter in the map.
         */
        insertGenre(queryMap ?: HashMap())

        binding.swipeRefreshLayout.isRefreshing = true

        if (base == MovieBase.YTS)
            viewModel.buildNewConfig()

        if (queryMap != null)
            Log.e(TAG, "=> Query: ${QueryConverter.fromMapToString(queryMap!!)}")

        adapter = CustomPagedAdapter(this, base!!)

        viewModel.itemPagedList?.observe(this, observer)

        binding.recyclerView.adapter = adapter

        /** Setting up handler for callbacks */
        updateHandler.postDelayed(updateTask, 1000)
    }

    /** An observer which will observe changes in live data from viewModel
     *  and submit it to adapter
     */
    private val observer = Observer<PagedList<MovieShort>?> {
        adapter.submitList(it)
    }


    /** This dialog view is only available when base is YTS. Basically it is
     *  a filter dialog for YTS queries
     */
    private fun setupDialogView() {

        dialogView = LayoutInflater.from(this).inflate(
            R.layout.custom_alert_filter, null
        )

        dialogView.alertPositiveText.text = "Apply"
        dialogView.alertPositiveText.setOnClickListener {

            val map = HashMap<String, String>()

            dialogView.sortChipGroup.forEach {
                val chip = it as Chip
                if (chip.isChecked) {
                    map["sort_by"] = getQueryTextfromText(chip.text.toString())!!
                }
            }
            dialogView.OrderChipGroup.forEach {
                val chip = it as Chip
                if (chip.isChecked) {
                    map["order_by"] = getQueryTextfromText(chip.text.toString())!!
                }
            }

            initQueries(map)

            alertDialog?.dismiss()
        }
        dialogView.alertCardNegative.setOnClickListener {
            alertDialog?.dismiss()
        }

        alertDialog = AlertDialog.Builder(this).apply {
            setView(dialogView)
            setCancelable(false)
        }.create()


        binding.itemFilterMore.setOnClickListener {
            showAlertDialog()
        }
    }

    private fun showAlertDialog() {
        val values: ArrayList<String>? = ArrayList(queryMap?.values!!)

        if (queryMap?.containsKey("sort_by") == true)
            dialogView.sortChipGroup.forEach {
                val chip = it as Chip
                chip.isChecked = false
                (0 until values?.size!!).forEach { i ->
                    if (chip.text == getTextfromQuery(values[i]))
                        chip.isChecked = true
                }
            }
        else dialogView.sortChipGroup.clearCheck()

        if (queryMap?.containsKey("order_by") == true)
            dialogView.OrderChipGroup.forEach {
                val chip = it as Chip
                chip.isChecked = false
                (0 until values?.size!!).forEach { i ->
                    if (chip.text == getTextfromQuery(values[i]))
                        chip.isChecked = true
                }
            }
        else dialogView.OrderChipGroup.clearCheck()

        alertDialog?.show()
    }

    /** Since fetching live data depends on network parameter, so I've created
     *  a handler task which will check if Adapter has atleast one item.
     *
     *  Depending on it we are hiding the progress
     */
    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            try {
                if (binding.recyclerView.adapter?.itemCount ?: 0 <= 0) {
                    updateHandler.postDelayed(this, 1000)
                } else {

                    if (base == MovieBase.YTS)
                        binding.amChipLayout.visibility = View.VISIBLE

                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                Toasty.error(applicationContext, "Error: ${e.message}").show()
            }
        }
    }

    private fun getTextfromQuery(text: String) =
        when (text) {
            "desc" -> getString(R.string.desc)
            "asc" -> getString(R.string.asc)
            YTSQuery.SortBy.date_added.name -> getString(R.string.date)
            YTSQuery.SortBy.download_count.name -> getString(R.string.download)
            YTSQuery.SortBy.year.name -> getString(R.string.year)
            YTSQuery.SortBy.seeds.name -> getString(R.string.seeders)
            YTSQuery.SortBy.rating.name -> getString(R.string.rating)
            YTSQuery.SortBy.like_count.name -> getString(R.string.popular)
            else -> null
        }

    private fun getQueryTextfromText(text: String) =
        when (text) {
            getString(R.string.date) ->
                YTSQuery.SortBy.date_added.name
            getString(R.string.download) ->
                YTSQuery.SortBy.download_count.name
            getString(R.string.popular) ->
                YTSQuery.SortBy.like_count.name
            getString(R.string.rating) ->
                YTSQuery.SortBy.rating.name
            getString(R.string.seeders) ->
                YTSQuery.SortBy.seeds.name
            getString(R.string.year) ->
                YTSQuery.SortBy.year.name
            getString(R.string.asc) ->
                "asc"
            getString(R.string.desc) ->
                "desc"
            else -> null
        }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateHandler.removeCallbacks(updateTask)
    }
}
