package com.arctouch.codechallenge.home

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.arctouch.codechallenge.model.Movie

/**
 * Created by mauker on 07/03/2018.
 * Interface that defines the Presenter methods from the MVP pattern for the HomeActivity.
 *
 */
interface HomePresenter {

    fun onDestroy()
    fun loadGenres()
    fun loadMovies()
    fun searchMovie(query: String)

    fun getRvScrollListener(layoutManager: LinearLayoutManager): RecyclerView.OnScrollListener

    // ViewHolder logic
    fun onBindMovie(position: Int, rowView: MovieRowView)
    fun onMovieClick(position: Int, rowView: MovieRowView)
    fun addFooter()
    fun removeFooter()
    fun addMovie(movie: Movie)
    fun addAll(newMovies: List<Movie>)
    fun removeMovie(movie: Movie)
    fun removeMovieAtPos(position: Int)
    fun removeAll()

    fun getMoviePos(movie: Movie): Int
    fun getMoviesCount(): Int

    fun isLoadingOnFooter(): Boolean
}