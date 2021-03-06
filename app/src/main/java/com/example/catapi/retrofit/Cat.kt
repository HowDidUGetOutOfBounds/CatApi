package com.example.catapi.retrofit

import com.google.gson.annotations.SerializedName

data class Cat (

    @SerializedName("id") val id : String,

    @SerializedName("url") val url : String,

    @SerializedName("width") val width : Int,

    @SerializedName("height") val height : Int


) {
    constructor() : this("0", "0", 0, 0) // empty constructor to add footer
}