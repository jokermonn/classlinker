package com.joker.codelinker.util

import org.objectweb.asm.Opcodes
import org.objectweb.asm.signature.SignatureVisitor

internal class TraceSignatureVisitor2 : SignatureVisitor {

    /** The Java generic type declaration corresponding to the visited signature.  */
    val declaration: MutableList<String>

    /** The Java generic method return type declaration corresponding to the visited signature.  */
    var returnType: MutableList<String>? = null
        private set

    /** The Java generic exception types declaration corresponding to the visited signature.  */
    var exceptions: MutableList<String>? = null
        private set

    constructor() : super(Opcodes.ASM9) {
        declaration = arrayListOf()
    }

    private constructor(declaration: MutableList<String>) : super(Opcodes.ASM9) {
        this.declaration = declaration
    }

    override fun visitReturnType(): SignatureVisitor {
        returnType = arrayListOf()
        return TraceSignatureVisitor2(returnType!!)
    }

    override fun visitExceptionType(): SignatureVisitor {
        if (exceptions == null) {
            exceptions = arrayListOf()
        }
        return TraceSignatureVisitor2(exceptions!!)
    }

    override fun visitClassType(name: String) {
        declaration.add(name.replace('/', '.'))
    }

    override fun visitInnerClassType(name: String) {
        declaration.add(name.replace('/', '.'))
    }
}