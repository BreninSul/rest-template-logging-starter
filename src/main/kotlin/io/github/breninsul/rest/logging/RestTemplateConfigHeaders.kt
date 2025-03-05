package io.github.breninsul.rest.logging


import io.github.breninsul.logging2.HttpBodyType
import io.github.breninsul.logging2.HttpConfigHeaders
import io.github.breninsul.logging2.HttpConfigHeaders.TECHNICAL_HEADERS
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.web.client.RestClient


/**
 * Converts the HttpHeaders to a MultiMap where keys are strings and values
 * are lists of strings.
 *
 * @return The converted MultiMap.
 */
fun HttpHeaders.mapToMultiMap(): Map<String, List<String>> =
    this.toMap()

/**
 * Returns a list of technical headers present in the HTTP request.
 *
 * @return A list of pairs representing the technical headers, where each
 *    pair consists of a header name and its corresponding value.
 */
fun HttpRequest.getTechnicalHeaders(): List<Pair<String, String>> = TECHNICAL_HEADERS.map { it to this.headers.getFirst(it) }.filter { it.second != null } as List<Pair<String, String>>

/**
 * Removes the technical headers from the HTTP request headers. Technical
 * headers are headers that are used for internal purposes and are not
 * relevant for logging or further processing.
 *
 * @return A list of pairs representing the technical headers that were
 *    removed from the request. Each pair consists of a header name and its
 *    corresponding value.
 * @see HttpRequest
 */
fun HttpRequest.removeTechnicalHeaders(): List<Pair<String, String>> {
    return TECHNICAL_HEADERS.map { it to headers.remove(it)?.firstOrNull() }.filter { it.second != null } as List<Pair<String, String>>
}

/**
 * Sets the technical headers in the current HttpRequest.
 *
 * @param list The list of header pairs to be set. Each pair consists of a
 *    header name and its corresponding value.
 */
fun HttpRequest.setTechnicalHeaders(list: List<Pair<String, String>>) {
    list.forEach { this.headers[it.first] = listOf(it.second) }
}

/**
 * Extracts and converts the `LOG_REQUEST_ID` header value to a Boolean.
 *
 * This function checks the headers of an `HttpRequest` for
 * the presence of the `LOG_REQUEST_ID` header defined in
 * `HttpConfigHeaders.LOG_REQUEST_ID`. If present, the first value
 */
fun HttpRequest.logRequestId(): Boolean? = headers[HttpConfigHeaders.LOG_REQUEST_ID]?.firstOrNull()?.toBoolean()

/**
 * Adds or removes a header to handle the logging of the request ID.
 * When the `log` parameter is non-null, the method adds a header with
 * the name defined in `HttpConfigHeaders.LOG_REQUEST_ID` and the
 * string representation of the `log` parameter as its value. If the
 * `log` parameter is null, the method removes the header with the name
 * `HttpConfigHeaders.LOG_REQUEST_ID`.
 *
 * @param log A Boolean value that determines whether to add or remove the
 *    request ID log header. If true or false, a header is added with the
 *    respective string value. If null, the header is removed.
 * @return The instance of `Request.Builder` to allow for method chaining.
 */
fun RestClient.RequestBodySpec.logRequestId(log: Boolean?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_REQUEST_ID
    return this.header(headerName, log?.toString())
}

/**
 * Retrieves the value of the "LOG_REQUEST_URI" header from the current
 * HTTP request headers and converts it to a Boolean.
 *
 * @return The Boolean value of the "LOG_REQUEST_URI" header, or null if
 *    the header is not present or cannot be converted to a Boolean.
 */
fun HttpRequest.logRequestUri(): Boolean? = headers.getFirst(HttpConfigHeaders.LOG_REQUEST_URI)?.toBoolean()

fun RestClient.RequestBodySpec.logRequestUri(log: Boolean?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_REQUEST_URI
    return this.header(headerName, log?.toString())
}

fun HttpRequest.logRequestMaskQueryParameters(): List<String>? = headers[HttpConfigHeaders.LOG_REQUEST_MASK_QUERY_PARAMETERS]?.firstOrNull()?.split(",")


fun RestClient.RequestBodySpec.logRequestMaskQueryParameters(params: Collection<String>?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_REQUEST_MASK_QUERY_PARAMETERS
    return this.header(headerName, params?.joinToString(","))
}

/**
 * Retrieves the value of the "LOG_REQUEST_HEADERS" header from the
 * HttpRequest headers.
 *
 * @return The value of the "LOG_REQUEST_HEADERS" header as a Boolean, or
 *    null if the header is not present or cannot be parsed as a Boolean.
 */
fun HttpRequest.logRequestHeaders(): Boolean? = headers.getFirst(HttpConfigHeaders.LOG_REQUEST_HEADERS)?.toBoolean()
fun RestClient.RequestBodySpec.logRequestHeaders(log: Boolean?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_REQUEST_HEADERS
    return this.header(headerName, log?.toString())
}


fun HttpRequest.logRequestMaskHeaders(): List<String>? = headers[HttpConfigHeaders.LOG_REQUEST_MASK_HEADERS]?.firstOrNull()?.split(",")


fun RestClient.RequestBodySpec.logRequestMaskHeaders(params: Collection<String>?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_REQUEST_MASK_HEADERS
    return this.header(headerName, params?.joinToString(","))
}

/**
 * Retrieves the value of the "Log-Request-Body" header from the
 * HttpRequest instance.
 *
 * @return The boolean value of the "Log-Request-Body" header, or null if
 *    the header is not present or cannot be parsed as a boolean.
 */
fun HttpRequest.logRequestBody(): Boolean? = headers.getFirst(HttpConfigHeaders.LOG_REQUEST_BODY)?.toBoolean()
fun RestClient.RequestBodySpec.logRequestBody(log: Boolean?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_REQUEST_BODY
    return this.header(headerName, log?.toString())
}

fun HttpRequest.logRequestMaskBodyKeys(): Map<HttpBodyType,Set<String>>? = headers[HttpConfigHeaders.LOG_REQUEST_MASK_BODY_KEYS]?.firstOrNull()?.let{ val keyTypes = it.split(";")
    val mapped=keyTypes.map { keyType ->
        val split = keyType.split(":")
        return@map if (split.size == 2) {
            HttpBodyType(split[0]) to split[1].split(",").toSet()
        } else {
            null
        }
    }.filterNotNull()
    return@let mapped.toMap()
}


fun RestClient.RequestBodySpec.logRequestMaskBodyKeys(params:Map<HttpBodyType,Set<String>>?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_REQUEST_MASK_BODY_KEYS
    return if (params!=null ) {
        this.header(headerName, params.entries.joinToString(";") { "${it.key}:${it.value.joinToString(",")}}" })
    } else {
        this.header(headerName,null)
    }
}
/**
 * Retrieves the value of the LOG_REQUEST_TOOK_TIME header and converts it
 * to a Boolean.
 *
 * @return The Boolean value of the LOG_REQUEST_TOOK_TIME header, or null
 *    if the header is not present or cannot be parsed as a Boolean.
 */
fun HttpRequest.logRequestTookTime(): Boolean? = headers.getFirst(HttpConfigHeaders.LOG_REQUEST_TOOK_TIME)?.toBoolean()
fun RestClient.RequestBodySpec.logRequestTookTime(log: Boolean?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_REQUEST_TOOK_TIME
    return this.header(headerName, log?.toString())
}

fun HttpRequest.logResponseId(): Boolean? = headers[HttpConfigHeaders.LOG_RESPONSE_ID]?.firstOrNull()?.toBoolean()

fun RestClient.RequestBodySpec.logResponseId(log: Boolean?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_RESPONSE_ID
    return this.header(headerName, log?.toString())
}

fun HttpRequest.logResponseUri(): Boolean? = headers.getFirst(HttpConfigHeaders.LOG_RESPONSE_URI)?.toBoolean()

fun HttpRequest.logResponseMaskQueryParameters(): List<String>? = headers[HttpConfigHeaders.LOG_RESPONSE_MASK_QUERY_PARAMETERS]?.firstOrNull()?.split(",")


fun RestClient.RequestBodySpec.logResponseMaskQueryParameters(params: Collection<String>?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_RESPONSE_MASK_QUERY_PARAMETERS
    return this.header(headerName, params?.joinToString(","))
}
/**
 * Retrieves the value of the "LOG_RESPONSE_HEADERS" header and converts it
 * to a Boolean.
 *
 * @return The Boolean value of the "LOG_RESPONSE_HEADERS" header, or null
 *    if the header is not present or cannot be converted to Boolean.
 */
fun HttpRequest.logResponseHeaders(): Boolean? = headers.getFirst(HttpConfigHeaders.LOG_RESPONSE_HEADERS)?.toBoolean()
fun RestClient.RequestBodySpec.logResponseHeaders(log: Boolean?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_RESPONSE_HEADERS
    return this.header(headerName, log?.toString())
}
fun HttpRequest.logResponseMaskHeaders(): List<String>? = headers[HttpConfigHeaders.LOG_RESPONSE_MASK_HEADERS]?.firstOrNull()?.split(",")


fun RestClient.RequestBodySpec.logResponseMaskHeaders(params: Collection<String>?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_RESPONSE_MASK_HEADERS
    return this.header(headerName, params?.joinToString(","))
}
/**
 * Retrieves the value of the "log_response_body" header and converts it to
 * a Boolean value.
 *
 * @return The Boolean value of the "log_response_body" header, or null if
 *    the header is not present or cannot be converted to Boolean.
 */
fun HttpRequest.logResponseBody(): Boolean? = headers.getFirst(HttpConfigHeaders.LOG_RESPONSE_BODY)?.toBoolean()
fun RestClient.RequestBodySpec.logResponseBody(log: Boolean?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_RESPONSE_BODY
    return this.header(headerName, log?.toString())
}
fun HttpRequest.logResponseMaskBodyKeys(): Map<HttpBodyType,Set<String>>? = headers[HttpConfigHeaders.LOG_RESPONSE_MASK_BODY_KEYS]?.firstOrNull()?.let{ val keyTypes = it.split(";")
    val mapped=keyTypes.map { keyType ->
        val split = keyType.split(":")
        return@map if (split.size == 2) {
            HttpBodyType(split[0]) to split[1].split(",").toSet()
        } else {
            null
        }
    }.filterNotNull()
    return@let mapped.toMap()
}

fun RestClient.RequestBodySpec.logResponseMaskBodyKeys(params:Map<HttpBodyType,Set<String>>?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_RESPONSE_MASK_BODY_KEYS
    return if (params!=null ) {
        this.header(headerName, params.entries.joinToString(";") { "${it.key}:${it.value.joinToString(",")}}" })
    } else {
        this.header(headerName,null)
    }
}
/**
 * Retrieves the value of the "Log Response Took Time" header and converts
 * it to a Boolean.
 *
 * @return The value of the "Log Response Took Time" header as a Boolean,
 *    or null if the header is not present or cannot be converted to a
 *    Boolean.
 */
fun HttpRequest.logResponseTookTime(): Boolean? = headers.getFirst(HttpConfigHeaders.LOG_RESPONSE_TOOK_TIME)?.toBoolean()

fun RestClient.RequestBodySpec.logResponseTookTime(log: Boolean?): RestClient.RequestBodySpec {
    val headerName = HttpConfigHeaders.LOG_RESPONSE_TOOK_TIME
    return this.header(headerName, log?.toString())
}