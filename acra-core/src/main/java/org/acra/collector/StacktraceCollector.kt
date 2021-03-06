/*
 *  Copyright 2016
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.collector

import android.content.Context
import android.text.TextUtils
import com.google.auto.service.AutoService
import org.acra.ReportField
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer

/**
 * Collects the holy stacktrace
 *
 * @author F43nd1r
 * @since 4.9.1
 */
@AutoService(Collector::class)
class StacktraceCollector : BaseReportFieldCollector(ReportField.STACK_TRACE, ReportField.STACK_TRACE_HASH) {
    override val order: Collector.Order
        get() = Collector.Order.FIRST

    override fun collect(reportField: ReportField, context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder, target: CrashReportData) {
        when (reportField) {
            ReportField.STACK_TRACE -> target.put(ReportField.STACK_TRACE, getStackTrace(reportBuilder.message, reportBuilder.exception))
            ReportField.STACK_TRACE_HASH -> target.put(ReportField.STACK_TRACE_HASH, getStackTraceHash(reportBuilder.exception))
            else -> throw IllegalArgumentException()
        }
    }

    override fun shouldCollect(context: Context, config: CoreConfiguration, collect: ReportField, reportBuilder: ReportBuilder): Boolean {
        return collect == ReportField.STACK_TRACE || super.shouldCollect(context, config, collect, reportBuilder)
    }

    private fun getStackTrace(msg: String?, th: Throwable?): String {
        val result: Writer = StringWriter()
        PrintWriter(result).use { printWriter ->
            if (msg != null && !TextUtils.isEmpty(msg)) {
                printWriter.println(msg)
            }
            th?.printStackTrace(printWriter)
            return result.toString()
        }
    }

    private fun getStackTraceHash(th: Throwable?): String {
        val res = StringBuilder()
        var cause = th
        while (cause != null) {
            val stackTraceElements = cause.stackTrace
            for (e in stackTraceElements) {
                res.append(e.className)
                res.append(e.methodName)
            }
            cause = cause.cause
        }
        return Integer.toHexString(res.toString().hashCode())
    }
}