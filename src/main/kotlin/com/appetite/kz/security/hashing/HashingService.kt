package com.appetite.kz.security.hashing

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

interface HashingService {
    fun generateSaltedHash(value: String, saltLength: Int = 32): SaltedHash

    fun verify(value: String, saltedHash: SaltedHash): Boolean

    class Base() : HashingService {
        override fun generateSaltedHash(value: String, saltLength: Int): SaltedHash {
            val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
            val saltAsHex = Hex.encodeHexString(salt)
            val hash = DigestUtils.sha256Hex("$saltAsHex$value")
            return SaltedHash(
                hash = hash,
                salt = saltAsHex
            )
        }

        override fun verify(value: String, saltedHash: SaltedHash): Boolean {
            val res = DigestUtils.sha256Hex("${saltedHash.salt}$value")
            return res == saltedHash.hash
        }
    }
}