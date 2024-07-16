package com.valeo.app.lofapk.utils.api

import android.annotation.SuppressLint
import android.util.Log
import com.valeo.app.lofapk.utils.ApiConstant.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {

    private lateinit var apiService: IApiService

    @SuppressLint("LogNotTimber")
    fun getApiService(): IApiService {

        // Initialize ApiService if not initialized yet
        if (!::apiService.isInitialized) {

            Log.i("APP_DEBUG", "ApiService initialize on base_url $BASE_URL")

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(IApiService::class.java)
        }

        return apiService
    }
}