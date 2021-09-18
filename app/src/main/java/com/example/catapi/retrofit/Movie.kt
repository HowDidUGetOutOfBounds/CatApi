package com.example.catapi.retrofit

import com.google.gson.annotations.SerializedName


class Movie {

    @SerializedName("title")
    private var title: String? = null

    @SerializedName("image")
    private var imageUrl: String? = null

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun getImageUrl(): String? {
        return imageUrl
    }

    fun setImageUrl(imageUrl: String?) {
        this.imageUrl = imageUrl
    }
}