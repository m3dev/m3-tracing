package com.m3.tracing.jdbc.p6spy

import com.m3.tracing.M3TracerFactory
import com.m3.tracing.TraceSpan
import com.p6spy.engine.common.StatementInformation
import com.p6spy.engine.event.SimpleJdbcEventListener
import java.sql.SQLException

open class M3TracingJdbcEventListener: SimpleJdbcEventListener() {
    companion object {
        private val currentSpan = ThreadLocal<TraceSpan>()
    }

    // Allow override by unittest / subclasses
    protected val tracer = M3TracerFactory.get()

    override fun onBeforeAnyExecute(statementInformation: StatementInformation) {
        val span = tracer.startSpan("SQL")
        currentSpan.set(span) // Set to ThreadLoacal ASAP to prevent leak
        doQuietly {
            span["sql"] = statementInformation.statementQuery
        }
    }

    override fun onAfterAnyExecute(statementInformation: StatementInformation, timeElapsedNanos: Long, e: SQLException?) {
        val span = currentSpan.get() ?: return
        doQuietly { // Must proceed to close() statement to prevent leak
            span["time_elapsed_nanos"] = timeElapsedNanos
            if (e != null) {
                span.setError(e)
                span["sql_error_code"] = e.errorCode
            }
        }
        span.close()
    }

    /**
     * `On Error Resume Next` in 21st century.
     */
    protected inline fun doQuietly(action: () -> Unit) {
        try {
            action()
        } catch (e: Throwable) {
            // Intentionally no log output to prevent massive log output
        }
    }
}
