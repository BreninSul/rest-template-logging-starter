package io.github.breninsul.rest.logging

import io.github.breninsul.logging.HttpBodyMasking
import io.github.breninsul.logging.HttpRequestBodyMasking
import io.github.breninsul.logging.HttpResponseBodyMasking
import io.github.breninsul.logging.HttpUriMasking

interface RestTemplateUriMasking : HttpUriMasking

interface RestTemplateRequestBodyMasking : HttpRequestBodyMasking

interface RestTemplateResponseBodyMasking : HttpResponseBodyMasking

open class RestTemplateRequestBodyMaskingDelegate(
    protected open val delegate: HttpBodyMasking,
) : RestTemplateRequestBodyMasking {
    override fun mask(message: String?): String = delegate.mask(message)
}
open class RestTemplateResponseBodyMaskingDelegate(
    protected open val delegate: HttpBodyMasking
) : RestTemplateResponseBodyMasking {
    override fun mask(message: String?): String = delegate.mask(message)
}
open class RestTemplateUriMaskingDelegate(
    protected open val delegate: HttpUriMasking
) : RestTemplateUriMasking {
    override fun mask(uri: String?): String = delegate.mask(uri)
}
