package com.popularmoviesstage2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.popularmoviesstage2.R;

public class MoviePreference {

    public static String getSortOrder(Context vcontext){
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(vcontext);
        return shared.getString(vcontext.getString(R.string.tmdb_sort_type),vcontext.getString(R.string.tmdb_popular));
    }

}
