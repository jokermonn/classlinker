package com.joker.lang

class LogClassLoader : ClassLoader() {
    companion object {
        val set = hashSetOf<String>()

        var isEnable = false
    }

    override fun findClass(name: String?): Class<*> {
        doSomething(name)
        return super.findClass(name)
    }

    private fun doSomething(name: String?) {
        if (isEnable) {
            name?.let { set.add(it) }
        }
    }

    override fun loadClass(name: String?): Class<*> {
        return super.loadClass(name)
    }
}