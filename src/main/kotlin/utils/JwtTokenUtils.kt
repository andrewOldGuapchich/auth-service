package com.andrew.greenhouse.auth.utils

import greenhouse_api.utills.TokenState
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtTokenUtils(
    @Value("\${jwt.token.secret.access}")
    private val jwtAccessSecret: String,
    @Value("\${jwt.token.duration}")
    private val tokenDuration: Long
) {
    private val secretKey = Base64.getDecoder().decode(jwtAccessSecret)
    fun generateAccessToken(username: String): String =
        Jwts.builder()
            .setSubject(username)
            .claim("role", "AUTH_CLIENT")
            .claim("privileges", listOf("WRITE, READ"))
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + tokenDuration * 60 * 60))
            .signWith(Keys.hmacShaKeyFor(secretKey), SignatureAlgorithm.HS256)
            .compact()

    fun extractUsername(token: String): String? =
        getClaims(token).subject ?: throw Exception("Incorrect token!")

    fun isTokenExpired(token: String) = getClaims(token).expiration.before(Date())

    fun validateToken(token: String, userDetails: UserDetails): TokenState =
        if(extractUsername(token) == userDetails.username && !isTokenExpired(token))
            TokenState.VALID
        else TokenState.INVALID

    private fun getClaims(token: String) =
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secretKey))
            .build()
            .parseClaimsJwt(token)
            .body
}