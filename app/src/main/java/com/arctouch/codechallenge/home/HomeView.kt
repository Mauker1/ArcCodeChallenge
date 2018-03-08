package com.arctouch.codechallenge.home

import android.support.annotation.StringRes
import android.view.View

/**
 * Created by mauker on 07/03/2018.
 * Interface that defines the View methods from the MVP pattern for the HomeActivity.
 */
interface HomeView {

    fun showProgress()
    fun hideProgress()

    fun postOnRv(action: Runnable)

//    fun loadMovies()

    // Displays an error on a Snackbar, and gives the option to retry if the listener isn't null
    fun showErrorMessage(@StringRes message: Int,
                         listener: View.OnClickListener? = null,
                         @StringRes resId: Int = 0)
}