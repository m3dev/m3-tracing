# JDBC query tracing with p6spy

This library automatically register [p6spy](https://github.com/p6spy/p6spy) `JdbcEventListener`.

When you use `p6spy`, SQL calls are automatically traced.

## How to enable p6spy

In most usecase, do followings:

1. If your DB connection is controlled by servlet container, put p6spy's jar into classpath
   - Put p6spy.jar into same place with JDBC driver jar
2. Add `p6spy:` prefix to JDBC url
   - e.g. `jdbc:mysql://` -> `jdbc:p6spy:mysql://`
3. If you specified JDBC driver class name in somewhere, replace it with `com.p6spy.engine.spy.P6SpyDriver`
   - e.g. `com.mysql.jdbc.Driver` -> `com.p6spy.engine.spy.P6SpyDriver`
4. Add this JVM system property on startup: `-Dp6spy.config.modulelist=com.p6spy.engine.spy.P6SpyFactory`
   - `p6spy` outputs SQL into file by default (modulelist=P6LogFactory)
   - This option disables P6LogFactory so that it disables SQL log file output
   - See [p6spy document](https://p6spy.readthedocs.io/en/latest/configandusage.html#settings) for detail.

See [p6spy document](https://p6spy.readthedocs.io/en/latest/install.html) for detail.

## Cautions / Limitations

### Casting to driver specific class does not work

[As written in p6spy document](https://github.com/p6spy/p6spy/blob/master/docs/knownissues.md#non-standard-driver-specific-jdbc-methods-are-not-directly-accessible), p6spy wraps JDBC objects so that casting to driver specific class does not work.

See p6spy document for detail and workaround.

