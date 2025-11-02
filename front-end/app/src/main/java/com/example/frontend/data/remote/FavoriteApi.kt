package com.example.frontend.data.remote

import com.example.frontend.data.models.favorites.FavoriteRequest
import com.example.frontend.data.models.favorites.FavoriteResponse
import com.example.frontend.data.models.home.GetTracksResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteApi {
    @POST("favorites/delete")
    suspend fun deleteFavorite(@Body request: FavoriteRequest) : Response<ResponseBody>

    @POST("favorites/add")
    suspend fun addFavorite(@Body request: FavoriteRequest) : Response<FavoriteResponse>

    @GET("favorites")
    suspend fun getFavorites() : List<GetTracksResponse>

    @GET("favorites/check/{id}")
    suspend fun isFavorite(@Path("id") id: String) : Boolean

}