package com.joker

import com.google.gson.Gson
import com.joker.codelinker.ClassLinker
import com.joker.codelinker.model.ClzMethodInfo
import com.joker.codelinker.model.MethodInfo
import com.joker.codelinker.model.Modifier
import com.joker.lang.LogClassLoader

fun main() {
    val actualSet = logClassLoader { Gson().newBuilder().create() }

    val wantSet = ClassLinker.newBuilder()
        .methods(
            ClzMethodInfo(
                clzName = "com.google.gson.Gson",
                methodInfo = setOf(
                    MethodInfo("newBuilder", Modifier(paramTypeString = "com.google.gson.GsonBuilder"), null)
                )
            ),
            ClzMethodInfo(
                clzName = "com.google.gson.GsonBuilder",
                methodInfo = setOf(
                    MethodInfo("create", Modifier(paramTypeString = "com.google.gson.Gson"), null)
                )
            )
        )
        .blockList { it.startsWith("java/") }
        .build()
        .link()

//    printLog("wantSet: $wantSet")
//    printLog("actual class loader: $actualSet")
//    printLog("want exist but actual not exist: ${wantSet.subtract(actualSet)}")
//    printLog("actual exist but want not exist: ${actualSet.subtract(wantSet).filter { !it.contains("$") }}")
}

private fun logClassLoader(block: () -> Unit): Set<String> {
    val loader = Object().javaClass.classLoader
    val parent = loader.parent
    val parentField = loader.javaClass.superclass.superclass.superclass.getDeclaredField("parent")
    parentField.isAccessible = true
    val newClassLoader = LogClassLoader()
    val declaredField = newClassLoader.javaClass.superclass.getDeclaredField("parent")
    declaredField.isAccessible = true
    declaredField.set(newClassLoader, parent)
    parentField.set(loader, newClassLoader)

    LogClassLoader.isEnable = true
    block.invoke()
    LogClassLoader.isEnable = false

    return LogClassLoader.set.map { it.replace(".", "/") }.toSet()
}