package com.m3.tracing.internal

object Config {

    /**
     * @param key Lower-case & dot-separated name
     */
    operator fun get(key: String): String? {
        System.getProperty(key).let {
            if (! it.isNullOrEmpty()) return it
        }
        System.getenv(key.replace('.', '_').toUpperCase()).let {
            if (! it.isNullOrEmpty()) return it
        }
        return null
    }
}
