# Apache HttpClient integration

## How to setup

Add `M3TracingHttpInterceptor` as request/response interceptor of HttpClient.

```java
// CAUTION: Must setup as BOTH interceptor otherwise it may cause memory leak.
httpclient.addRequestInterceptor(M3TracingHttpInterceptor.INSTANCE);
httpclient.addResponseInterceptor(M3TracingHttpInterceptor.INSTANCE);
```
