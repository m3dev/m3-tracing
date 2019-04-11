# OpenCensus SDK

## Sampling ratio setting (explicit setting required)

You can controll sampling ratio of tracing. Higher sampling ratio covers most of incoming requests.

If your application is an non-root of a trace (get API/RPC request from traced system), no need to set sampling ratio. Because sampling ratio is controlled by root of trace. In other word, if your application may receive request from external (non-traced) application, you have to set sampling ratio.

In general, it is better to explicitly set sampling ratio always to prevent unexpected behavior.

To set sampling ratio, set setting value into `M3_TRACER_OPENCENSUS_SAMPLING` environment variable or `m3.tracer.opencensus.sampling` JVM system property.

Possible values are:

- Decimal `0.0` to `1.0`
- `always`
- `never`

Default value is `never` to prevent unexpected traces, so that you need to specify value explicitly to enable tracing.

## Side effect of `io.grpc.Context`

If your application is using `io.grpc.Context`, please be careful because `opencensus-java` library effects it. `opencensus-java` need to share `io.grpc.Context` across threads if your application uses multi-thread / async programming. So that `opencensus-java` (and this wrapper library) effects `io.grpc.Context`.

## More information

See [M3OpenCensusTracer](src/main/kotlin/com/m3/tracing/tracer/opencensus/M3OpenCensusTracer.kt) for detail.