package com.m3.tracing.spring.boot.web.client

import com.m3.tracing.M3Tracer
import com.m3.tracing.spring.http.client.M3TracingHttpRequestInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Enhance [org.springframework.web.client.RestTemplate] for distributed tracing!
 *
 * Note that `new RestTemplate()` code cannot get any benefit from Spring Boot.
 * To get such benefit, use `@Autowired RestTemplateBuilder` then build RestTemplate from it.
 */
@Configuration
class M3TracerRestTemplateConfig {

    @Bean
    fun m3tracerRestTemplateCustomizer(
            interceptor: M3TracingHttpRequestInterceptor
    ): RestTemplateCustomizer = RestTemplateCustomizer {
        it.interceptors.add(0, interceptor)
    }

    @Bean
    @ConditionalOnMissingBean
    fun m3tracingHttpRequestInterceptor(tracer: M3Tracer) = M3TracingHttpRequestInterceptor(tracer)
}
