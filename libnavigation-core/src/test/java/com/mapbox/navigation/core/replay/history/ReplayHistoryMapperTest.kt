package com.mapbox.navigation.core.replay.history

import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import com.mapbox.base.common.logger.Logger
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplayHistoryMapperTest {

    private val logger: Logger = mockk(relaxUnitFun = true)

    private val replayHistoryMapper = ReplayHistoryMapper(logger = logger)

    @Test
    fun `should map events`() {
        val historyString = """{"events":[{"type":"getStatus","timestamp":1580744200.379,"event_timestamp":1580744198.879556,"delta_ms":0},{"type":"updateLocation","location":{"lat":50.1232182,"lon":8.6343946,"time":1580744199.406,"speed":0.02246818132698536,"bearing":33.55318069458008,"altitude":162.8000030517578,"accuracyHorizontal":14.710000038146973,"provider":"fused"},"event_timestamp":1580744199.407049,"delta_ms":0}],"version":"6.2.1","history_version":"1.0.0"}"""

        val historyEvents = replayHistoryMapper.mapToReplayEvents(historyString)

        assertEquals(2, historyEvents.size)
    }

    @Test
    fun `should map get status values`() {
        val historyString = """{"events":[{"type":"getStatus","timestamp":1580744200.379,"event_timestamp":1580744198.879556,"delta_ms":0},{"type":"updateLocation","location":{"lat":50.1232182,"lon":8.6343946,"time":1580744199.406,"speed":0.02246818132698536,"bearing":33.55318069458008,"altitude":162.8000030517578,"accuracyHorizontal":14.710000038146973,"provider":"fused"},"event_timestamp":1580744199.407049,"delta_ms":0}],"version":"6.2.1","history_version":"1.0.0"}"""

        val historyEvents = replayHistoryMapper.mapToReplayEvents(historyString)

        assertEquals(1580744198.879556, historyEvents[0].eventTimestamp, 0.000001)
        assertTrue(historyEvents[0] is ReplayEventGetStatus)
    }

    @Test
    fun `should map location values`() {
        val historyString = """{"events":[{"type":"getStatus","timestamp":1580744200.379,"event_timestamp":1580744198.879556,"delta_ms":0},{"type":"updateLocation","location":{"lat":50.1232182,"lon":8.6343946,"time":1580744199.406,"speed":0.02246818132698536,"bearing":33.55318069458008,"altitude":162.8000030517578,"accuracyHorizontal":14.710000038146973,"provider":"fused"},"event_timestamp":1580744199.407049,"delta_ms":0}],"version":"6.2.1","history_version":"1.0.0"}"""

        val historyEvents = replayHistoryMapper.mapToReplayEvents(historyString)

        assertEquals(1580744199.407049, historyEvents[1].eventTimestamp, 0.000001)
        (historyEvents[1] as ReplayEventUpdateLocation).location.let {
            assertEquals(50.1232182, it.lat, 0.00000001)
            assertEquals(8.6343946, it.lon, 0.00000001)
            assertEquals(1580744199.406, it.time)
            assertEquals(0.02246818132698536, it.speed)
            assertEquals(33.55318069458008, it.bearing)
            assertEquals(162.8000030517578, it.altitude)
            assertEquals(14.710000038146973, it.accuracyHorizontal)
            assertEquals("fused", it.provider)
        }
    }

    @Test
    fun `should map custom event`() {
        val historyString = """{"events":[{"type":"getStatus","timestamp":1580744200.379,"event_timestamp":1580744198.879556,"delta_ms":0},{"type":"updateLocation","location":{"lat":50.1232182,"lon":8.6343946,"time":1580744199.406,"speed":0.02246818132698536,"bearing":33.55318069458008,"altitude":162.8000030517578,"accuracyHorizontal":14.710000038146973,"provider":"fused"},"event_timestamp":1580744199.407049,"delta_ms":0},{"type":"getStatus","timestamp":1580744213.506,"event_timestamp":1580744212.006626,"delta_ms":0},{"type":"end_transit","properties":1580744212.223,"event_timestamp":1580744212.223644}],"version":"6.2.1","history_version":"1.0.0"}"""
        val replayHistoryMapper = ReplayHistoryMapper(customEventMapper = ExampleCustomEventMapper(), logger = logger)
        val historyEvents = replayHistoryMapper.mapToReplayEvents(historyString)
        assertEquals(4, historyEvents.size)
    }

    @Test
    fun `old versions of history are missing event_timestamp`() {
        val historyString = """{"events":[{"type":"getStatus","timestamp":1551460823.922}],"version":"5.0.0","history_version":"1.0.0"}"""
        val replayHistoryMapper = ReplayHistoryMapper(customEventMapper = ExampleCustomEventMapper(), logger = logger)
        val historyEvents = replayHistoryMapper.mapToReplayEvents(historyString)
        assertEquals(1, historyEvents.size)
    }

    private data class ExampleEndTransitEvent(
        @SerializedName("event_timestamp")
        override val eventTimestamp: Double,
        val properties: Double
    ) : ReplayEventBase

    private class ExampleCustomEventMapper : CustomEventMapper {
        override fun invoke(eventType: String, parameters: LinkedTreeMap<*, *>): ReplayEventBase? {
            return when (eventType) {
                "end_transit" -> ExampleEndTransitEvent(
                    eventTimestamp = parameters["event_timestamp"] as Double,
                    properties = parameters["properties"] as Double
                )
                else -> null
            }
        }
    }
}
