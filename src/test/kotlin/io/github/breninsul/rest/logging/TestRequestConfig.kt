package io.github.breninsul.rest.logging

import io.github.breninsul.logging.HttpLogSettings
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration
@SpringBootConfiguration
open class TestRequestConfig {

    @Bean("restClient")
    fun googleMapsRestClient(configurer: ObjectProvider<RestClientBuilderConfigurer>): RestClient {
        var connectTimeout: Duration = Duration.ofSeconds(10)
        var readTimeout: Duration = Duration.ofSeconds(120)
        val properties = RestTemplateLoggerProperties(response = HttpLogSettings(bodyIncluded = false))
        val simpleClientHttpRequestFactory = HttpComponentsClientHttpRequestFactory()
        simpleClientHttpRequestFactory.setConnectTimeout(connectTimeout.toMillis().toInt())
        simpleClientHttpRequestFactory.setConnectionRequestTimeout(readTimeout.toMillis().toInt())
        val factory = BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory)
        val builder = RestClient.builder()

        builder.requestFactory(factory)
        builder.requestInterceptor(RestTemplateLoggerConfiguration().registerRestTemplateLoggingInterceptor(properties))

        val configurers = configurer.toList()
        configurers.forEach { it.configure(builder) }
        val client = builder.build()
        return client
    }

    @Bean
    fun testService(restClient: RestClient): TestService {
        return TestService(restClient)
    }
}