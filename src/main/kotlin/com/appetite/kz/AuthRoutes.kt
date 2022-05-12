package com.appetite.kz

import com.appetite.kz.data.requests.AuthRequest
import com.appetite.kz.data.responses.AuthResponse
import com.appetite.kz.data.user.User
import com.appetite.kz.data.user.UserDataSource
import com.appetite.kz.security.hashing.HashingService
import com.appetite.kz.security.hashing.SaltedHash
import com.appetite.kz.security.token.TokenClaim
import com.appetite.kz.security.token.TokenConfig
import com.appetite.kz.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    hashingService: HashingService, userDataSource: UserDataSource
) {
    post("signup") {
        val request = call.receiveOrNull<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldBlank = request.username.isBlank() || request.password.isBlank()
        val isPwTooSHort = request.password.length < 8
        if (areFieldBlank || isPwTooSHort) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }
        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(username = request.username, password = saltedHash.hash, salt = saltedHash.salt)
        val wasAcknowledged = userDataSource.insertUser(user)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        call.respond(HttpStatusCode.OK)
    }
}

fun Route.signIn(
    userDataSource: UserDataSource, hashingService: HashingService, tokenService: TokenService, tokenConfig: TokenConfig
) {
    post("signin") {
        val request = call.receiveOrNull<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password, saltedHash = SaltedHash(
                hash = user.password, salt = user.salt
            )
        )
        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(config = tokenConfig, TokenClaim(name = "userId", value = user.id.toString()))

        call.respond(HttpStatusCode.OK, message = AuthResponse(token = token))
    }
}

fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "UserId is $userId")
        }
    }
}