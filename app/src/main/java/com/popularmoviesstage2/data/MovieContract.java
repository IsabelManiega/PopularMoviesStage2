package com.popularmoviesstage2.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.popularmoviesstage2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";

    public final static class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES)
                .build();

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVERAGE = "vote_Average";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_IMG = "img";
    }
}
