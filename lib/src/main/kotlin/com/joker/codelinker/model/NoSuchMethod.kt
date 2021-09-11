package com.joker.codelinker.model

internal data class NoSuchMethod(
    val clzName: String,
    val methodName: String,
    val methodDesc: String
)
