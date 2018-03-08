package com.arctouch.codechallenge.home

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.detail.DetailActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.movie_item.view.*

class HomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), HomeAdapterView {

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), MovieRowView {
        override fun setTitle(title: String) {
            itemView.titleTextView.text = title
        }

        override fun setGenres(genres: String) {
            itemView.genresTextView.text = genres
        }

        override fun setReleaseDate(releaseDate: String) {
            itemView.releaseDateTextView.text = releaseDate
        }

        override fun setPosterImage(url: String) {
            Glide.with(itemView)
                    .load(url)
                    .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                    .into(itemView.posterImageView)
        }

        override fun setClickListener(listener: View.OnClickListener) = itemView.setOnClickListener(listener)

        override fun onClick(movieId: Long) {
            Log.d(LOG_TAG, "onClick()")
            val context = itemView.context
            val it = Intent(context, DetailActivity::class.java)

            it.putExtra(DetailActivity.EXTRA_MOVIE_ID, movieId)

            context.startActivity(it)
        }
    }

    // Dummy ViewHolder for the footer loading progress bar.
    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    lateinit var presenter: HomePresenter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ITEM_DEFAULT -> MovieViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false) )
            else -> LoadingViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.loading_item, parent, false) )
        }
    }

    override fun getItemViewType(position: Int): Int =
            if (position == presenter.getMoviesCount() - 1 && presenter.isLoadingOnFooter()) ITEM_LOADING else ITEM_DEFAULT

    override fun getItemCount() = presenter.getMoviesCount()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // If we decide to add more view types later, add them here.
        when(getItemViewType(position)) {
            ITEM_DEFAULT -> presenter.onBindMovie(position, (holder as MovieViewHolder))
            // The ITEM_LOADING will basically show the progress bar, so I'm not binding it here.
        }
    }

    /**
     * HomeAdapterView methods
     */

    override fun notifyInserted(position: Int) = notifyItemInserted(position)

    override fun notifyRangeInserted(startPos: Int, count: Int) = notifyItemRangeInserted(startPos, count)

    override fun notifyRemoved(position: Int) = notifyItemRemoved(position)

    override fun notifyRemovedRage(startPos: Int, count: Int) = notifyItemRangeRemoved(startPos, count)

    // End section

    companion object {
        val LOG_TAG: String = HomeAdapter::class.java.simpleName

        private const val ITEM_DEFAULT = 0
        private const val ITEM_LOADING = 1
    }
}
