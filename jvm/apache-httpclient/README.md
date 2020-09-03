# Apache HttpClient integration

## How to setup

Add `M3TracingHttpInterceptor` as request/response interceptor of HttpClient.
In addition, to close a span whenever failed to send a request, set M3TracingHttpRequestRetryHandler as HttpRequestRetryHandler.

### httpclient 4.2-

```java
private static final M3TracingHttpRequestRetryHandler retryHander = new M3TracingHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler());
```

```java
// CAUTION: Must setup as BOTH interceptor otherwise it may cause memory leak.
httpclient.addRequestInterceptor(M3TracingHttpInterceptor.INSTANCE);
httpclient.addResponseInterceptor(M3TracingHttpInterceptor.INSTANCE);
httpclient.setHttpRequestRetryHandler(retryHander);
```

### httpclient 4.3+

```java
private static final M3TracingHttpRequestRetryHandler retryHander = new M3TracingHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler());
```

```java
CloseableHttpClient httpClient = HttpClientBuilder.create()
    .addInterceptorFirst((HttpRequestInterceptor) M3TracingHttpInterceptor.INSTANCE)
    .addInterceptorLast((HttpResponseInterceptor) M3TracingHttpInterceptor.INSTANCE)
    .setRetryHandler(retryHander)
    .build();
```
