# How to start

## Dependency

Load following libraries:

- `com.m3.tracing:core`
- `com.m3.tracing:opencensus`(If you want to use OpenCensus)

And there are framework/library integrations, describe later.


## Initialization

### Load SDK

You need to specify tracing SDK to use.

To use OpenCensus, specify `com.m3.tracing.tracer.opencensus.M3OpenCensusTracer` into `M3_TRACER_FQCN` environment variable or `m3.tracer.fqcn` JVM system property.

By default, it uses `com.m3.tracing.tracer.logging.M3LoggingTracer` that just output trace information into SLF4J logs. It is useful for local test or stubbing but not useful for production.

See [M3TracerFactory](core/src/main/kotlin/com/m3/tracing/M3TracerFactory.kt) for detail of SDK loading mechanism.

### Configure SDK

To use OpenCensus, don't forget to look [opencensus/README](opencensus/README.md). You need to set sampling ration explicitly.

## Integrate with application framework

Setup one or some of following integrations:

### Spring Boot

Load `com.m3.tracing:spring-boot` dependency.

It traces incoming HTTP request, outgoing HTTP request (with `RestTemplate`) and so on.

See [spring-boot/README](spring-boot/README.md) how to use it.

### Spring Framework without Spring Boot (`com.m3.tracing:spring-web`)

Load `com.m3.tracing:spring-web` dependency.

See [spring-web/README](spring-web/README.md) how to use it.

### Servlet without any web framework (`com.m3.tracing:servlet`)

Load `com.m3.tracing:serlvet` dependency.

See [servlet/README](servlet/README.md) how to use it.

## Integrate with libraries

### JDBC (trace SQLs of RDBMS)

You can use `com.m3.tracing:jdbc-p6spy` to capture SQLs via any JDBC driver.

See [jdbc-p6spy/README](jdbc-p6spy/README.md) how to use it.

### Apache HttpClient

You can use `com.m3.tracing:apache-httpclient` to trace outgoing HTTP request of Apache HttpClient.

See [apache-httpclient/README](apache-httpclient/README.md) how to use it.


# Create span by manual

You can create span (element of trace) explicitly as like as:

```java
// note: If you are using framework integration, may able to use DI (e.g. `@Autowired Tracer` in spring-boot)
private static final Tracer tracer = M3TracerFactory.get();

void yourMethod() {
  // You MUST close span to prevent memory leak.
  // Recommend to use try-with-resources (Java) or use (Kotlin).
  try(TraceSpan span = tracer.startSpan("do_something")){
    // ... do anything you want ...

    span.set("name_of_tag", "foobarbaz"); // You can set tag to the Span
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