# Apache HttpClient integration

## How to setup

Add `M3TracingHttpInterceptor` as request/response interceptor of HttpClient.

### httpclient 4.2-

```java:
// CAUTION: Must setup as BOTH interceptor otherwise it may cause memory leak.
httpclient.addRequestInterceptor(M3TracingHttpInterceptor.getINSTANCE());
httpclient.addResponseInterceptor(M3TracingHttpInterceptor.getINSTANCE());
```

### httpclient 4.3+

```java
CloseableHttpClient httpClient = HttpClientBuilder.create()
    .addInterceptorFirst((HttpRequestInterceptor) M3TracingHttpInterceptor.getINSTANCE())
    .addInterceptorLast((HttpResponseInterceptor) M3TracingHttpInterceptor.getINSTANCE())
    .build();
```
                }