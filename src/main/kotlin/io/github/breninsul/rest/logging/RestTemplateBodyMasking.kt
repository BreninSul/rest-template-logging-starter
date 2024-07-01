package io.github.breninsul.rest.logging

interface RestTemplateBodyMasking {
    fun mask(message: String?): String
}

interface RestTemplateRequestBodyMasking : RestTemplateBodyMasking

interface RestTemplateResponseBodyMasking : RestTemplateBodyMasking

open class RestTemplateRequestBodyMaskingDelegate(
    protected open val delegate: RestTemplateBodyMasking,
) : RestTemplateRequestBodyMasking {
    override fun mask(message: String?): String = delegate.mask(message)
}
open class RestTemplateRResponseBodyMaskingDelegate(
    protected open val delegate: RestTemplateBodyMasking
) : RestTemplateResponseBodyMasking {
    override fun mask(message: String?): String = delegate.mask(message)
}
open class RestTemplateRegexJsonBodyMasking(
    protected open val fields: List<String> = listOf("password", "pass", "code", "token", "secret"),
) : RestTemplateBodyMasking {
    protected open val emptyBody: String = ""
    protected open val maskedBody: String = "<MASKED>"

    protected open val fieldsGroup get() = fields.joinToString("|")
    protected open val regex: Regex = "\"($fieldsGroup)\":\"((\\\\\"|[^\"])*)\"".toRegex()

    override fun mask(message: String?): String {
        if (message == null) {
            return emptyBody
        }
        val ranges = regex.findAll(message).map { it.groups[2]!!.range }

        val maskedMessage = StringBuilder(message)
        ranges.forEach { range ->
            maskedMessage.replace(range.first,range.last+1, maskedBody)
        }
        return maskedMessage.toString()
    }
}

open class RestTemplateRegexFormUrlencodedBodyMasking(
    protected open val fields: List<String> = listOf("password", "pass", "code", "token", "secret"),
) : RestTemplateBodyMasking {
    protected open val emptyBody: String = ""
    protected open val maskedBody: String = "<MASKED>"

    protected open val fieldsGroup get() = fields.joinToString("|")
    protected open val regex: Regex = "($fieldsGroup)(=)([^&]*)(?:&|\$)".toRegex()

    override fun mask(message: String?): String {
        if (message == null) {
            return emptyBody
        }
        val ranges = regex.findAll(message).map { it.groups[3]!!.range }

        val maskedMessage = StringBuilder(message)
        ranges.forEach { range ->
            maskedMessage.replace(range.first,range.last+1, maskedBody)
        }
        return maskedMessage.toString()
    }
}
