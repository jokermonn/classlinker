package com.joker.codelinker

import com.joker.codelinker.model.ClzMethod
import com.joker.codelinker.model.METHODS_ALL
import com.joker.codelinker.model.METHODS_CLINIT
import com.joker.codelinker.model.Method
import com.joker.codelinker.model.NoSuchMethod
import java.util.LinkedList

class LinkerConfig {
    internal var filterBlock: (String) -> Boolean = { false }

    internal val noSuchMethods: MutableList<NoSuchMethod> = arrayListOf()

    // clz#method#descriptor
    private var recordSet: MutableSet<ClzMethod> = hashSetOf()

    // record all method
    internal var clzMethods: LinkedList<ClzMethod> = LinkedList()

    fun addClz(clzName: String?) {
        clzName ?: return
        addMethod(ClzMethod(clzName, METHODS_CLINIT))
    }

    internal fun addMethod(clzName: String?, method: Method) {
        clzName ?: return
        addMethod(ClzMethod(clzName, method))
    }

    internal fun addMethod(clzMethod: ClzMethod) {
        val clzName = clzMethod.className
        if (filterBlock.invoke(clzName)) {
            return
        }
        if (recordSet.contains(ClzMethod(clzName, METHODS_ALL))) {
            return
        }
        if (recordSet.contains(clzMethod)) {
            return
        }
        if (clzMethod.startClzName != null) {
            noSuchMethods.removeIf { it.clzName == clzMethod.startClzName }
        }
        recordSet.add(clzMethod)
        println("clzMethods add: $clzMethod")
        clzMethods.add(clzMethod)
    }
}