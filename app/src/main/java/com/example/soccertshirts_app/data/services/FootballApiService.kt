package com.example.soccertshirts_app.data.services

import com.example.soccertshirts_app.data.model.CountryResponse
import com.example.soccertshirts_app.data.model.TeamResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FootballApiService {

    @GET("countries")
    suspend fun getCountries(
        @Header("x-apisports-key") apiKey: String = "3b753dfe2b240ca1e6e87f6781861d68"
    ): CountryResponse

    @GET("teams")
    suspend fun getTeams(
        @Query("country") country: String,
        @Header("x-apisports-key") apiKey: String = "3b753dfe2b240ca1e6e87f6781861d68"
    ): TeamResponse

    companion object {
        private const val BASE_URL = "https://v3.football.api-sports.io/"

        fun create(): FootballApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FootballApiService::class.java)
        }
    }
}
