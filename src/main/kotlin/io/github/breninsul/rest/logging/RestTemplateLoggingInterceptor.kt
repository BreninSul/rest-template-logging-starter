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

import io.github.breninsul.logging.HttpLoggingHelper
import org.springframework.core.Ordered
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.nio.charset.StandardCharsets
import java.util.function.Supplier
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
    requestBodyMaskers: List<RestTemplateRequestBodyMasking>,
    responseBodyMaskers: List<RestTemplateResponseBodyMasking>,
) : ClientHttpRequestInterceptor, Ordered {
    protected open val helper = HttpLoggingHelper("RestTemplate", properties, requestBodyMaskers, responseBodyMaskers)
    protected open val logger: Logger = Logger.getLogger(RestTemplateLoggingInterceptor::class.java.name)

    /**
     * Intercepts an HTTP request and logs the request and response.
     *
     * @param request The HTTP request to be intercepted.
     * @param body The request body as a byte array.
     * @param execution The execution object for executing the request.
     * @return The intercepted HTTP response.
     */
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse =
        try {
            val startTime = System.currentTimeMillis()
            val rqId = helper.getIdString()
            logRequest(rqId, request, body, startTime)
            // Have to remove technical headers before request
            val technicalHeadersBackup = request.removeTechnicalHeaders()
            val httpResponse = execution.execute(request, body)
            // And set them back
            request.setTechnicalHeaders(technicalHeadersBackup)
            val response = logResponse(rqId, httpResponse, request, startTime)
            response
        } catch (e: Throwable) {
            logger.log(Level.SEVERE, "Exception in RestTemplate logging interceptor", e)
            throw e
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
        if (helper.loggingLevel == Level.OFF) {
            return response
        }
        val haveToLogBody = request.logResponseBody() ?: properties.response.bodyIncluded
        val wrappedResponse = if (!haveToLogBody) response else CachedBodyClientHttpResponse(response)
        val logBody = constructRsBody(rqId, response, request, time){
            val contentLength = response.headers.contentLength
            val emptyBody = contentLength == 0L
            if (contentLength > properties.maxBodySize) {
                helper.constructTooBigMsg(contentLength)
            } else if (!haveToLogBody) {
                ""
            } else if (emptyBody) {
                ""
            } else {
                String((wrappedResponse as CachedBodyClientHttpResponse).bodyByteArray, StandardCharsets.UTF_8)
            }
        }
        logger.log(helper.loggingLevel, logBody)
        return wrappedResponse
    }

    /**
     * Constructs the request body log message.
     *
     * @param rqId The request ID.
     * @param request The request object.
     * @param time The time taken for the request.
     * @param contentSupplier The request body Supplier.
     * @return The constructed request body log message.
     */
    protected open fun constructRqBody(
        rqId: String,
        request: HttpRequest,
        time: Long,
        contentSupplier: Supplier<String?>,
    ): String {
        val type = HttpLoggingHelper.Type.REQUEST
        val message =
            listOf(
                helper.getHeaderLine(type),
                helper.getIdString(rqId, type),
                helper.getUriString(request.logRequestUri(), "${request.method} ${request.uri}", type),
                helper.getTookString(request.logRequestTookTime(), time, type),
                helper.getHeadersString(request.logRequestHeaders(), request.headers, type),
                helper.getBodyString(request.logRequestBody(), contentSupplier, type),
                helper.getFooterLine(type),
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
     * @param contentSupplier The response body content Supplier.
     * @return The formatted response body.
     */
    protected open fun constructRsBody(
        rqId: String,
        response: ClientHttpResponse,
        request: HttpRequest,
        time: Long,
        contentSupplier: Supplier<String?>,
    ): String {
        val type = HttpLoggingHelper.Type.RESPONSE
        val message =
            listOf(
                helper.getHeaderLine(type),
                helper.getIdString(rqId, type),
                helper.getUriString(request.logResponseUri(), "${response.statusCode.value()} ${request.method} ${request.uri}", type),
                helper.getTookString(request.logResponseTookTime(), time, type),
                helper.getHeadersString(request.logResponseHeaders(), response.headers, type),
                helper.getBodyString(request.logResponseBody(), contentSupplier, type),
                helper.getFooterLine(type),
            ).filter { !it.isNullOrBlank() }
                .joinToString("\n")
        return message
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
        startTime: Long,
    ) {
        if (helper.loggingLevel == Level.OFF) {
            return
        }

        val logString =
            constructRqBody(rqId, request, startTime) {
                val contentLength = (requestBody?.size ?: 0).toLong()
                val emptyBody = contentLength == 0L
                val haveToLogBody = request.logRequestBody() ?: properties.request.bodyIncluded

                if (contentLength > properties.maxBodySize) {
                    helper.constructTooBigMsg(contentLength)
                } else if (!haveToLogBody) {
                    ""
                } else if (emptyBody) {
                    ""
                } else {
                    String(requestBody!!, StandardCharsets.UTF_8)
                }
            }
        logger.log(helper.loggingLevel, logString)
    }

    /**
     * Retrieves the order value of the `OKLoggingInterceptor`.
     *
     * @return The order value of the `OKLoggingInterceptor`.
     */
    override fun getOrder(): Int = properties.order
}
