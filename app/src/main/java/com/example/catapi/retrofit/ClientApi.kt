package com.example.catapi.retrofit

import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.Retrofit
import retrofit2.Call

import retrofit2.http.GET





class ClientApi {
    companion object
    {
        fun getClient(): Retrofit? {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://velmm.com/apis/")
                    .build()
            }
            return retrofit
        }


        private var retrofit: Retrofit? = null
    }
}

interface ApiInterface {
    @GET("volley_array.json")
    fun getMovies(): Call<MutableList<Movie>>
}