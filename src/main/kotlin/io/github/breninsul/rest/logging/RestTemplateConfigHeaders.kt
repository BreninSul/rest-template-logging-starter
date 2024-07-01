package io.github.breninsul.rest.logging

import io.github.breninsul.rest.logging.RestTemplateConfigHeaders.TECHNICAL_HEADERS
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest

/**
 * The RestTemplateConfigHeaders class defines constants for the technical headers used in the RestTemplate configuration.
 * These headers can be set in the HttpRequest headers to control various logging behaviors.
 */
object RestTemplateConfigHeaders {
    const val LOG_REQUEST_URI: String = "LOG_REQUEST_URI_TECHNICAL_HEADER"
    const val LOG_REQUEST_HEADERS: String = "LOG_REQUEST_HEADERS_TECHNICAL_HEADER"
    const val LOG_REQUEST_BODY: String = "LOG_REQUEST_BODY_TECHNICAL_HEADER"
    const val LOG_REQUEST_TOOK_TIME: String = "LOG_REQUEST_TOOK_TIME_TECHNICAL_HEADER"

    const val LOG_RESPONSE_URI: String = "LOG_RESPONSE_URI_TECHNICAL_HEADER"
    const val LOG_RESPONSE_HEADERS: String = "LOG_RESPONSE_HEADERS_TECHNICAL_HEADER"
    const val LOG_RESPONSE_BODY: String = "LOG_RESPONSE_BODY_TECHNICAL_HEADER"
    const val LOG_RESPONSE_TOOK_TIME: String = "LOG_RESPONSE_TOOK_TIME_TECHNICAL_HEADER"
    //You can add your headers here
    val TECHNICAL_HEADERS = mutableListOf(LOG_REQUEST_URI, LOG_REQUEST_HEADERS, LOG_REQUEST_BODY, LOG_REQUEST_TOOK_TIME, LOG_RESPONSE_URI, LOG_RESPONSE_HEADERS, LOG_RESPONSE_BODY, LOG_RESPONSE_TOOK_TIME)
}

/**
 * Retrieves a string representation of the HttpHeaders object, excluding technical headers.
 *
 * @return A string representing the headers, with each header key-value pair separated by ":" and each header separated by ";".
 */
fun HttpHeaders.getHeadersString(maskingHeaders:List<String>) =
    (this.asSequence()
        .filter { h->!TECHNICAL_HEADERS.any { th->th.contentEquals(h.key) } }
        .map { "${it.key}:${if(maskingHeaders.any { m->m.contentEquals(it.key,true) }) "<MASKED>" else it.value.joinToString(",")}" }
        .joinToString(";"))
/**
 * Returns a list of technical headers present in the HTTP request.
 *
 * @return A list of pairs representing the technical headers, where each pair consists of a header name and its corresponding value.
 */
fun HttpRequest.getTechnicalHeaders(): List<Pair<String, String>> = TECHNICAL_HEADERS.map { it to this.headers.getFirst(it) }.filter { it.second != null } as List<Pair<String, String>>

/**
 * Removes the technical headers from the HTTP request headers.
 * Technical headers are headers that are used for internal purposes and
 * are not relevant for logging or further processing.
 *
 * @return A list of pairs representing the technical headers that were removed from the request.
 * Each pair consists of a header name and its corresponding value.
 * @see HttpRequest
 */
fun HttpRequest.removeTechnicalHeaders(): List<Pair<String, String>> {
   return TECHNICAL_HEADERS.map {it to  headers.remove(it)?.firstOrNull() }.filter { it.second!=null } as List<Pair<String, String>>
}

/**
 * Sets the technical headers in the current HttpRequest.
 *
 * @param list The list of header pairs to be set. Each pair consists of a header name and its corresponding value.
 */
fun HttpRequest.setTechnicalHeaders(list: List<Pair<String, String>>) {
    list.forEach { this.headers[it.first] = listOf(it.second) }
}

/**
 * Retrieves the value of the "LOG_REQUEST_URI" header from the current HTTP request headers and
 * converts it to a Boolean.
 *
 * @return The Boolean value of the "LOG_REQUEST_URI" header, or null if the header is not present
 * or cannot be converted to a Boolean.
 */
fun HttpRequest.logRequestUri(): Boolean? = headers.getFirst(RestTemplateConfigHeaders.LOG_REQUEST_URI)?.toBoolean()

/**
 * Retrieves the value of the "LOG_REQUEST_HEADERS" header from the HttpRequest headers.
 *
 * @return The value of the "LOG_REQUEST_HEADERS" header as a Boolean, or null if the header is not present or cannot be parsed as a Boolean.
 */
fun HttpRequest.logRequestHeaders(): Boolean? = headers.getFirst(RestTemplateConfigHeaders.LOG_REQUEST_HEADERS)?.toBoolean()

/**
 * Retrieves the value of the "Log-Request-Body" header from the HttpRequest instance.
 *
 * @return The boolean value of the "Log-Request-Body" header, or null if the header is not present or cannot be parsed as a boolean.
 */
fun HttpRequest.logRequestBody(): Boolean? = headers.getFirst(RestTemplateConfigHeaders.LOG_REQUEST_BODY)?.toBoolean()

/**
 * Retrieves the value of the LOG_REQUEST_TOOK_TIME header and converts it to a Boolean.
 *
 * @return The Boolean value of the LOG_REQUEST_TOOK_TIME header, or null if the header is not present or cannot be parsed as a Boolean.
 */
fun HttpRequest.logRequestTookTime(): Boolean? = headers.getFirst(RestTemplateConfigHeaders.LOG_REQUEST_TOOK_TIME)?.toBoolean()

/**
 * Retrieves the value of the 'LOG_RESPONSE_URI' header from the HttpRequest headers and converts it to a Boolean.
 *
 * @return The Boolean value of the 'LOG_RESPONSE_URI' header, or null if the header is not present or the value cannot be converted to Boolean.
 */
fun HttpRequest.logResponseUri(): Boolean? = headers.getFirst(RestTemplateConfigHeaders.LOG_RESPONSE_URI)?.toBoolean()

/**
 * Retrieves the value of the "LOG_RESPONSE_HEADERS" header and converts it to a Boolean.
 *
 * @return The Boolean value of the "LOG_RESPONSE_HEADERS" header, or null if the header is not present or cannot be converted to Boolean.
 */
fun HttpRequest.logResponseHeaders(): Boolean? = headers.getFirst(RestTemplateConfigHeaders.LOG_RESPONSE_HEADERS)?.toBoolean()

/**
 * Retrieves the value of the "log_response_body" header and converts it to a Boolean value.
 *
 * @return The Boolean value of the "log_response_body" header, or null if the header is not present or cannot be converted to Boolean.
 */
fun HttpRequest.logResponseBody(): Boolean? = headers.getFirst(RestTemplateConfigHeaders.LOG_RESPONSE_BODY)?.toBoolean()

/**
 * Retrieves the value of the "Log Response Took Time" header and converts it to a Boolean.
 *
 * @return The value of the "Log Response Took Time" header as a Boolean, or null if the header is not present or cannot be converted to a Boolean.
 */
fun HttpRequest.logResponseTookTime(): Boolean? = headers.getFirst(RestTemplateConfigHeaders.LOG_RESPONSE_TOOK_TIME)?.toBoolean()
