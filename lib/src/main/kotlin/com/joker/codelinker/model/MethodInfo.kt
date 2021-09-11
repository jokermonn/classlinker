package com.joker.codelinker.model

import com.joker.codelinker.util.DESC_BOOLEAN
import com.joker.codelinker.util.DESC_BYTE
import com.joker.codelinker.util.DESC_CHAR
import com.joker.codelinker.util.DESC_DOUBLE
import com.joker.codelinker.util.DESC_FLOAT
import com.joker.codelinker.util.DESC_INT
import com.joker.codelinker.util.DESC_LONG
import com.joker.codelinker.util.DESC_SHORT
import com.joker.codelinker.util.DESC_VOID

data class MethodInfo(
    private val methodNameString: String,
    val methodReturnType: Modifier,
    val methodParams: List<Modifier>?
) {
    val methodName = methodNameString.replace(".", "/")
}

data class Modifier(
    private val paramTypeString: String,
    val isArray: Boolean = false,
    val arrayDimensions: Int = 0
) {
    val paramType = paramTypeString.replace(".", "/")
}

val BYTE = Modifier(DESC_BYTE)
val CHAR = Modifier(DESC_CHAR)
val DOUBLE = Modifier(DESC_DOUBLE)
val FLOAT = Modifier(DESC_FLOAT)
val INT = Modifier(DESC_INT)
val LONG = Modifier(DESC_LONG)
val SHORT = Modifier(DESC_SHORT)
val BOOLEAN = Modifier(DESC_BOOLEAN)
val VOID = Modifier(DESC_VOID)

internal val basicModifiers = setOf(
    BYTE,
    CHAR,
    DOUBLE,
    FLOAT,
    INT,
    LONG,
    SHORT,
    BOOLEAN,
    VOID
)
