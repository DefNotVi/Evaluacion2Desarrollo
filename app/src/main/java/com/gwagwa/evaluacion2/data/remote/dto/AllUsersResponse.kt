package com.gwagwa.evaluacion2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la respuesta de lista de usuarios del endpoint /auth/users
 */
data class AllUsersResponse(
    @SerializedName("success")
    val success: Boolean,


    @SerializedName("data")
    val userList: List<UserDto>
)