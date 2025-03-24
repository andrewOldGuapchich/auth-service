package com.andrew.greenhouse.auth.services

import com.andrew.greenhouse.auth.repositories.OtpArchiveRepository
import com.andrew.greenhouse.auth.utils.RedisService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import services.OtpService
import kotlin.random.Random

@Service
class OtpService @Autowired constructor(
    private val otpArchiveRepository: OtpArchiveRepository,
    private val otpRedisService: RedisService.OtpRedisService
) : OtpService {
    override fun findOtp(condition: String): String? {
        return otpRedisService.get(condition)
    }

    override fun deleteOtp(condition: String){
        otpRedisService.delete(condition)
    }
    override fun generateOtp(): Number =
        Random.nextInt(100000, 1000000)

    override fun saveOtp(value: Map<String, String>): Number {
        otpRedisService.save(value)
        return 1
    }
}