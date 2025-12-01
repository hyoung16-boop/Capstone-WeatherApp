package com.example.weatherproject.network

import com.google.gson.annotations.SerializedName

data class CctvResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("cctv_name")
    val cctvName: String,

    @SerializedName("cctv_url")
    val cctvUrl: String,

    @SerializedName("cctv_type")
    val cctvType: String,

    @SerializedName("cctv_lat")
    val cctvLat: String,

    @SerializedName("cctv_lng")
    val cctvLng: String
)