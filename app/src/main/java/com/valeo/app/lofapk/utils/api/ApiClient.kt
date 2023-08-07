package com.valeo.app.lofapk.utils.api

import com.valeo.app.lofapk.utils.ApiConstant.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {

    private lateinit var apiService: IApiService

    fun getApiService(): IApiService {

        // Initialize ApiService if not initialized yet
        if (!::apiService.isInitialized) {

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(IApiService::class.java)
        }

        return apiService
    }
}