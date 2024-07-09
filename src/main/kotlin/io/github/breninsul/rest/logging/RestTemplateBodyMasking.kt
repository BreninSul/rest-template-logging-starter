package io.github.breninsul.rest.logging

import io.github.breninsul.logging.HttpBodyMasking
import io.github.breninsul.logging.HttpRequestBodyMasking
import io.github.breninsul.logging.HttpResponseBodyMasking


interface RestTemplateRequestBodyMasking : HttpRequestBodyMasking

interface RestTemplateResponseBodyMasking : HttpResponseBodyMasking

open class RestTemplateRequestBodyMaskingDelegate(
    protected open val delegate: HttpBodyMasking,
) : RestTemplateRequestBodyMasking {
    override fun mask(message: String?): String = delegate.mask(message)
}
open class RestTemplateRResponseBodyMaskingDelegate(
    protected open val delegate: HttpBodyMasking
) : RestTemplateResponseBodyMasking {
    override fun mask(message: String?): String = delegate.mask(message)
}
