# How to start

## Dependency

Load following libraries:

- `com.m3.tracing:core`
- `com.m3.tracing:opencensus`(If you want to use OpenCensus)

Application framework integrations (e.g. integration with Spring Boot) are provided, which are described later.


## Initialization

### Load SDK

You need to specify which tracing SDK to use.

To use OpenCensus, specify `com.m3.tracing.tracer.opencensus.M3OpenCensusTracer` into `M3_TRACER_FQCN` environment variable or `m3.tracer.fqcn` JVM system property.

By default, the library uses `com.m3.tracing.tracer.logging.M3LoggingTracer` that just outputs trace information into SLF4J logs. This setting is useful for local testing or stubbing but not for production purposes.

See [M3TracerFactory](core/src/main/kotlin/com/m3/tracing/M3TracerFactory.kt) for more details of SDK loading mechanisms.

### Configure SDK

To use OpenCensus, don't forget to look at [opencensus/README](opencensus/README.md). You need to set sampling ratio explicitly.

## Integrate with application framework

Setup one or some of following integrations:

### Spring Boot

Load `com.m3.tracing:spring-boot` dependency.

It traces incoming HTTP requests, outgoing HTTP requests (with `RestTemplate`) and so on.

See [spring-boot/README](spring-boot/README.md).

### Spring Framework without Spring Boot (`com.m3.tracing:spring-web`)

Load `com.m3.tracing:spring-web` dependency.

See [spring-web/README](spring-web/README.md).

### Servlet without any web framework (`com.m3.tracing:servlet`)

Load `com.m3.tracing:serlvet` dependency.

See [servlet/README](servlet/README.md).

## Integrate with libraries

### JDBC (trace SQLs of RDBMS)

You can use `com.m3.tracing:jdbc-p6spy` to capture SQLs via any JDBC driver.

See [jdbc-p6spy/README](jdbc-p6spy/README.md).

### Apache HttpClient

You can use `com.m3.tracing:apache-httpclient` to trace outgoing HTTP requests of Apache HttpClient.

See [apache-httpclient/README](apache-httpclient/README.md).


# Create span by manual

You can create span (element of trace) explicitly as like as:

```java
// note: If you are using framework integration, you might be able to use DI (e.g. `@Autowired Tracer` in spring-boot)
private static final Tracer tracer = M3TracerFactory.get();

void yourMethod() {
  // You MUST close span to prevent memory leak.
  // We recommend using try-with-resources (Java) or use (Kotlin).
  try(TraceSpan span = tracer.startSpan("do_something")){
    // ... do anything you want ...

    span.set("name_of_tag", "foobarbaz"); // You can set tag to the Span
  }
}
```

Also you can set custom tag to the span by `Span#set(tagName, value)` method.

## Caution for thread / asynchronous operation

If your application performs an operation over multiple threads (e.g. using [Executor](https://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/Executor.html), [Akka](https://akka.io/), ...), you need to propagate the tracing context across threads.

Although this library hides context propagation matters as possible as it can, you need to write a code like this:

```java
private static final Tracer tracer = M3TracerFactory.get();

void yourMethod() {
  // The parent thread should call getCurrentContext() before child thread's task.
  final TraceContext traceContext = tracer.getCurrentContext();
  executor.execute(() -> {
    // In the child thread, use traceContext.startSpan() to start span in the same context as parent's one
    try(TraceSpan span = traceContext.startSpan("do_something")){
      // ... do something you want ...
    }
  });
}
```