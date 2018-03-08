package com.arctouch.codechallenge.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import br.com.mauker.materialsearchview.MaterialSearchView
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.api.ApiManager
import kotlinx.android.synthetic.main.home_activity.*


class HomeActivity : AppCompatActivity(), HomeView {

    companion object {
        val LOG_TAG: String = HomeActivity::class.java.simpleName
    }

    private var presenter: HomePresenter? = null
    private val adapter = HomeAdapter()

    // Activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        setSupportActionBar(toolbar)

        setupPresenter()
        setupRV()
        setupSearchView()

        if (savedInstanceState == null) {
            // Load the genres. This method will load the movies afterwards.
            presenter?.loadGenres()
        }
    }

    override fun onDestroy() {
        presenter?.onDestroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.action_search -> {
                searchView.openSearch()
                true
            }
            R.id.action_refresh -> {
                presenter?.removeAll()
                presenter?.loadMovies()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Using this method to get the audio query for the search view.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == Activity.RESULT_OK) {
            val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null && matches.size > 0) {
                val searchWrd = matches[0]
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false)
                }
            }
        }
        else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (searchView.isOpen)
            searchView.closeSearch()
        else
            super.onBackPressed()
    }

    /**
     * Save the presenter instance to avoid downloading the data again.
     */
    override fun onRetainCustomNonConfigurationInstance(): Any {
        val myPresenter = presenter
        return if (presenter != null) myPresenter as HomePresenter else super.onRetainCustomNonConfigurationInstance()
    }

    // End section

    /**
     * Load the presenter instance if it was saved before. Or create a new one.
     */
    private fun setupPresenter() {
        presenter = lastCustomNonConfigurationInstance as HomePresenter?

        if (presenter == null) {
            presenter = HomePresenterImpl(this, adapter, ApiManager)
        }
        else {
            presenter?.attachView(this)
            presenter?.attachAdapterView(adapter)
            presenter?.onRestore()
        }

        adapter.presenter = presenter as HomePresenter
    }

    /**
     * Setup the Recycler View, adding the LayoutManager and setting the scroll listener.
     */
    private fun setupRV() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
        // Listen to the scrolls, and make the magic happen. Infinite scroll.
        recyclerView.addOnScrollListener(presenter?.getRvScrollListener(layoutManager))
    }

    private fun setupSearchView() {
        searchView.setBackgroundColor(ContextCompat.getColor(this, R.color.searchViewTint))
        searchView.setOnItemClickListener { _, _, pos, _ -> searchView.setQuery(searchView.getSuggestionAtPosition(pos), false) }
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d(LOG_TAG, "Query: $query")
                presenter?.removeAll()
                presenter?.searchMovie(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean = false
        })
    }

    /**
     * HomeView methods
     */
    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    override fun postOnRv(action: Runnable) {
        recyclerView.post(action)
    }

    override fun showErrorMessage(@StringRes message: Int, listener: View.OnClickListener?, @StringRes resId: Int) {
        val snack = Snackbar.make(homeRoot, message, Snackbar.LENGTH_LONG)

        if (listener != null) {
            snack.setAction(getString(R.string.action_try_again).toUpperCase(), listener)
        }

        snack.show()
    }

    // End section
}
