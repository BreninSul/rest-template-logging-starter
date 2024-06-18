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

import io.github.breninsul.rest.logging.RestTemplateLoggerProperties
import io.github.breninsul.rest.logging.RestTemplateLoggingInterceptor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * RestTemplateLoggerConfiguration is a configuration class that registers the RestTemplateLoggingInterceptor
 * as an interceptor in ResTemplate client when the property "rest-template.logging-interceptor.enabled" is set to true.
 * It is annotated with @ConditionalOnProperty, @AutoConfiguration, and @EnableConfigurationProperties.
 *
 * @constructor Creates a new RestTemplateLoggerConfiguration.
 */
@ConditionalOnProperty(value = ["rest-template.logging-interceptor.enabled"], havingValue = "true", matchIfMissing = true)
@AutoConfiguration
@EnableConfigurationProperties(RestTemplateLoggerProperties::class)
open class RestTemplateLoggerConfiguration {
    /**
     * Registers the RestTemplateLoggingInterceptor as bean
     *
     * @param properties The RestTemplateLoggerProperties used for configuring the interceptor.
     * @return The RestTemplateLoggingInterceptor instance.
     */
    @Bean
    open fun registerRestTemplateLoggingInterceptor(properties: RestTemplateLoggerProperties): RestTemplateLoggingInterceptor {
        return RestTemplateLoggingInterceptor(properties)
    }

}