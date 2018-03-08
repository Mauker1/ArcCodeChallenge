package com.arctouch.codechallenge.api

import com.arctouch.codechallenge.model.GenreResponse
import com.arctouch.codechallenge.model.Movie
import com.arctouch.codechallenge.model.MoviesResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface TmdbApi {

    // TODO - Move the URL and the API_KEY to BuildConfig or equivalent to address different build types.
    companion object {
        const val URL = "https://api.themoviedb.org/3/"
        const val API_KEY = "1f54bd990f1cdfb230adb312546d765d"
        val DEFAULT_LANGUAGE: String = Locale.getDefault().toString()
        val DEFAULT_REGION: String = Locale.getDefault().country
    }

    @GET("genre/movie/list")
    fun genres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Observable<GenreResponse>

    @GET("movie/upcoming")
    fun upcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Long,
        @Query("region") region: String
    ): Observable<MoviesResponse>

    @GET("movie/{id}")
    fun movie(
        @Path("id") id: Long,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Observable<Movie>

    @GET("search/movie")
    fun searchMovie(
            @Query("api_key") apiKey: String,
            @Query("language") language: String,
            @Query("query") query: String
    ): Observable<MoviesResponse>
}
