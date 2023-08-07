package com.valeo.app.lofapk.model

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("data") var data: List<OFinfo>
)
