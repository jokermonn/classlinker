package com.joker.codelinker

import com.joker.codelinker.model.ALL_METHOD
import com.joker.codelinker.model.ClzMethod
import com.joker.codelinker.model.ClzMethodInfo
import com.joker.codelinker.model.METHODS_ALL
import com.joker.codelinker.model.METHODS_CLINIT
import com.joker.codelinker.util.logEnabled
import com.joker.codelinker.util.printLog
import com.joker.codelinker.util.toMethod
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes

class ClassLinker constructor(builder: Builder) {

    private val linkerConfig = LinkerConfig()

    init {
        linkerConfig.blockList = builder.blockList
        linkerConfig.whiteList = builder.whiteList
        logEnabled = builder.logEnabled
        builder.list.forEach { linkerConfig.addMethod(it) }
    }

    fun link(): List<String> {
        val clzs = arrayListOf<String>()
        while (linkerConfig.clzMethods.isNotEmpty()) {
            val clzNode = ClzNode(Opcodes.ASM5)
            val clzMethod = linkerConfig.clzMethods.poll()
            val clzName = clzMethod.className
            clzs.add(clzName)
            printLog("$clzName ${clzMethod.method.methodName}${clzMethod.method.methodDesc} is reading...")
            val reader = ClassReader(clzName)
            reader.accept(clzNode, 0)
            clzNode.start(clzMethod.method, linkerConfig)
        }
        linkerConfig.noSuchMethods.forEach {
            System.err.println("no such method of ${it.methodName} ${it.methodDesc} in ${it.clzName}")
        }

        return clzs.distinct()
    }

    class Builder internal constructor() {
        internal val list = arrayListOf<ClzMethod>()
        internal var logEnabled = false
        internal var blockList: ((String) -> Boolean)? = null
        internal var whiteList: ((String) -> Boolean)? = null

        fun methods(vararg clzMethods: ClzMethodInfo): Builder {
            val onlyClzSelf = clzMethods.filter { it.methodInfo == null }
            list.addAll(onlyClzSelf.map { ClzMethod(it.className, METHODS_CLINIT) })
            val subtract = clzMethods.subtract(onlyClzSelf)
            list.addAll(subtract.flatMap { clzMethod ->
                val info = clzMethod.methodInfo!!
                if (info == ALL_METHOD) {
                    listOf(ClzMethod(clzMethod.className, METHODS_ALL))
                } else {
                    info.map { methodInfo -> ClzMethod(clzMethod.className, methodInfo.toMethod()) }
                }
            })
            return this
        }

        fun enableLog(enabled: Boolean): Builder {
            this.logEnabled = enabled
            return this
        }

        fun blockList(block: (String) -> Boolean): Builder {
            if (whiteList != null) {
                throw IllegalArgumentException("The blocklist does not take effect when the whitelist is used")
            }
            blockList = block
            return this
        }

        fun whiteList(block: (String) -> Boolean): Builder {
            if (blockList != null) {
                throw IllegalArgumentException("The blocklist does not take effect when the whitelist is used")
            }
            whiteList = block
            return this
        }

        fun build(): ClassLinker {
            return ClassLinker(this)
        }
    }

    companion object {
        fun newBuilder(): Builder {
            return Builder()
        }
    }
}