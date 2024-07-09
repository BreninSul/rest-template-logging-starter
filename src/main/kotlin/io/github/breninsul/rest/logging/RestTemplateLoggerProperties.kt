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

import io.github.breninsul.logging.HttpLogSettings
import io.github.breninsul.logging.HttpLoggingProperties
import io.github.breninsul.logging.JavaLoggingLevel
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for RestTemplateLogger.
 *
 * This class represents the configuration properties for RestTemplateLogger.
 * It is annotated with @ConfigurationProperties and the prefix "rest-template.logging-interceptor".
 *
 * The properties can be used to configure various aspects of the RestTemplateLogger library.
 * The default values are provided in the constructor.
 * @property enabled Whether the RestTemplateLogger is enabled. Default value is `true`.
 * @property loggingLevel The logging level for RestTemplateLogger. Default value is INFO.
 * @property request The settings for logging various aspects of requests.
 * @property response The settings for logging various aspects of responses.
 * @property maxBodySize The maximum body size to log. Default value is Int.MAX_VALUE.
 * @property order The order of the RestTemplateLogger interceptor. Default value is 0.
 * @property newLineColumnSymbols The number of column symbols for new line. Default value is 14.
 */
@ConfigurationProperties("rest-template.logging-interceptor")
open class RestTemplateLoggerProperties(
     enabled: Boolean = true,
     loggingLevel: JavaLoggingLevel = JavaLoggingLevel.INFO,
     request: HttpLogSettings = HttpLogSettings(tookTimeIncluded = false),
     response: HttpLogSettings = HttpLogSettings(tookTimeIncluded = true),
     maxBodySize: Int = Int.MAX_VALUE,
     order: Int = 0,
     newLineColumnSymbols: Int = 14,
):HttpLoggingProperties(enabled, loggingLevel, request, response, maxBodySize, order, newLineColumnSymbols)
