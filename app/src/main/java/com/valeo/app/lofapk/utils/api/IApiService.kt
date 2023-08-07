package com.valeo.app.lofapk.utils.api

import com.valeo.app.lofapk.model.LoginResponse
import com.valeo.app.lofapk.utils.ApiConstant.Companion.LOGIN_URL
import retrofit2.Call
import retrofit2.http.*

interface IApiService {

    /*
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Authorization: Basic $CLIENT_B64"
    )
     */
    @Headers(
        "Content-Type: application/x-www-form-urlencoded"
    )

    @FormUrlEncoded
    @POST(LOGIN_URL)
    //@FormUrlEncoded
    //fun login(@Body request: String): Call<LoginResponse>
    fun login(@Header("Authorization") auth: String,
              @Field(("grant_type")) grantType: String): Call<LoginResponse>
}