package com.arctouch.codechallenge.home

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.arctouch.codechallenge.BuildConfig

/**
 * Created by mauker on 07/03/2018.
 * Listener that will be used to check if our RecyclerView has reached the end of the scroll, and
 * load more items if there are any pages to load.
 */
abstract class RvScrollListener(private val layoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        // If it's not already loading, and it isn't on the last page already,
        // try to get more items.
        if (!isLoading() && !isLastPage()) {
            if ( (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                && totalItemCount >= 0 ) {
                if (BuildConfig.DEBUG) Log.d("TAG", "end of scroll, trying to fetch more items.")
                loadMoreItems()
            }
        }
    }

    abstract fun isLoading() : Boolean
    abstract fun isLastPage() : Boolean
    abstract fun getTotalPageCount() : Int
    abstract fun loadMoreItems()
}
