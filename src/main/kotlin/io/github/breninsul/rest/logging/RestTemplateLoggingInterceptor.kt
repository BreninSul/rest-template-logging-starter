/*
 * MIT License
 *
 * Copyright (c) 2024 BreninSul
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.breninsul.rest.logging


import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * A client HTTP request interceptor for logging request and response
 * messages using RestTemplate.
 *
 * @param properties The RestTemplateLoggerProperties object containing the
 *     configuration properties.
 */
open class RestTemplateLoggingInterceptor(
    protected open val properties: RestTemplateLoggerProperties,
    protected open val requestBodyMaskers: List<RestTemplateRequestBodyMasking>,
    protected open val responseBodyMaskers: List<RestTemplateResponseBodyMasking>
) : ClientHttpRequestInterceptor, Ordered {
    /**
     * This variable represents a logger used for logging messages in the
     * RestTemplate class.
     *
     * @property logger An instance of the Logger class provided by the Java
     *     Logging API.
     * @see Logger
     */
    protected open val logger = Logger.getLogger(this.javaClass.name)

    /**
     * Represents the logging level used for logging. The value is retrieved
     * from the properties loggingLevel of the containing class.
     *
     * @see RestTemplateLoggingInterceptor
     */
    protected open val loggingLevel = properties.loggingLevel.javaLevel

    /**
     * The format of the header for logging purposes.
     *
     * The default value is "===========================CLIENT RestTemplate
     * %type% begin===========================", where "%type%" is a
     * placeholder that will be replaced with the type of log message (e.g.,
     * Request or Response). This format is used to create a header for the log
     * message.
     *
     * @see RestTemplateLoggingInterceptor.constructRqBody
     * @see RestTemplateLoggingInterceptor.constructRsBody
     */
    protected open val headerFormat: String = "\n===========================CLIENT RestTemplate %type% begin==========================="

    /**
     * The format string used to construct the footer of a log message.
     *
     * This format string is used in the RestTemplateLoggingInterceptor
     * class to construct the footer of a log message. It contains a
     * placeholder "%type%" which will be replaced with the log message
     * type (Request or Response). The default value of this format string
     * is "===========================CLIENT RestTemplate %type% end
     * ===========================".
     *
     * @see RestTemplateLoggingInterceptor
     */
    protected open val footerFormat: String = "===========================CLIENT RestTemplate %type% end  ==========================="

    /**
     * Represents the format string for new lines in the log output.
     *
     * The `newLineFormat` property is used to specify the format string for
     * new lines in the log output. It is used in the `formatLine` function to
     * pad the line start with the specified format string.
     *
     * The default value for `newLineFormat` is `"="`.
     */
    protected open val newLineFormat: String = "="

    /** A random number generator. */
    protected open val random = Random()

    /**
     * Enum class representing the type of the log message. It can be either
     * Request or Response.
     */
    protected enum class Type {
        Request, Response
    }

    /**
     * Intercepts an HTTP request and logs the request and response.
     *
     * @param request The HTTP request to be intercepted.
     * @param body The request body as a byte array.
     * @param execution The execution object for executing the request.
     * @return The intercepted HTTP response.
     */
    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        return try {
            val startTime = System.currentTimeMillis()
            val rqId = getIdString()
            logRequest(rqId, request, body, startTime)
            //Have to remove technical headers before request
            val technicalHeadersBackup = request.removeTechnicalHeaders()
            val httpResponse = execution.execute(request, body)
            //And set them back
            request.setTechnicalHeaders(technicalHeadersBackup)
            val response = logResponse(rqId, httpResponse, request, startTime)
            response
        } catch (e: Throwable) {
            logger.log(Level.SEVERE, "Exception in RestTemplate logging interceptor", e)
            throw e
        }
    }

    /**
     * Logs the response of an RestTemplate request and returns the intercepted
     * response.
     *
     * @param rqId The ID of the request.
     * @param response The original response from the server.
     * @param request The original request sent to the server.
     * @param time The time taken for the request and response.
     * @return The intercepted response.
     */
    protected open fun logResponse(
        rqId: String,
        response: ClientHttpResponse,
        request: HttpRequest,
        time: Long,
    ): ClientHttpResponse {
        if (loggingLevel == Level.OFF) {
            return response
        }
        val contentLength = response.headers.contentLength

        val haveToLogBody = request.logResponseBody() ?: properties.response.bodyIncluded
        val wrappedResponse = if (!haveToLogBody) response else CachedBodyClientHttpResponse(response)
        val emptyBody = contentLength == 0L
        val body = if (contentLength > properties.maxBodySize) constructTooBigMsg(contentLength)
        else if (!haveToLogBody) ""
        else if (emptyBody) ""
        else String((wrappedResponse as CachedBodyClientHttpResponse).bodyByteArray, StandardCharsets.UTF_8)
        val logBody = constructRsBody(rqId, response, request, time, body)
        logger.log(loggingLevel, logBody)
        return wrappedResponse
    }

    /**
     * Constructs the request body log message.
     *
     * @param rqId The request ID.
     * @param request The request object.
     * @param time The time taken for the request.
     * @param body The request body.
     * @return The constructed request body log message.
     */
    protected open fun constructRqBody(
        rqId: String,
        request: HttpRequest,
        time: Long,
        body: String,
    ): String {
        val message = listOf(
            headerFormat.replace("%type%", Type.Request.name),
            getIdString(rqId, Type.Request),
            getUriString(request.logRequestUri(), "${request.method} ${request.uri}", Type.Request),
            getTookString(request.logRequestTookTime(), time, Type.Request),
            getHeadersString(request.logRequestHeaders(), request.headers, Type.Request),
            getBodyString(request.logRequestBody(), body, Type.Request),
            footerFormat.replace("%type%", Type.Request.name),
        ).filter { !it.isNullOrBlank() }
            .joinToString("\n")
        return message
    }

    /**
     * Constructs the response body for logging.
     *
     * @param rqId The request ID.
     * @param response The ClientHttpResponse response.
     * @param request The HttpRequest request.
     * @param time The time taken for the request/response.
     * @param content The response body content.
     * @return The formatted response body.
     */
    protected open fun constructRsBody(
        rqId: String,
        response: ClientHttpResponse,
        request: HttpRequest,
        time: Long,
        content: String?,
    ): String {
        val message = listOf(
            headerFormat.replace("%type%", Type.Response.name),
            getIdString(rqId, Type.Response),
            getUriString(request.logResponseUri(), "${response.statusCode.value()} ${request.method} ${request.uri}", Type.Response),
            getTookString(request.logResponseTookTime(), time, Type.Response),
            getHeadersString(request.logResponseHeaders(), response.headers, Type.Response),
            getBodyString(request.logResponseBody(), content, Type.Response),
            footerFormat.replace("%type%", Type.Response.name),
        ).filter { !it.isNullOrBlank() }
            .joinToString("\n")
        return message
    }

    /**
     * Retrieves the ID string for logging purposes.
     *
     * @param rqId The request ID.
     * @param type The type of the log message.
     * @return The formatted ID string if it is included in logging, otherwise
     *     null.
     */
    protected open fun getIdString(rqId: String, type: Type): String? {
        return if (type.properties().idIncluded) formatLine("ID", rqId)
        else null

    }

    /**
     * Retrieves the URI string for logging purposes.
     *
     * @param logEnabledForRequest Indicates if logging is enabled for the
     *     request.
     * @param uri The URI string.
     * @param type The type of the log message (Request or Response).
     * @return The formatted URI string if logging is enabled for the request
     *     and the type properties indicate that the URI should be included in
     *     the log message, otherwise null.
     */
    protected open fun getUriString(logEnabledForRequest: Boolean?, uri: String, type: Type): String? {
        return if (logEnabledForRequest ?: type.properties().uriIncluded) formatLine("URI", uri)
        else null
    }

    /**
     * Retrieves the result of the "Took" operation as a formatted string.
     *
     * @param logEnabledForRequest Indicates if logging is enabled for the
     *     request.
     * @param startTime The start time of the operation.
     * @param type The type of the log message.
     * @return The formatted "Took" string if it is included in logging,
     *     otherwise null.
     */
    protected open fun getTookString(logEnabledForRequest: Boolean?, startTime: Long, type: Type): String? {
        return if (logEnabledForRequest ?: type.properties().tookTimeIncluded) formatLine("Took", "${System.currentTimeMillis() - startTime} ms")
        else null
    }

    /**
     * Retrieves the formatted headers string based on the given Headers object
     * and type.
     *
     * @param logEnabledForRequest Indicates if logging is enabled for the
     *     request.
     * @param headers The HttpHeaders object containing the headers
     *     information.
     * @param type The Type of the log message (Request or Response).
     * @return The formatted headers string if headersIncluded is true for the
     *     given type, otherwise null.
     */
    protected open fun getHeadersString(logEnabledForRequest: Boolean?, headers: HttpHeaders, type: Type): String? {
        val maskHeaders = when (type) {
            Type.Request -> properties.request.mask.maskHeaders
            Type.Response->properties.response.mask.maskHeaders
        }
        return if (logEnabledForRequest ?: type.properties().bodyIncluded) formatLine("Headers", headers.getHeadersString(maskHeaders))
        else null
    }

    /**
     * Retrieves the body string based on the given body and type.
     *
     * @param logEnabledForRequest Indicates if logging is enabled for the
     *     request.
     * @param body The body string to be included in the log message.
     * @param type The type of the log (Request or Response).
     * @return The formatted body string if bodyIncluded is true for the given
     *     type, otherwise null.
     */
    protected open fun getBodyString(logEnabledForRequest: Boolean?, body: String?, type: Type): String? {
        val maskers=when(type){
            Type.Request->requestBodyMaskers
            Type.Response->responseBodyMaskers
        }
        return if (logEnabledForRequest ?: type.properties().bodyIncluded) formatLine("Body", maskers.fold(body){b,it->it.mask(b)})
        else null
    }

    /**
     * Formats a line of log message with name and value.
     *
     * @param name The name of the line.
     * @param value The value of the line.
     * @return The formatted line.
     */
    protected open fun formatLine(name: String, value: String?): String {
        val lineStart = "${newLineFormat}${name}".padEnd(properties.newLineColumnSymbols, ' ')
        return "${lineStart}: $value"
    }

    /**
     * Retrieves the log settings for the given type.
     *
     * @return The log settings for the type.
     */
    protected open fun Type.properties(): RestTemplateLoggerProperties.LogSettings {
        return when (this) {
            Type.Request -> properties.request
            Type.Response -> properties.response
        }
    }


    /**
     * Logs the request.
     *
     * @param rqId The ID of the request.
     * @param request The HttpRequest object.
     * @param startTime The start time of the request.
     */
    protected open fun logRequest(
        rqId: String,
        request: HttpRequest,
        requestBody: ByteArray?,
        startTime: Long
    ) {
        if (loggingLevel == Level.OFF) {
            return
        }

        val contentLength = (requestBody?.size ?: 0).toLong()
        val emptyBody = contentLength == 0L
        val haveToLogBody = request.logRequestBody() ?: properties.request.bodyIncluded

        val body = if (contentLength > properties.maxBodySize) constructTooBigMsg(contentLength)
        else if (!haveToLogBody) ""
        else if (emptyBody) ""
        else String(requestBody!!, StandardCharsets.UTF_8)
        val logString = constructRqBody(rqId, request, startTime, body)
        logger.log(loggingLevel, logString)
    }

    /**
     * Constructs a too big message indicating the size of the content.
     *
     * @param contentLength The length of the content in bytes.
     * @return The constructed too big message.
     */
    protected open fun constructTooBigMsg(contentLength: Long) = "<TOO BIG $contentLength bytes>"

    /**
     * Retrieves the ID string for logging purposes.
     *
     * This method generates a random integer between 0 and 10,000,000, pads
     * the integer with leading zeros to reach a length of 7 digits, and
     * returns a substring of the padded integer. The returned ID string
     * consists of the first four characters of the substring, followed by a
     * hyphen, and then the fifth and subsequent characters of the substring.
     *
     * @return The formatted ID string.
     */
    protected open fun getIdString(): String {
        val integer = random.nextInt(10000000)
        val leftPad = integer.toString().padStart(7, '0')
        return leftPad.substring(0, 4) + '-' + leftPad.substring(5)
    }

    /**
     * Retrieves the order value of the `OKLoggingInterceptor`.
     *
     * @return The order value of the `OKLoggingInterceptor`.
     */
    override fun getOrder(): Int {
        return properties.order
    }


}
