package com.andrew.greenhouse.auth.utils

import com.andrew.greenhouse.auth.services.kafka.ProducerService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import greenhouse_api.greenhouse_log.LogData
import greenhouse_api.greenhouse_log.MessageType
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LogUtil @Autowired constructor(
    private val producerService: ProducerService
){
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun log(logger: Logger,
            messageType: MessageType,
            logData: LogData
    ) {
        val payload = objectMapper.writeValueAsString(logData)
        val logMessage = "${logData.sourceApp}: ${logData.message} Message payload: ${logData.messagePayload}"
        when (logData.messageType) {
            MessageType.INFO ->
                logger.info(logMessage)
            MessageType.WARN ->
                logger.warn(logMessage)
            MessageType.ERROR ->
                logger.error(logMessage)
        }

        producerService.sendMessage(Topic.GREENHOUSE_LOG_OUTGOING, payload)
    }
}