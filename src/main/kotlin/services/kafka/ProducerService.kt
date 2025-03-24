package com.andrew.greenhouse.auth.services.kafka

import com.andrew.greenhouse.auth.utils.Topic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class ProducerService @Autowired constructor(
    private val kafkaTemplate: KafkaTemplate<String, Any>
){
    fun sendMessage(topic: Topic, message: Any){
        kafkaTemplate.send(topic.topicName, message)
        println("Send message $message in topic ${topic.topicName}")
    }
}