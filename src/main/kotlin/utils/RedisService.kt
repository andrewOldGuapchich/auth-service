package com.andrew.greenhouse.auth.utils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

sealed interface RedisService {
    val redisTemplate: StringRedisTemplate
    fun save(value: Map<String, String>)
    fun get(key: String): String?
    fun delete(key: String)

    @Service
    class OtpRedisService @Autowired constructor(
        override val redisTemplate: StringRedisTemplate
    ) : RedisService {
        override fun save(value: Map<String, String>) {
            value.forEach { (key, otp) ->
                redisTemplate.opsForValue().set("otp:$key", otp, Duration.ofMinutes(10))
            }
        }

        override fun get(key: String): String? =
            redisTemplate.opsForValue().get("otp:$key")

        override fun delete(key: String) {
            redisTemplate.delete("otp:$key")
        }
    }
}