package com.example.catapi.retrofit

import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.Retrofit
import retrofit2.Call

import retrofit2.http.GET
import retrofit2.http.Query


class ClientApi {
    companion object {
        fun getClient(): Retrofit? {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.thecatapi.com/v1/images/")
                    .build()
            }
            return retrofit
        }


        private var retrofit: Retrofit? = null
    }
}

interface ApiInterface {
    @GET("search?api_key=4c880356-6ac8-4074-9563-88661e06bd41")
    fun getCats(
        @Query("limit") amountOfCats: String,
    ): Call<MutableList<Cat>>
}