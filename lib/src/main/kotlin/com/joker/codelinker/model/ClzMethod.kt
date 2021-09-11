package com.joker.codelinker.model

internal data class ClzMethod(
    private val clzName: String,
    val method: Method
) {
    val className = clzName.replace(".", "/")

    var startClzName: String? = null

    override fun toString(): String = "$className ${method.methodName}${method.methodDesc}"
}
