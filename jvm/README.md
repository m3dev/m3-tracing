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

By default, it uses `com.m3.tracing.tracer.logging.M3LoggingTracer` that just output trace information into SLF4J logs. It is useful for local test or stubbing but not useful for production.

See [M3TracerFactory](core/src/main/kotlin/com/m3/tracing/M3TracerFactory.kt) for detail of SDK loading mechanism.

### Configure SDK

To use OpenCensus, don't forget to look [opencensus/README](opencensus/README.md). You need to set sampling ration explicitly.

## Integrate with your application

Setup one or some of following integrations:

### Spring Boot (`com.m3.tracing:spring-boot`)

See [spring-boot/README](spring-boot/README.md).

### Spring Framework without Spring Boot (`com.m3.tracing:spring-web`)

See [spring-web/README](spring-web/README.md).

### Servlet without any web framework (`com.m3.tracing:servlet`)

See [servlet/README](servlet/README.md).


# Create span by manual

You can create span (element of trace) explicitly as like as:

```java
// note: If you are using framework integration, may able to use DI (e.g. `@Autowired Tracer` in spring-boot)
private static final Tracer tracer = M3TracerFactory.get();

void yourMethod() {
  try(TraceSpan span = tracer.startSpan("do_something")){
    // ... do anything you want ...
  }
}
```

Also you can set custom tag to the span with `Span#set(tagName, value)` method.

## Caution for thread / asynchronous operation

If your application perform operation over threads (e.g. using [Executor](https://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/Executor.html), [Akka](https://akka.io/), ...), you need to propagate tracing context across threads.

Although this library hides context propagation matter as possible, but you need to write a code like this:

```java
private static final Tracer tracer = M3TracerFactory.get();

void yourMethod() {
  // Parent thread should call getCurrentContext() before child thread's task.
  final TraceContext traceContext = tracer.getCurrentContext();
  executor.execute(() -> {
    // In child thread, use traceContext.startChildSpan() to start span in the same context with parent
    try(TraceSpan span = traceContext.startChildSpan("do_something")){
      // ... do anything you want ...
    }
  });
}
```