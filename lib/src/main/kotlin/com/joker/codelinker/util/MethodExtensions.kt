package com.joker.codelinker.util

import com.joker.codelinker.model.METHODS_ALL
import com.joker.codelinker.model.METHODS_CLINIT
import com.joker.codelinker.model.Method
import com.joker.codelinker.model.MethodInfo
import com.joker.codelinker.model.Modifier
import com.joker.codelinker.model.basicModifiers

internal const val METHOD_NAME_INIT = "<init>"

internal const val METHOD_NAME_CLINIT = "<clinit>"

internal fun Method.containsAllMethods() = this == METHODS_ALL

internal fun Method.isClinitMethod() = this == METHODS_CLINIT

internal fun MethodInfo.toMethod(): Method {
    val stringBuilder = StringBuilder()
    stringBuilder.append("(")
    analyseParams(this.methodParams, stringBuilder)
    stringBuilder.append(")")
    analyseModifier(this.methodReturnType, stringBuilder, true)

    return Method(this.methodName, stringBuilder.toString())
}

private fun analyseParams(methodParams: List<Modifier>?, stringBuilder: StringBuilder) {
    methodParams?.forEach { analyseModifier(it, stringBuilder, true) }
}

private fun analyseModifier(modifier: Modifier, stringBuilder: StringBuilder, needEndObject: Boolean = false) {
    if (basicModifiers.contains(modifier)) {
        stringBuilder.append(modifier.paramType)
    } else if (modifier.isArray) {
        stringBuilder.append(DESC_START_ARRAY.repeat(modifier.arrayDimensions))
        analyseModifier(modifier.copy(isArray = false), stringBuilder, needEndObject)
    } else {
        stringBuilder.append(DESC_START_OBJECT).append(modifier.paramType)
        val endObject = if (needEndObject) {
            DESC_END_OBJECT
        } else ""
        stringBuilder.append(endObject)
    }
}