package com.arctouch.codechallenge.home

import android.view.View

/**
 * Created by mauker on 07/03/2018.
 * Interface that defines the ViewHolder View methods from the MVP pattern.
 * This represents a single ViewHolder.
 */
interface MovieRowView {

    fun setTitle(title: String)
    fun setGenres(genres: String)
    fun setReleaseDate(releaseDate: String)
    fun setPosterImage(url: String)
    fun setClickListener(listener: View.OnClickListener)

    fun onClick(movieId: Long)
}