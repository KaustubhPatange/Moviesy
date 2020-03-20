package com.kpstv.yts.models.data

import com.kpstv.yts.models.Movie

data class data_movie(val movie: Movie?, val movie_count: Int, val movies: ArrayList<Movie>?,
                      val limit: Int, val page_number: Int)