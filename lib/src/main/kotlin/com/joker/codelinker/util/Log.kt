package com.joker.codelinker.util

var logEnabled = false

fun printLog(string: String?) {
    if (logEnabled) {
        println(string)
    }
}
