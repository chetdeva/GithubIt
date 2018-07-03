package com.chetdeva.githubit.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Github API communication setup via Retrofit.
 */
interface GithubApi {
    /**
     * Get repos ordered by stars.
     */
    @GET("search/users?sort=followers")
    fun searchUsers(@Query("q") query: String,
                    @Query("page") page: Int,
                    @Query("per_page") perPage: Int): Call<UsersSearchResponse>


    companion object {
        private const val BASE_URL = "https://api.github.com/"

        fun create(): GithubApi {
            val logger = HttpLoggingInterceptor()
            logger.level = Level.BODY

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(GithubApi::class.java)
        }
    }
}