package com.kpstv.yts.extensions;


import java.util.HashMap;
import java.util.Map;

public class YTSQuery {
    public enum Quality {q720p, q1080p, q2160p, q3D}

    public enum SortBy {title, year, rating, peers, seeds, download_count, like_count, date_added}

    public enum OrderBy {ascending, descending}

    public enum Genre {comedy, documentary, musical, sport, history, sci_fi, horror, romance, western, action, thriller, drama, mystery, crime, animation, adventure, fantasy, family}

    public static class MovieBuilder {
        Map<String, String> vals = new HashMap<>();

        /**
         * The ID of the movie.
         */
        public MovieBuilder setMovieId(int id) {
            vals.put("movie_id", id + "");
            return this;
        }

        /**
         * When set the data returned will include the added image URLs.
         */
        public MovieBuilder setIncludeImages(boolean val) {
            vals.put("with_images", val + "");
            return this;
        }

        /**
         * When set the data returned will include the added information about the cast.
         */
        public MovieBuilder setIncludeCast(boolean val) {
            vals.put("with_cast", val + "");
            return this;
        }

        /**
         * @return The queryMap used by Retrofit interface
         */
        public Map<String, String> build() {
            return vals;
        }
    }

    public static class ListMoviesBuilder {
        Map<String, String> vals = new HashMap<>();

        /**
         * Sorts the results by choosen value
         *
         * @String (title, year, rating, peers, seeds, download_count, like_count, date_added)
         * @default (date_added)
         */
        public ListMoviesBuilder setSortBy(SortBy sortBy) {
            vals.put("sort_by", sortBy.name());
            return this;
        }

        /**
         * Orders the results by either Ascending or Descending order
         *
         * @String (desc, asc)
         * @default (desc)
         */
        public ListMoviesBuilder setOrderBy(OrderBy orderBy) {
            String val = "asc";
            if (orderBy.name().equals("descending")) val = "desc";
            vals.put("order_by", val);
            return this;
        }

        /**
         * Returns the list with the Rotten Tomatoes rating included
         *
         * @default (false)
         */
        public ListMoviesBuilder setWithRtRatings(boolean val) {
            vals.put("with_rt_ratings", val + "");
            return this;
        }

        /**
         * Used to filter by a given quality
         *
         * @String (720p, 1080p, 2160p, 3D)
         * @default (All)
         */
        public ListMoviesBuilder setQuality(Quality quality) {
            String val = "720p";
            switch (quality) {
                case q3D:
                    val = "3D";
                    break;
                case q720p:
                    val = "720p";
                    break;
                case q1080p:
                    val = "1080p";
                    break;
                case q2160p:
                    val = "2160p";
                    break;
            }
            vals.put("quality", val);
            return this;
        }

        /**
         * Used to filter by a given genre (See http://www.imdb.com/genre for full list)
         *
         * @default (All)
         */
        public ListMoviesBuilder setGenre(Genre genre) {
            String val = genre.name();
            if (val.equals("sci_fi"))
                val = "sci-fi";
            vals.put("genre", val);
            return this;
        }

        /**
         * The limit of results per page that has been set
         *
         * @Integer between 1 - 50 (inclusive)
         * @default (20)
         */
        public ListMoviesBuilder setLimit(int val) {
            vals.put("limit", val + "");
            return this;
        }

        /**
         * Used to see the next page of movies, eg limit=15 and page=2 will show you movies 15-30
         *
         * @default (1)
         */
        public ListMoviesBuilder setPage(int val) {
            vals.put("page", val + "");
            return this;
        }

        /**
         * Used to filter movie by a given minimum IMDb rating
         *
         * @Integer between 0 - 9 (inclusive)
         * @default (0)
         */
        public ListMoviesBuilder setMinimumRating(int val) {
            vals.put("minimum_rating", val + "");
            return this;
        }

        /**
         * Used for movie search, matching on: Movie Title/IMDb Code,
         * Actor Name/IMDb Code, Director Name/IMDb Code
         *
         * @default (0)
         */
        public ListMoviesBuilder setQuery(String val) {
            vals.put("query_term", val + "");
            return this;
        }

        /**
         * @return The queryMap used by Retrofit interface
         */
        public Map<String, String> build() {
            return vals;
        }
    }
}
