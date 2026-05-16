package com.example.soccertshirts_app.data.model

import com.google.gson.annotations.SerializedName

data class CountryResponse(
    @SerializedName("response") val response: List<CountryApiData>
)

data class CountryApiData(
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?,
    @SerializedName("flag") val flag: String?
)

data class TeamResponse(
    @SerializedName("response") val response: List<TeamApiData>
)

data class TeamApiData(
    @SerializedName("team") val team: TeamInfo
)

data class TeamInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("logo") val logo: String?
)
