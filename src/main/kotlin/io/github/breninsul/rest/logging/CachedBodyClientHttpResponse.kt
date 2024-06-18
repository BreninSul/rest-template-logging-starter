package io.github.breninsul.rest.logging

import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayInputStream
import java.io.InputStream

open class CachedBodyClientHttpResponse(val delegate: ClientHttpResponse) : ClientHttpResponse by delegate {
    open val bodyByteArray = delegate.body.readAllBytes()
    override fun getBody(): InputStream {
        return ByteArrayInputStream(bodyByteArray)
    }
}