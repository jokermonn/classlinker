package com.joker.codelinker.model

import com.joker.codelinker.util.METHOD_NAME_CLINIT
import com.joker.codelinker.util.METHOD_NAME_INIT
import com.joker.codelinker.util.VOID_SIG

internal data class Method(
    val methodName: String,
    val methodDesc: String
)

internal val METHOD_INIT_WITHOUT_PARAMS = Method(METHOD_NAME_INIT, VOID_SIG)

internal val METHODS_CLINIT = Method(METHOD_NAME_CLINIT, VOID_SIG)

internal val METHODS_ALL = Method("all_methods", "all_methods")