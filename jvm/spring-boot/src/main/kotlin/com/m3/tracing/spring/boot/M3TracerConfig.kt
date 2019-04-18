package com.m3.tracing.spring.boot

import com.m3.tracing.M3Tracer
import com.m3.tracing.M3TracerFactory
import com.m3.tracing.spring.boot.aop.M3TracedAspect
import com.m3.tracing.spring.boot.web.client.M3TracerRestTemplateConfig
import com.m3.tracing.tracer.servlet.M3TracingFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered

/**
 * Auto configuration class for spring-boot.
 * You can enable this configuration as `@Import(M3TracerConfig)`.
 *
 * This configuration also includes some additional configs, see `@Import` annotation of this class.
 */
@Configuration
@Import(M3TracerRestTemplateConfig::class)
class M3TracerConfig {

    /**
     * You can get [M3Tracer] instance with DI.
     * DI is an better way rather than static getter (`M3TracerFactory.get()`).
     * Especially it makes easy to write unittest / stubbing.
     */
    @Bean
    @ConditionalOnMissingBean
    fun m3Tracer() = M3TracerFactory.get()

    /**
     * You can trace method calls with [com.m3.tracing.annotation.M3Traced] annotation.
     *
     * Note that only DI (AOP) managed objects are traced.
     * Direct method call (such as `new Something().call()`) cannot be traced due to lack of AOP proxy.
     */
    @Bean
    fun m3TracedAspect(tracer: M3Tracer) = M3TracedAspect(tracer)

    /**
     * Configure [M3TracingFilter] with DI / spring way, rather than Servlet way.
     */
    @Bean
    @ConditionalOnMissingBean
    fun m3TracingFilter(tracer: M3Tracer) = M3TracingFilter().also {
        // init(Config) method should be called BEFORE init(FilterConfig) to set configuration by instance.
        // We do not rely on FilterRegistrationBean#initParameters
        it.init(M3TracingFilter.Config(
                tracer = tracer, // User can override bean definition
                shutdownTracer = false // M3Tracer will be closed by ApplicationContext.
        ))
    }

    /**
     * Configure [M3TracingFilter].
     * User can override setting with spring boot properties (as defined in `@Value`).
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("m3.tracing.filter.enable", matchIfMissing = true)
    fun m3TracingFilterRegistration(
            filter: M3TracingFilter,
            @Value("\${m3.tracing.filter.order:${Ordered.HIGHEST_PRECEDENCE}}")
            order: Int,
            @Value("\${m3.tracing.filter.urlPatterns:/*}")
            urlPatterns: List<String>
    ) = FilterRegistrationBean<M3TracingFilter>(filter).also {
        it.order = order
        it.urlPatterns = urlPatterns
    }
}
