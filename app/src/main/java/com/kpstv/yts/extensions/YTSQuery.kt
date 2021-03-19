package com.kpstv.yts.extensions

import java.util.*


class YTSQuery {
    enum class Quality {
        q720p, q1080p, q2160p, q3D
    }

    enum class SortBy {
        title, year, rating, peers, seeds, download_count, like_count, date_added
    }

    enum class OrderBy {
        ascending, descending
    }

    enum class Genre {
        comedy, documentary, musical, sport, history, sci_fi, horror, romance, western, action, thriller, drama, mystery, crime, animation, adventure, fantasy, family
    }

    class MovieBuilder {
        var vals: MutableMap<String, String> =
            HashMap()

        /**
         * The ID of the movie.
         */
        fun setMovieId(id: Int): MovieBuilder {
            vals["movie_id"] = id.toString() + ""
            return this
        }

        /**
         * When set the data returned will include the added image URLs.
         *
         * Note: Response will not be successful sometimes when this is set to true
         */
        fun setIncludeImages(`val`: Boolean): MovieBuilder {
            vals["with_images"] = `val`.toString() + ""
            return this
        }

        /**
         * When set the data returned will include the added information about the cast.
         *
         * Note: Response will not be successful sometimes when this is set to true
         */
        fun setIncludeCast(`val`: Boolean): MovieBuilder {
            vals["with_cast"] = `val`.toString() + ""
            return this
        }

        /**
         * @return The queryMap used by Retrofit interface
         */
        fun build(): Map<String, String> {
            return vals
        }
    }

    class ListMoviesBuilder {
        companion object {
            fun getDefault(): ListMoviesBuilder =
                ListMoviesBuilder()
                    .setOrderBy(OrderBy.descending)
                    .setSortBy(SortBy.date_added)
                    .setPage(1)
                    .setLimit(20)
        }

        var vals: MutableMap<String, String> =
            HashMap()

        /**
         * Sorts the results by choosen value
         *
         * @String (title, year, rating, peers, seeds, download_count, like_count, date_added)
         * @default (date_added)
         */
        fun setSortBy(sortBy: SortBy): ListMoviesBuilder {
            vals["sort_by"] = sortBy.name
            return this
        }

        /**
         * Orders the results by either Ascending or Descending order
         *
         * @String (desc, asc)
         * @default (desc)
         */
        fun setOrderBy(orderBy: OrderBy): ListMoviesBuilder {
            var `val` = "asc"
            if (orderBy.name == "descending") `val` = "desc"
            vals["order_by"] = `val`
            return this
        }

        /**
         * Returns the list with the Rotten Tomatoes rating included
         *
         * @default (false)
         */
        fun setWithRtRatings(`val`: Boolean): ListMoviesBuilder {
            vals["with_rt_ratings"] = `val`.toString() + ""
            return this
        }

        /**
         * Used to filter by a given quality
         *
         * @String (720p, 1080p, 2160p, 3D)
         * @default (All)
         */
        fun setQuality(quality: Quality?): ListMoviesBuilder {
            var `val` = "720p"
            when (quality) {
                Quality.q3D -> `val` = "3D"
                Quality.q720p -> `val` = "720p"
                Quality.q1080p -> `val` = "1080p"
                Quality.q2160p -> `val` = "2160p"
            }
            vals["quality"] = `val`
            return this
        }

        /**
         * Used to filter by a given genre (See http://www.imdb.com/genre for full list)
         *
         * @default (All)
         */
        fun setGenre(genre: Genre): ListMoviesBuilder {
            var `val` = genre.name
            if (`val` == "sci_fi") `val` = "sci-fi"
            vals["genre"] = `val`
            return this
        }

        /**
         * The limit of results per page that has been set
         *
         * @Integer between 1 - 50 (inclusive)
         * @default (20)
         */
        fun setLimit(`val`: Int): ListMoviesBuilder {
            vals["limit"] = `val`.toString() + ""
            return this
        }

        /**
         * Used to see the next page of movies, eg limit=15 and page=2 will show you movies 15-30
         *
         * @default (1)
         */
        fun setPage(`val`: Int): ListMoviesBuilder {
            vals["page"] = `val`.toString() + ""
            return this
        }

        /**
         * Used to filter movie by a given minimum IMDb rating
         *
         * @Integer between 0 - 9 (inclusive)
         * @default (0)
         */
        fun setMinimumRating(`val`: Int): ListMoviesBuilder {
            vals["minimum_rating"] = `val`.toString() + ""
            return this
        }

        /**
         * Used for movie search, matching on: Movie Title/IMDb Code,
         * Actor Name/IMDb Code, Director Name/IMDb Code
         *
         * @default (0)
         */
        fun setQuery(`val`: String): ListMoviesBuilder {
            vals["query_term"] = `val` + ""
            return this
        }

        /**
         * @return The queryMap used by Retrofit interface
         */
        fun build(): Map<String, String> {
            return vals
        }
    }
}
