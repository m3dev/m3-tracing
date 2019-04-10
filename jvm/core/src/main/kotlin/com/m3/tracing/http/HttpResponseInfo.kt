package com.m3.tracing.http

/**
 * Represents HTTP response.
 * Implementation depends on framework (Servlet, Play Framework, ...).
 *
 * This interface is intended NOT to consume stream of response body.
 * So that this interface only intaract with response headers & metadata.
 */
interface HttpResponseInfo {
    /**
     * Get metadata (e.g. Remote IP address) of this response.
     * @return If given key is not supported or no value supplied, return null.
     */
    fun <T> tryGetMetadata(key: HttpResponseMetadataKey<T>): T?
}

/**
 * Represents type (key) of metadata.
 * Any subclass must be singleton so that implementation can rely on instance reference equality.
 */
abstract class HttpResponseMetadataKey<T> {

    /** HTTP response status code */
    object StatusCode: HttpResponseMetadataKey<Int>()

    /** Length of the body */
    object ContentLength: HttpResponseMetadataKey<Long>()
}
