package com.andrew.greenhouse.auth.services.kafka

import com.andrew.greenhouse.auth.utils.Topic
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class ConsumerService @Autowired constructor(){
    @KafkaListener(topics = [Topic.OTP_TOPIC_INCOMING], groupId = "otp-group")
    fun readIntoTopic(record: ConsumerRecord<String, String>) {
        val offset = record.offset()
        val partition = record.partition()
        val topic = record.topic()

        println("Consumed message from topic: $topic, partition: $partition, offset: $offset")
        println("Message content: ${record.value()}")
    }
}

//    @KafkaListener(topics = ["action-otp"], groupId = "otp")
//    fun consume(record: ConsumerRecord<String, String>) {
//        val offset = record.offset()
//        val partition = record.partition()
//        val topic = record.topic()
//
//        println("Consumed message from topic: $topic, partition: $partition, offset: $offset")
//        println("Message content: ${record.value()}")
//    }