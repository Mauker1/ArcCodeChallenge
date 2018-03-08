package com.arctouch.codechallenge.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by mauker on 07/03/2018.
 * Singleton for our API requests.
 */
object ApiManager {

    val api: TmdbApi = Retrofit.Builder()
            .baseUrl(TmdbApi.URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(TmdbApi::class.java)
}