package com.popularmoviesstage2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.popularmoviesstage2.adapters.MovieAdapter;
import com.popularmoviesstage2.datatypes.Movie;
import com.popularmoviesstage2.datatypes.MovieList;
import com.popularmoviesstage2.utils.MoviePreference;
import com.popularmoviesstage2.utils.NetworkUtils;

import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.popularmoviesstage2.data.MovieContract.MovieEntry.CONTENT_URI;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickListener,
        LoaderManager.LoaderCallbacks<List<Movie>>, SharedPreferences.OnSharedPreferenceChangeListener {


    @BindView(R.id.movie_list)
    RecyclerView mMovieGrid;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.tv_error_message_display)
    TextView errMessage;
    @BindView(R.id.my_toolbar)
    Toolbar toolbar;
    private MovieAdapter mAdapter;
    private ActionBar bar;
    private final int LOADER_ID = 1112;

    Parcelable savedRecyclerLayoutState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        bar = getSupportActionBar();

        mAdapter = new MovieAdapter(this,this);
        GridLayoutManager layout = new GridLayoutManager(this,2);
        mMovieGrid.setLayoutManager(layout);
        mMovieGrid.setHasFixedSize(true);
        mMovieGrid.setAdapter(mAdapter);

        //Added!!!
        if(savedInstanceState != null)
        {
            savedRecyclerLayoutState = savedInstanceState.getParcelable("my_list");
            mMovieGrid.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }


    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Added!!!
        outState.putParcelable("my_list", mMovieGrid.getLayoutManager().onSaveInstanceState());
    }



    @Override
    protected void onResume() {
        super.onResume();
        loadMovie();
    }


    private void loadMovie() {
        showMovieView();
        String sort = MoviePreference.getSortOrder(this);
        changeTitlebar(sort);
        Bundle bundle = new Bundle();
        bundle.putCharSequenceArray(getString(R.string.param), new String[]{sort, getString(R.string.apiKey)});
        getSupportLoaderManager().restartLoader(LOADER_ID,bundle,this);
    }

    private void changeTitlebar(String sorttype) {
        if(sorttype.equals(getString(R.string.tmdb_popular)))
            bar.setTitle(getString(R.string.tmdb_popular_title));
        else if(sorttype.equals(getString(R.string.tmdb_top_rated)))
            bar.setTitle(getString(R.string.tmdb_top_rated_title));
        else if(sorttype.equals(getString(R.string.tmdb_favorite)))
            bar.setTitle(getString(R.string.tmdb_favorite_title));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_setting)
            startActivity(new Intent(this,SettingsActivity.class));
        return true;
    }


    @Override
    public void onclick(Movie moviedata) {
        Intent intent = new Intent(this,ChildActivity.class);
        intent.putExtra(getString(R.string.tmdb_movie_title),moviedata.getOriginal_title());
        intent.putExtra(getString(R.string.tmdb_movie_image),moviedata.getPoster_path());
        intent.putExtra(getString(R.string.tmdb_movie_overview),moviedata.getOverview());
        intent.putExtra(getString(R.string.tmdb_movie_ratings),moviedata.getVote_average());
        intent.putExtra(getString(R.string.tmdb_movie_release_date),moviedata.getRelease_date());
        intent.putExtra(getString(R.string.tmdb_movie_genre),moviedata.getGenre_ids());
        intent.putExtra(getString(R.string.tmdb_movie_id),moviedata.getId());
        startActivity(intent);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        final String params[] = args.getStringArray(getString(R.string.param));
        return new AsyncTaskLoader<List<Movie>>(this) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                mLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }
            @Override
            public List<Movie> loadInBackground() {
                if (params.length == 0) {
                    return null;
                }

                String sortorder = params[0];
                String apikey = params[1];
                URL movieRequestURL = NetworkUtils.buildUrl(sortorder,apikey);

                try {
                    if(!sortorder.equals(getString(R.string.tmdb_favorite))){
                        String movieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestURL);
                        List<Movie> MovieJSONResponse = new MovieList().getMovieList( movieResponse);
                        return MovieJSONResponse;
                    }
                    else{
                        Cursor cursor = getContentResolver().query(CONTENT_URI,null,null,null,null);
                        List<Movie> MovieJSONResponse = new MovieList().getMovieListFromCursor( cursor);
                        return MovieJSONResponse;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }
    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (data != null) {
            showMovieView();
            mAdapter.setMovieData(data);
            //Added!!!
            mMovieGrid.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        } else {
            showErrorView();
        }
    }
    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {

    }
    private void showErrorView() {
        mMovieGrid.setVisibility(View.INVISIBLE);
        errMessage.setVisibility(View.VISIBLE);
    }
    private void showMovieView() {
        errMessage.setVisibility(View.INVISIBLE);
        mMovieGrid.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        loadMovie();
    }
}
