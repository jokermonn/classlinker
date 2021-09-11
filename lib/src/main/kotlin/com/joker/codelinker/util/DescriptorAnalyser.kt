package com.joker.codelinker.util

/**
 * modify from org.objectweb.asm.util.CheckMethodAdapter
 */
object DescriptorAnalyser {

    fun getClzDescriptor(descriptor: String?): String? {
        descriptor ?: return null
        val list = ArrayList<String>(1)
        if (descriptor[0] == '[') {
            checkDescriptor(descriptor, 0, false, list)
        } else {
            checkInternalClassName(descriptor, list)
        }
        assert(list.size <= 1)
        return list.getOrNull(0)
    }

    fun getMethodDescriptor(descriptor: String?): List<String> {
        if (descriptor.isNullOrEmpty()) {
            return emptyList()
        }
        val list = arrayListOf<String>()
        require(!(descriptor[0] != '(' || descriptor.length < 3)) { "$INVALID_DESCRIPTOR$descriptor" }
        var pos = 1
        if (descriptor[pos] != ')') {
            do {
                require(descriptor[pos] != 'V') { "$INVALID_DESCRIPTOR$descriptor" }
                pos = checkDescriptor(descriptor, pos, false, list)
            } while (pos < descriptor.length && descriptor[pos] != ')')
        }
        pos = checkDescriptor(descriptor, pos + 1, true, list)
        require(pos == descriptor.length) { "$INVALID_DESCRIPTOR$descriptor" }
        return list
    }

    fun getFieldDescriptor(descriptor: String?): String? {
        descriptor ?: return null
        val list = ArrayList<String>(1)
        checkDescriptor(descriptor, 0, false, list)
        assert(list.size <= 1)
        return list.getOrNull(0)
    }

    private fun checkDescriptor(
        descriptor: String?,
        startPos: Int,
        canBeVoid: Boolean,
        list: MutableList<String>
    ): Int {
        require(!(descriptor == null || startPos >= descriptor.length)) { "Invalid type descriptor (must not be null or empty)" }
        val stringArrays = descriptor.toCharArray().map { it.toString() }
        return when (stringArrays[startPos]) {
            DESC_VOID -> if (canBeVoid) {
                startPos + 1
            } else {
                throw IllegalArgumentException("$INVALID_DESCRIPTOR$descriptor")
            }
            in basicDescriptors -> startPos + 1
            DESC_START_ARRAY -> {
                var pos = startPos + 1
                while (pos < descriptor.length && descriptor[pos] == '[') {
                    ++pos
                }
                if (pos < descriptor.length) {
                    checkDescriptor(descriptor, pos, false, list)
                } else {
                    throw IllegalArgumentException("$INVALID_DESCRIPTOR$descriptor")
                }
            }
            DESC_START_OBJECT -> {
                val endPos = descriptor.indexOf(';', startPos)
                require(!(startPos == -1 || endPos - startPos < 2)) { "$INVALID_DESCRIPTOR$descriptor" }
                try {
                    checkInternalClassName(descriptor.substring(startPos + 1, endPos), list)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException(INVALID_DESCRIPTOR + descriptor, e)
                }
                endPos + 1
            }
            else -> throw IllegalArgumentException("$INVALID_DESCRIPTOR$descriptor")
        }
    }

    private fun checkInternalClassName(name: String, list: MutableList<String>) {
        try {
            var startIndex = 0
            var slashIndex: Int
            while (name.indexOf('/', startIndex + 1).also { slashIndex = it } != -1) {
                checkIdentifier(name, startIndex, slashIndex, list)
                startIndex = slashIndex + 1
            }
            checkIdentifier(name, startIndex, name.length, list)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("$name must be an internal class name", e)
        }
    }

    private fun checkIdentifier(name: String?, startPos: Int, endPos: Int, list: MutableList<String>) {
        require(!(name == null || if (endPos == -1) name.length <= startPos else endPos <= startPos)) { INVALID + MUST_NOT_BE_NULL_OR_EMPTY }
        val max = if (endPos == -1) name.length else endPos
        var i = startPos
        while (i < max) {
            require(".;[/".indexOf(name.codePointAt(i).toChar()) == -1) { "$name must not contain . ; [ or /): " }
            list.add(name)
            i = name.offsetByCodePoints(i, 1)
        }
    }

    private const val INVALID_DESCRIPTOR = "Invalid descriptor: "
    private const val INVALID = "Invalid "
    private const val MUST_NOT_BE_NULL_OR_EMPTY = " (must not be null or empty)"
}