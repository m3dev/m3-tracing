# Servlet integration

If you are using Spring Boot or Spring Framework, consider to use [spring-boot](../spring-boot) integration or [spring-web](../spring-web) integration.

## Setvlet Filter setup

To trace incoming HTTP request, setup `com.m3.tracing.tracer.servlet.M3TracingFilter` in your serlvet.

Snippet:

```xml
<web-app>
  <filter>
    <filter-name>M3TracingFilter</filter-name>
    <filter-class>com.m3.tracing.tracer.servlet.M3TracingFilter</filter-class>
  </filter>

  <filter-mapping>
    <filter-name>M3TracingFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
</web-app>
```

For more detail, see [M3TracingFilter](src/main/kotlin/com/m3/tracing/tracer/servlet/M3TracingFilter.kt).

## Tracer shutdown setting (optional)

By default, M3TracingFilter shutdown tracing system when servlet container unloads. If you need to keep tracing system running regardless of servlet lifecycle, set `shutdown_tracer` filter parameter as `false` (`true` by default).

Caution: If you set `shutdown_tracer` as `false`, you need to shutdown tracer properly. Otherwise it may cause ClassLoader leak due to running tracer system (especially background threads).
