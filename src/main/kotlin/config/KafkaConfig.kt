package com.andrew.greenhouse.auth.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig {
    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> =
        KafkaTemplate(producerFactory)

    @Bean
    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Any>): ConcurrentKafkaListenerContainerFactory<String, Any> =
        ConcurrentKafkaListenerContainerFactory<String, Any>()
            .apply {
                this.consumerFactory = consumerFactory
            }

//    @Bean
//    fun producerFactory(): ProducerFactory<String, Any> =
//        DefaultKafkaProducerFactory(
//            HashMap<String, Any>().apply {
//                this[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
//                this[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
//                this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
//            }
//        )
//
//    @Bean
//    fun kafkaTemplate():KafkaTemplate<String, String> = KafkaTemplate(producerFactory())
 //
//    @Bean
//    fun consumerFactory(): ConsumerFactory<String, Any> =
//        DefaultKafkaConsumerFactory(
//            HashMap<String, Any>().apply {
//                this[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
//                this[ConsumerConfig.GROUP_ID_CONFIG] = "otp-group"
//                this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
//                this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
//            }
//        )
}