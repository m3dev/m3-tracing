# How to start

## Dependency

Load following libraries:

- `com.m3.tracing:core`
- `com.m3.tracing:opencensus`
- `com.m3.tracing:spring-boot` (If you are using Spring Boot)
- `com.m3.tracing:spring-web` (If you are using Spring Framework without Spring Boot)
- `com.m3.tracing:servlet` (If you are using Java Servlet without any web framework)

## Initialization

### Load SDK

You need to specify tracing SDK to use.

To use OpenCensus, specify `com.m3.tracing.tracer.opencensus.M3OpenCensusTracer` into `M3_TRACER_FQCN` environment variable or `m3.tracer.fqcn` JVM system property.

By default, it uses `com.m3.tracing.tracer.logging.M3LoggingTracer` which is useful for local test but not useful for production.

See [M3TracerFactory](src/main/kotlin/com/m3/tracing/M3TracerFactory.kt) for detail of SDK loading.

### Setup SDK

For OpenCensus, see [opencensus/README](opencensus/README.md). You need to set sampling ration explicitly.

## Integrate with your application

Setup one or some of following integrations:

### Spring Boot

See [spring-boot/README](spring-boot/README.md).

### Spring Framework without Spring Boot

See [spring-web/README](/README.md).

### Servlet without any web framework

See [servlet/README](/README.md).
