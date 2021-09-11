package com.joker.codelinker

import com.joker.codelinker.model.ClzMethod
import com.joker.codelinker.model.METHODS_CLINIT
import com.joker.codelinker.model.Method
import com.joker.codelinker.util.DescriptorAnalyser
import com.joker.codelinker.util.TraceSignatureVisitor2
import com.joker.codelinker.util.containsAllMethods
import com.joker.codelinker.util.isClinitMethod
import com.joker.codelinker.util.isStatic
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.TryCatchBlockNode
import org.objectweb.asm.tree.TypeInsnNode

internal class ClzNode(api: Int) : ClassNode(api) {
    internal fun start(targetMethod: Method, linkerConfig: LinkerConfig) {
        val mode = when {
            targetMethod.containsAllMethods() -> AllMethodsMode()
//            targetMethod.clinitMethod() -> AllStaticMethodsMode()
            else -> SpecificOneMethodMode(targetMethod)
        }
        // method
        val targetMethods = methods.filter { mode.isTargetMethod(it) }
        if (targetMethods.isEmpty() && mode.targetMethodMustExist()) {
            val clzMethod = ClzMethod(superName, Method(targetMethod.methodName, targetMethod.methodDesc))
            if (clzMethod.startClzName == null) {
                clzMethod.startClzName = name
            }
            linkerConfig.addMethod(clzMethod)
        }
        analyseMethods(targetMethods, linkerConfig)

        // class
        analyseClz(linkerConfig)

        // filed
        val staticFields = fields.filter { it.isStatic() }
        analyseFields(staticFields, linkerConfig)
        if (mode.analyseNotStaticFields()) {
            analyseFields(fields.subtract(staticFields).toList(), linkerConfig)
        }
    }

    private fun analyseMethods(methods: List<MethodNode>, linkerConfig: LinkerConfig) {
        val clzName = name
        methods.forEach {
            println("================method start: $clzName ${it.name}${it.desc} ================")
            analyseInstructions(it.instructions, linkerConfig)
            analyseLocalVariables(it.localVariables, linkerConfig)
            analyseMethodDescriptor(it.desc, linkerConfig)
            analyseExceptions(it.exceptions, linkerConfig)
            analyseTryCatchBlocks(it.tryCatchBlocks, linkerConfig)
            println("================method end: $clzName ${it.name}${it.desc} ================")
        }
    }

    private fun analyseClz(linkerConfig: LinkerConfig) {
        linkerConfig.addClz(superName)
        interfaces?.forEach { linkerConfig.addClz(it) }
    }

    private fun analyseFields(filed: List<FieldNode>, linkerConfig: LinkerConfig) {
        filed.forEach { analyseFiledDescriptor(it.desc, linkerConfig) }
    }

    private fun analyseMethodDescriptor(d: String?, linkerConfig: LinkerConfig) {
        val desc = d ?: return
        DescriptorAnalyser.getMethodDescriptor(desc).forEach { linkerConfig.addClz(it) }
    }

    private fun analyseFiledDescriptor(d: String?, linkerConfig: LinkerConfig) {
        val desc = d ?: return
        linkerConfig.addClz(DescriptorAnalyser.getFieldDescriptor(desc))
    }

    private fun analyseMethodDescriptor(methodInsnNode: MethodInsnNode, linkerConfig: LinkerConfig) {
        val clzDescriptor = DescriptorAnalyser.getClzDescriptor(methodInsnNode.owner) ?: return
        linkerConfig.addMethod(ClzMethod(clzDescriptor, Method(methodInsnNode.name, methodInsnNode.desc)))
    }

    private fun analyseClzDescriptor(d: String?, linkerConfig: LinkerConfig) {
        val desc = d ?: return
        linkerConfig.addClz(DescriptorAnalyser.getClzDescriptor(desc))
    }

    private fun analyseInstructions(instructions: InsnList?, linkerConfig: LinkerConfig) {
        instructions ?: return
        instructions.forEach {
            when (it) {
                is FieldInsnNode -> {
                    analyseClzDescriptor(it.owner, linkerConfig)
                    analyseFiledDescriptor(it.desc, linkerConfig)
                }
                is InvokeDynamicInsnNode -> {
                    analyseInvokeDynamic(it, linkerConfig)
                }
                is MethodInsnNode -> {
                    analyseMethodDescriptor(it, linkerConfig)
                }
                is MultiANewArrayInsnNode -> {
                    analyseClzDescriptor(it.desc, linkerConfig)
                }
                is TypeInsnNode -> {
                    analyseClzDescriptor(it.desc, linkerConfig)
                }
            }
        }
    }

    private fun analyseInvokeDynamic(dynamicInsnNode: InvokeDynamicInsnNode, linkerConfig: LinkerConfig) {
        linkerConfig.addMethod(
            ClzMethod(
                dynamicInsnNode.bsm.owner,
                Method(dynamicInsnNode.bsm.name, dynamicInsnNode.bsm.desc)
            )
        )
        DescriptorAnalyser.getMethodDescriptor(dynamicInsnNode.desc).forEach { linkerConfig.addClz(it) }
    }

    private fun analyseLocalVariables(localVariables: List<LocalVariableNode>?, linkerConfig: LinkerConfig) {
        localVariables?.forEach { analyseFiledDescriptor(it.desc, linkerConfig) }
    }

    private fun analyseExceptions(exceptions: List<String>, linkerConfig: LinkerConfig) {
        exceptions.forEach { linkerConfig.addClz(it) }
    }

    private fun analyseTryCatchBlocks(tryCatchBlocks: List<TryCatchBlockNode>, linkerConfig: LinkerConfig) {
        tryCatchBlocks.forEach { linkerConfig.addClz(it.type) }
    }

    private interface ScanMode {
        fun isTargetMethod(methodNode: MethodNode): Boolean

        fun targetMethodMustExist() = false

        fun analyseNotStaticFields(): Boolean
    }

    private class SpecificOneMethodMode(private val targetMethod: Method) : ScanMode {
        override fun isTargetMethod(methodNode: MethodNode): Boolean =
            methodNode.name == targetMethod.methodName && methodNode.desc == targetMethod.methodDesc

        override fun targetMethodMustExist(): Boolean = true

        override fun analyseNotStaticFields(): Boolean = !targetMethod.isClinitMethod()
    }

    private class AllMethodsMode : ScanMode {
        override fun isTargetMethod(methodNode: MethodNode): Boolean = true

        override fun analyseNotStaticFields() = true
    }

    private fun analyseClzSignature(signatureStrings: String?, linkerConfig: LinkerConfig) {
        signatureStrings ?: return
        val signatureV = TraceSignatureVisitor2()
        SignatureReader(signatureStrings).accept(signatureV)
        signatureV.declaration.forEach { linkerConfig.addClz(it) }
    }

    private fun analyseMethodSignature(signatureStrings: String?, linkerConfig: LinkerConfig) {
        signatureStrings ?: return
        val signatureV = TraceSignatureVisitor2()
        SignatureReader(signatureStrings).accept(signatureV)
        signatureV.declaration.forEach {
            linkerConfig.addMethod(ClzMethod(it, METHODS_CLINIT))
        }
    }

    private fun analyseFieldSignature(signatureStrings: String?, linkerConfig: LinkerConfig) {
        signatureStrings ?: return
        val signatureV = TraceSignatureVisitor2()
        SignatureReader(signatureStrings).acceptType(signatureV)
        signatureV.declaration.forEach { linkerConfig.addClz(it) }
    }
}