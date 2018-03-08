package com.arctouch.codechallenge.home

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.api.ApiManager
import com.arctouch.codechallenge.api.TmdbApi
import com.arctouch.codechallenge.data.Cache
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.model.MoviesResponse
import com.arctouch.codechallenge.util.MovieImageUrlBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by mauker on 08/03/2018.
 * Implementation of the HomePresenter interface.
 * This class is responsible for updating the HomeView class, and the MovieRowView class.
 */
class HomePresenterImpl(
        private var homeView: HomeView?,
        private var adapterView: HomeAdapterView?,
        private val model: ApiManager) : HomePresenter {

    private val adapterModel : MutableList<Movie> = ArrayList()

    /**
     * Pagination variables
     */

    // Always start from the first page.
    private var curPage = 1L
    // The API docs says that the limit of pagination is 1000.
    private var maxPages = 1000
    // The number of pages returned by the API.
    private var numPages = 0

    private var isLoading = false
    private var isLastPage = false

    // Flag that will indicate whether the footer has been added or not.
    private var isLoadingOnFooter = false

    // End section

    /**
     * HomeView methods
     */
    override fun onDestroy() {
        homeView = null
        adapterView = null
    }

    /**
     * Method that will fetch the movies from the API, and display the results on the RV.
     */
    override fun loadMovies() {
        if (!isLoading) {
            if (curPage == 1L)
                homeView?.showProgress()
            else if (curPage > 1L)
                homeView?.postOnRv(Runnable { addFooter() })

            if (!isLastPage) {
                isLoading = true
                model.api.upcomingMovies(TmdbApi.API_KEY, TmdbApi.DEFAULT_LANGUAGE, curPage, TmdbApi.DEFAULT_REGION)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry(3)
                        .onErrorReturn {
                            it.stackTrace
                            MoviesResponse(1, ArrayList(), 1, 0)
                        }
                        .subscribe {
                            isLoading = false

                            if (it.totalResults > 0) {
                                val moviesWithGenres = it.results.map { movie ->
                                    movie.copy(genres = Cache.genres.filter { movie.genreIds?.contains(it.id) == true })
                                }

                                // Check if we're past the first page. Which means there's a footer to be removed.
                                if (curPage > 1L) removeFooter()

                                // Check if it's the last page
                                isLastPage = it.totalPages <= it.page
                                // Increment the page counter if it's not the last page yet.
                                if (!isLastPage)
                                    curPage = it.page + 1L
                                // Insert the results on the RV.
                                addAll(moviesWithGenres)

                                homeView?.hideProgress()
                            }
                            else {
                                homeView?.hideProgress()
                                homeView?.showErrorMessage(
                                        R.string.no_upcoming_results,
                                        View.OnClickListener { loadMovies() },
                                        R.string.action_try_again
                                )
                            }
                        }// End subscribe
            }
        }
    }

    override fun loadGenres() {
        homeView?.showProgress()
        model.api.genres(TmdbApi.API_KEY, TmdbApi.DEFAULT_LANGUAGE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(3)
                .subscribe {
                    Cache.cacheGenres(it.genres)
                    loadMovies()
                }
    }

    /**
     * Searchs for a movie based on a String query.
      */
    override fun searchMovie(query: String) {
        if (!isLoading) {
            homeView?.showProgress()
            isLoading = true

            model.api.searchMovie(TmdbApi.API_KEY, TmdbApi.DEFAULT_LANGUAGE, query)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .retry(3)
                    .subscribe {

                        isLoading = false

                        if (it.totalResults > 0) {
                            addAll(it.results)
                            homeView?.hideProgress()
                        }
                        else {
                            homeView?.hideProgress()
                            homeView?.showErrorMessage(R.string.no_query_results)
                        }
                    }
        }
    }

    override fun getRvScrollListener(layoutManager: LinearLayoutManager): RecyclerView.OnScrollListener {
        return object: RvScrollListener(layoutManager) {
            override fun isLoading(): Boolean = isLoading
            override fun isLastPage(): Boolean = isLastPage
            override fun getTotalPageCount(): Int = if (numPages > 0) numPages else maxPages

            override fun loadMoreItems() {
                loadMovies()
            }
        }
    }

    // End section.

    /**
     * AdapterView methods
     */

    override fun onBindMovie(position: Int, rowView: MovieRowView) {
        val movie = adapterModel[position]

        rowView.setTitle(movie.title)
        rowView.setGenres(movie.genres?.joinToString(separator = ", ") { it.name } ?: "")
        rowView.setReleaseDate(movie.releaseDate ?: "")
        rowView.setPosterImage(movie.posterPath?.let { MovieImageUrlBuilder().buildPosterUrl(it) } ?: "")
        rowView.setClickListener(View.OnClickListener { onMovieClick(position, rowView) })
    }

    override fun onMovieClick(position: Int, rowView: MovieRowView) {
        if (position < adapterModel.size)
            rowView.onClick(adapterModel[position].id.toLong())
    }

    override fun addFooter() {
        isLoadingOnFooter = true
        // Add a dummy movie object on the end of the list.
        addMovie(Movie())
    }

    override fun removeFooter() {
        isLoadingOnFooter = false
        // Check if there are any items on the list.
        if (!adapterModel.isEmpty()) {
            val pos = adapterModel.size - 1

            removeMovieAtPos(pos)
        }
    }

    override fun addMovie(movie: Movie) {
        adapterModel.add(movie)
        adapterView?.notifyInserted(adapterModel.size - 1)
    }

    override fun addAll(newMovies: List<Movie>) {
        val size = adapterModel.size
        adapterModel.addAll(newMovies)
        adapterView?.notifyRangeInserted(size, newMovies.size)
    }

    override fun removeMovie(movie: Movie) {
        val pos = adapterModel.indexOf(movie)

        if (pos > -1) {
            adapterModel.removeAt(pos)
            adapterView?.notifyRemoved(pos)
        }
    }

    override fun removeMovieAtPos(position: Int) {
        if (position < adapterModel.size) {
            adapterModel.removeAt(position)
            adapterView?.notifyRemoved(position)
        }
    }

    /**
     * This method will clear the AdapterModel.
     */
    override fun removeAll() {
        // Only remove the data if it's not loading anything new.
        if (!isLoading) {
            isLoadingOnFooter = false
            curPage = 1L
            isLoading = false
            isLastPage = false

            while (adapterModel.size > 0)
                removeMovie(adapterModel[0])
        }
    }

    override fun getMoviePos(movie: Movie): Int = adapterModel.indexOf(movie)

    override fun getMoviesCount(): Int = adapterModel.size

    override fun isLoadingOnFooter(): Boolean = isLoadingOnFooter

    // End section.
}