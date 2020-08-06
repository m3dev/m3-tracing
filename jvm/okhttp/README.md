# OkHttp integration

## How to setup

Add `M3TracingInterceptor` as interceptor of OkHttpClient.

```kotlin
OkHttpClient.Builder().addInterceptor(M3TracingInterceptor(tracer)).build()
```
