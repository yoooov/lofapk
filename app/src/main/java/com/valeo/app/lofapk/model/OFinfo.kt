package com.valeo.app.lofapk.model

import com.google.gson.annotations.SerializedName

data class OFinfo(
    @SerializedName("UNIQUEID") var uniqueId: String,
    @SerializedName("LOCATION") var location: String
)
