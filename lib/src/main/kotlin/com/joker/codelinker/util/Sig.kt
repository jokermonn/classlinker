package com.joker.codelinker.util

internal const val DESC_BYTE = "B"
internal const val DESC_CHAR = "C"
internal const val DESC_DOUBLE = "D"
internal const val DESC_FLOAT = "F"
internal const val DESC_INT = "I"
internal const val DESC_LONG = "J"
internal const val DESC_SHORT = "S"
internal const val DESC_BOOLEAN = "Z"
internal const val DESC_VOID = "V"
internal val basicDescriptors = setOf(
    DESC_BYTE,
    DESC_CHAR,
    DESC_DOUBLE,
    DESC_FLOAT,
    DESC_INT,
    DESC_LONG,
    DESC_SHORT,
    DESC_BOOLEAN,
    DESC_VOID
)

internal const val DESC_START_ARRAY = "["
internal const val DESC_START_OBJECT = "L"
internal const val DESC_END_OBJECT = ";"

internal const val VOID_SIG = "()V"

internal fun String.removeClzSign(): String {
    if (this in basicDescriptors) {
        return this
    }
    require(this.startsWith(DESC_START_OBJECT) && this.endsWith(DESC_END_OBJECT)) {
        "unknown clz $this"
    }
    return this.substring(1, this.length - 1)
}