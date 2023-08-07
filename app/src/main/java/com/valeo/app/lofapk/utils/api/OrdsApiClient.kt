package com.valeo.app.lofapk.utils.api

import android.util.Log
import com.valeo.app.lofapk.model.Location
import com.valeo.app.lofapk.model.OFinfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdsApiClient {

    fun addLocation(token: String, ofData: OFinfo, onResult: (Location?) -> Unit) {

        val retrofit = ServiceBuilder.buildService(IOrdsApiService::class.java)
        val list = List(1) { ofData }
        val locationData: Location = Location(list)

        Log.d("APP_DEBUG", "Location to be POSTED: $locationData")

        retrofit.postLocation(token = token, ofData = locationData)
            .enqueue(object : Callback<Location> {
                override fun onFailure(call: Call<Location>, t: Throwable) {
                    onResult(null)
                }
                /*
                override fun onResponse( call: Call<Location>, response: Response<Location>) {
                    val addedLocation = response.body()
                    onResult(addedLocation)
                }
                */
                override fun onResponse(
                    call: Call<Location>,
                    response: Response<Location>
                ) {
                    val addedLocation = response.body()
                    onResult(addedLocation)
                }
            }
            )

    }
}