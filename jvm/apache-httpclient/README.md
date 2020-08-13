# Apache HttpClient integration

## How to setup

Add `M3TracingHttpInterceptor` as request/response interceptor of HttpClient.
In addition, to close a span whenever failed to send a request, set M3TracingHttpRequestRetryHandler as HttpRequestRetryHandler.

### httpclient 4.2-

```java:
// CAUTION: Must setup as BOTH interceptor otherwise it may cause memory leak.
httpclient.addRequestInterceptor(M3TracingHttpInterceptor.INSTANCE);
httpclient.addResponseInterceptor(M3TracingHttpInterceptor.INSTANCE);
httpclient.setHttpRequestRetryHandler(M3TracingHttpRequestRetryHandler.INSTANCE);
```

### httpclient 4.3+

```java
CloseableHttpClient httpClient = HttpClientBuilder.create()
    .addInterceptorFirst((HttpRequestInterceptor) M3TracingHttpInterceptor.INSTANCE)
    .addInterceptorLast((HttpResponseInterceptor) M3TracingHttpInterceptor.INSTANCE)
    .setRetryHandler(M3TracingHttpRequestRetryHandler.INSTANCE)
    .build();
```
                }