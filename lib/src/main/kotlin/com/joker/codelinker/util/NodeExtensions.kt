package com.joker.codelinker.util

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

fun MethodNode.isStatic(): Boolean = this.access and Opcodes.ACC_STATIC != 0

fun FieldNode.isStatic(): Boolean = this.access and Opcodes.ACC_STATIC != 0