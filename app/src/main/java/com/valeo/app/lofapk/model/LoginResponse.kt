package com.valeo.app.lofapk.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(

    @SerializedName("access_token")
    var authToken: String,

    @SerializedName("token_type")
    var typeToken: String,

    @SerializedName("expires_in")
    var expiresIn: Int
)
