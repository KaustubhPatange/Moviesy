package com.kpstv.yts.data.models.data

import com.kpstv.yts.data.models.Movie

data class data_movie(val movie: Movie?, val movie_count: Int, val movies: ArrayList<Movie>?,
                      val limit: Int, val page_number: Int)