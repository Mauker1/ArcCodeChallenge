package com.arctouch.codechallenge.home

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.api.ApiManager.api
import com.arctouch.codechallenge.api.TmdbApi
import com.arctouch.codechallenge.data.Cache
import com.arctouch.codechallenge.model.UpcomingMoviesResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.home_activity.*


class HomeActivity : AppCompatActivity() {

    companion object {
        val LOG_TAG = HomeActivity::class.java.simpleName
    }

    // Always start from the first page.
    private var curPage = 1L
    // The API docs says that the limit of pagination is 1000.
    private var maxPages = 1000
    // The number of pages returned by the API.
    private var numPages = 0

    private var isLoading = false
    private var isLastPage = false

    private val adapter: HomeAdapter by lazy {
        HomeAdapter(ArrayList())
    }

    // Activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        setupRV()
        // Load the movies for the first time (First page)
        loadMovies()
    }

    // TODO - Issue #8: Save Activity state to avoid multiple downloads.

    //end section


    /**
     * Setup the Recycler View, adding the LayoutManager and setting the scroll listener.
     */
    private fun setupRV() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
        // Listen to the scrolls, and make the magic happen. Infinite scroll.
        recyclerView.addOnScrollListener(object: RvScrollListener(layoutManager) {
            override fun isLoading(): Boolean = isLoading
            override fun isLastPage(): Boolean = isLastPage
            override fun getTotalPageCount(): Int = if (numPages > 0) numPages else maxPages

            override fun loadMoreItems() {
                loadMovies()
            }
        })
    }

    /**
     * Method that will fetch the movies from the API, and display the results on the RV.
     */
    private fun loadMovies() {
        if (curPage == 1L)
            progressBar.visibility = View.VISIBLE
        else if (curPage > 1L)
            recyclerView.post { adapter.addFooter()  }

        if (!isLastPage) {
            isLoading = true
            api.upcomingMovies(TmdbApi.API_KEY, TmdbApi.DEFAULT_LANGUAGE, curPage, TmdbApi.DEFAULT_REGION)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .retry(3)
                    .onErrorReturn {
                        it.stackTrace
                        UpcomingMoviesResponse(1, ArrayList(), 1, 0)
                    }
                    .subscribe {
                        val moviesWithGenres = it.results.map { movie ->
                            movie.copy(genres = Cache.genres.filter { movie.genreIds?.contains(it.id) == true })
                        }

                        // Check if we're past the first page. Which means there's a footer to be removed.
                        if (curPage > 1L) adapter.removeFooter()

                        // Check if it's the last page
                        isLastPage = it.totalPages <= it.page
                        // Increment the page counter if it's not the last page yet.
                        if (!isLastPage)
                            curPage = it.page + 1L
                        // Insert the results on the RV.
                        adapter.addAll(moviesWithGenres)

                        isLoading = false
                        progressBar.visibility = View.GONE
                    }
        }
    }
}
