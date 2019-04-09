package com.m3.tracing.servlet.impl

import io.opencensus.contrib.http.HttpExtractor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Similar to OcHttpServletExtractor (opencensus-contrib-http-servlet) but do not break request.
 */
internal class ServletRequestExtractor : HttpExtractor<HttpServletRequest, HttpServletResponse>() {
    override fun getHost(request: HttpServletRequest) = request.serverName
    override fun getMethod(request: HttpServletRequest) = request.method

    // TODO: Does this preserve request character set?
    override fun getPath(request: HttpServletRequest) = request.requestURI

    override fun getUserAgent(request: HttpServletRequest) = request.getHeader("User-Agent")

    override fun getStatusCode(response: HttpServletResponse?) = response?.status ?: 0

    // Intentionally avoided request.getQueryString to preserve character encoding
    // https://stackoverflow.com/a/19409520/914786
    override fun getUrl(request: HttpServletRequest) = request.requestURL.toString()

    override fun getRoute(request: HttpServletRequest) = ""
}
