package com.m3.tracing.http

/**
 * Represents mutable HTTP request.
 * Implementation depends on framework (Servlet, Play Framework, ...).
 *
 * This interface is intended NOT to consume stream of request body to prevent breaking application.
 * Thus this interface does NOT provide any information depends on request body (includes request "parameter").
 */
interface MutableHttpRequestInfo : HttpRequestInfo {
    /**
     * Set value into header.
     * Should restrict name. Especially encoding/charset header must NOT be modified.
     */
    fun trySetHeader(name: String, value: String)
}
