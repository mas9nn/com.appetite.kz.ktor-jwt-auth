package com.appetite.kz.plugins

import com.appetite.kz.authenticate
import com.appetite.kz.data.user.UserDataSource
import com.appetite.kz.getSecretInfo
import com.appetite.kz.security.hashing.HashingService
import com.appetite.kz.security.token.TokenConfig
import com.appetite.kz.security.token.TokenService
import com.appetite.kz.signIn
import com.appetite.kz.signUp
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {

    routing {
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(hashingService, userDataSource)
        authenticate()
        getSecretInfo()
    }
}
