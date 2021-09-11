package com.joker.codelinker.model

data class ClzMethodInfo(
    private val clzName: String,
    val methodInfo: Set<MethodInfo>?
) {
    val className = clzName.replace(".", "/")
}

val ALL_METHOD = hashSetOf(MethodInfo("*", Modifier("*"), emptyList()))
