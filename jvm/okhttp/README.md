# OkHTTP integration

## How to setup

Add `M3TracingInterceptor` as interceptor of OkHttpClient.

```
OkHttpClient.Builder().addInterceptor(M3TracingInterceptor(tracer)).build()
```
