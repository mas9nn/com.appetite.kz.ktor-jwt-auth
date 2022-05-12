package com.appetite.kz.security.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)
