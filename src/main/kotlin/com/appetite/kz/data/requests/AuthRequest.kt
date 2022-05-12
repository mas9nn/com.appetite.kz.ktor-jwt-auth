package com.appetite.kz.data.requests

@kotlinx.serialization.Serializable
data class AuthRequest(
    val username: String,
    val password: String
)
