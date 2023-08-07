package com.valeo.app.lofapk.utils.api

import com.valeo.app.lofapk.model.Location
import com.valeo.app.lofapk.utils.ApiConstant.Companion.POSTS_URL
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface IOrdsApiService {
    @Headers(
        "Content-Type: application/json"
    )
    /*
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer $ACCESS_TOKEN"
    )
     */
    @POST(POSTS_URL)
    fun postLocation(@Header("Authorization") token: String, @Body ofData: Location): Call<Location>
}