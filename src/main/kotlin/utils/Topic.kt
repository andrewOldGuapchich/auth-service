package com.andrew.greenhouse.auth.utils

enum class Topic(val topicName: String) {
    OTP_TOPIC_OUTGOING("action-otp-outgoing"),
    GREENHOUSE_LOG_OUTGOING("greenhouse-log-outgoing"),
    CLIENT_TOPIC("client-register");

    companion object {
        const val OTP_TOPIC_INCOMING = "otp-topic-incoming"
    }
}