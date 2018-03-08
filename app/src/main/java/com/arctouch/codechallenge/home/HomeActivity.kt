package com.arctouch.codechallenge.home

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.api.ApiManager
import kotlinx.android.synthetic.main.home_activity.*


class HomeActivity : AppCompatActivity(), HomeView {

    companion object {
        val LOG_TAG = HomeActivity::class.java.simpleName
    }

    private lateinit var presenter: HomePresenter
    private val adapter = HomeAdapter()

    // Activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        // TODO - Check if this will work. The adapter has a cyclic reference to the presenter.
        presenter = HomePresenterImpl(this, adapter, ApiManager)

        adapter.presenter = presenter

        setupRV()

        // TODO - Check if there's a saved instance.

        // Load the movies for the first time (First page)
        presenter.loadMovies()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    // TODO - Issue #8: Save Activity state to avoid multiple downloads.

    // End section

    /**
     * Setup the Recycler View, adding the LayoutManager and setting the scroll listener.
     */
    private fun setupRV() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
        // Listen to the scrolls, and make the magic happen. Infinite scroll.
        recyclerView.addOnScrollListener(presenter.getRvScrollListener(layoutManager))
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

    override fun showErrorMessage(message: String, listener: View.OnClickListener?, @StringRes resId: Int) {
        val snack = Snackbar.make(homeRoot, message, Snackbar.LENGTH_LONG)

        if (listener != null) {
            snack.setAction(resId, listener)
        }

        snack.show()
    }

    // End section
}
