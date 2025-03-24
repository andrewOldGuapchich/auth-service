package com.andrew.greenhouse.auth.utils

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

typealias GetRequestBuilder<T> = GetRequest<T>.() -> Unit

@Service
class RestHandler @Autowired constructor(
    private val webClient: WebClient,
    @Value("\${rest.base-url}") private val baseUrl: String
) {
    private val logger = LoggerFactory.getLogger(RestHandler::class.java)

   fun <T> get(getRequestBuilder: GetRequestBuilder<T>): T?{
       val getRequest = GetRequest<T>().apply(getRequestBuilder)

       logger.info(getRequest.toString())
       return try {
           webClient
               .get()
               .uri { uriBuilder ->
                   val uri = uriBuilder.scheme("http")
                       .host(baseUrl)
                       .port(getRequest.port)
                       .path(getRequest.endpoint)
                       .apply {
                           getRequest.params.forEach { (key, value) ->
                               queryParam(key, value.toString())
                           }
                       }
                       .build()
                   logger.info("Final URI: $uri")
                   uri
               }
               .headers { httpHeaders ->
                   getRequest.headers.forEach { (key, value) ->
                       httpHeaders.add(key, value.toString())
                   }
               }
               .retrieve()
               .onStatus({status -> status.is5xxServerError || status.is4xxClientError}) {
                   throw RuntimeException("Internal server error! ${it.statusCode()}")
               }
               .bodyToMono(getRequest.bodyClass!!)
               .block()
       } catch (e: Exception) {
           logger.error("Internal server error! ${e.message}")
           null
       }
   }
}

data class GetRequest<T>(
    var bodyClass: Class<T>? = null,
    var endpoint: String = "",
    var port: Int = 1125,
    var params: MutableMap<String, Any> = mutableMapOf(),
    var headers: MutableMap<String, Any> = mutableMapOf()
)

