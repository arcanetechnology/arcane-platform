package no.arcane.platform.utils.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import io.micronaut.gcp.logging.StackdriverJsonLayout

class GcpJsonLayoutWithErrorReporting : StackdriverJsonLayout() {

    override fun toJsonMap(event: ILoggingEvent): MutableMap<String, Any> {
        val jsonMap = super.toJsonMap(event)
        if (event.level.isGreaterOrEqual(Level.ERROR)) {
            jsonMap["@type"] = "type.googleapis.com/google.devtools.clouderrorreporting.v1beta1.ReportedErrorEvent"
        }
        return jsonMap
    }
}