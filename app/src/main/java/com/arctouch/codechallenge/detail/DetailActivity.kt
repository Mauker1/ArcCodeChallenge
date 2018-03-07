package com.arctouch.codechallenge.detail

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.api.ApiManager.api
import com.arctouch.codechallenge.api.TmdbApi
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.util.MovieImageUrlBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    companion object {
        private val LOG_TAG = DetailActivity::class.java.simpleName

        const val EXTRA_MOVIE_ID = "MOVIE_ID"
    }

    private val movieImageUrlBuilder = MovieImageUrlBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Hide the default title.
        supportActionBar?.title = ""
        toolbar_layout?.isTitleEnabled = false

        val movieId = intent.getLongExtra(EXTRA_MOVIE_ID, -1)

        if (movieId > -1) {
            api.movie(movieId, TmdbApi.API_KEY, TmdbApi.DEFAULT_LANGUAGE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Log.d(LOG_TAG, "Got the movie!")

                        // Load the fetched data.
                        setupDetails(it)
                    }
        }
        else {
            // TODO - Handle error gracefully
            finish()
        }
    }

    /**
     * Fills the card and the toolbar layout with the downloaded data.
     */
    private fun setupDetails(movie: Movie) {
        // Show the title.
        toolbar_layout?.isTitleEnabled = true
        toolbar_layout?.title = movie.title

        movieTitle.text = movie.title
        stars.text = String.format(Locale.getDefault(), getString(R.string.stars_and_votes, movie.voteAverage, movie.voteCount))
        genres.text = movie.genres?.joinToString(separator = ", ") { it.name }
        overview.text = movie.overview

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat.getDateInstance()
        val outDate = outputFormat.format(inputFormat.parse(movie.releaseDate))

        releaseDate.text = outDate

        Glide.with(this)
                .load(movie.posterPath?.let { movieImageUrlBuilder.buildPosterUrl(it) })
                .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                .into(moviePoster)

        Glide.with(this)
                .load(movie.backdropPath?.let { movieImageUrlBuilder.buildBackdropUrl(it) })
                .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                .into(backdrop)

        progressBar.visibility = View.GONE
        detailCard.visibility = View.VISIBLE
    }
}
