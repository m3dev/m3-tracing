package com.m3.tracing

import javax.annotation.CheckReturnValue

/**
 * This object is NOT thread-safe expect for some methods.
 */
interface TraceSpan: AutoCloseable {

    /**
     * Close this span, means end of this span.
     */
    @Throws() // Remove checked exception
    override fun close()

    /**
     * Create child span.
     *
     * This method is thread-safe.
     * If the child span run in different thread from this span, must call this method from child thread.
     * See jvm/README.md for detail.
     *
     * @return Child span. Do NOT forget to close resulted span.
     */
    @CheckReturnValue // Caller must close the new span
    fun startChildSpan(name: String): TraceSpan

    /**
     * If exception raised while this span, should set this property.
     */
    @JvmDefault
    fun setError(e: Throwable?) {
        if (e != null) {
            this["exception_class"] = e.javaClass.name
            this["exception_messsage"] = e.message
        }
    }

    /** Set tag/attribute into this span. */
    operator fun set(tagName: String, value: String?): TraceSpan
    /** Set tag/attribute into this span. */
    operator fun set(tagName: String, value: Boolean?): TraceSpan
    /** Set tag/attribute into this span. */
    operator fun set(tagName: String, value: Long?): TraceSpan
    /** Set tag/attribute into this span. */
    @JvmDefault
    operator fun set(tagName: String, value: Any?) = when(value) {
        null -> this
        is String -> set(tagName, value)
        is Boolean -> set(tagName, value)
        is Long -> set(tagName, value)
        else -> set(tagName, "$value")
    }
}
