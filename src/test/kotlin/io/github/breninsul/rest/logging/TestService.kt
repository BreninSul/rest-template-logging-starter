package io.github.breninsul.rest.logging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.io.InputStream
open class TestService(
    val restClient: RestClient,
    val restTemplate: RestTemplate

) {


    open fun test():InputStream {
        val response =
            restClient.get().uri("https://img-cdn.pixlr.com/image-generator/history/65bb506dcb310754719cf81f/ede935de-1138-4f66-8ed7-44bd16efc709/medium.webp").exchange { _, rs ->
                rs.body
            }
        return response
    }
    open fun testTemplate():InputStream {
        val response =
            restTemplate
                .
                .get().uri("https://img-cdn.pixlr.com/image-generator/history/65bb506dcb310754719cf81f/ede935de-1138-4f66-8ed7-44bd16efc709/medium.webp").exchange { _, rs ->
                rs.body
            }
        return response
    }

}