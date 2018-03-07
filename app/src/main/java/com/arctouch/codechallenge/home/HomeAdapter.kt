package com.arctouch.codechallenge.home

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arctouch.codechallenge.R
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.util.MovieImageUrlBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.movie_item.view.*

class HomeAdapter(private val movies: MutableList<Movie>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val movieImageUrlBuilder = MovieImageUrlBuilder()

        fun bind(movie: Movie) {
            itemView.titleTextView.text = movie.title
            itemView.genresTextView.text = movie.genres?.joinToString(separator = ", ") { it.name }
            itemView.releaseDateTextView.text = movie.releaseDate

            Glide.with(itemView)
                .load(movie.posterPath?.let { movieImageUrlBuilder.buildPosterUrl(it) })
                .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                .into(itemView.posterImageView)

            itemView.setOnClickListener {
                onClick(movie)
            }
        }

        private fun onClick(movie: Movie) {
            Log.d(LOG_TAG, "onClick()")
        }
    }

    // Dummy ViewHolder for the footer loading progress bar.
    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // Flag that will indicate wheter the footer has been added or not.
    private var isLoadingOnFooter = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ITEM_DEFAULT -> MovieViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false) )
            else -> LoadingViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.loading_item, parent, false) )
        }
    }

    override fun getItemViewType(position: Int): Int =
            if (position == movies.size - 1 && isLoadingOnFooter) ITEM_LOADING else ITEM_DEFAULT

    override fun getItemCount() = movies.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val movie = movies[position]

        // If we decide to add more view types later, add them here.
        when(getItemViewType(position)) {
            ITEM_DEFAULT -> (holder as MovieViewHolder).bind(movie)
            // The ITEM_LOADING will basically show the progress bar, so I'm not binding it here.
        }
    }

    // Adapter helper methods (Sort of a CRUD)

    fun add(movie: Movie) {
        movies.add(movie)
        notifyItemInserted(movies.size - 1)
    }

    fun addAll(newMovies: List<Movie>) {
        for (movie in newMovies) {
            add(movie)
        }
    }

    fun remove(movie: Movie) {
        val pos = movies.indexOf(movie)

        if (pos > -1) {
            movies.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }

    fun removeAtPos(position: Int) {
        if (position < movies.size) {
            movies.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clearAll() {
        isLoadingOnFooter = false

        while (movies.size > 0) {
            remove(movies[0])
        }
    }

    fun addFooter() {
        isLoadingOnFooter = true

        // Add a dummy movie object on the end of the list.
        add(Movie())
    }

    fun removeFooter() {
        isLoadingOnFooter = false

        // Check if there are any items on the list.
        if (!movies.isEmpty()) {
            val pos = movies.size - 1

            removeAtPos(pos)
        }
    }

    companion object {
        val LOG_TAG = HomeAdapter::class.java.simpleName

        private const val ITEM_DEFAULT = 0
        private const val ITEM_LOADING = 1
    }
}
