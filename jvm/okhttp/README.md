# OkHttp integration

## How to setup

Add `M3TracingInterceptor` as interceptor of OkHttpClient.

### Kotlin

```kotlin
OkHttpClient.Builder().addInterceptor(M3TracingInterceptor(tracer)).build()
```

### Java

```java
new OkHttpClient.Builder().addInterceptor(new M3TracingInterceptor(tracer)).build()
```
