package com.cscareerguide.app.data.bridge

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okio.BufferedSource

internal data class InterviewSseParseResult(
    val content: String,
    val sessionId: String?,
    val done: Boolean,
    val ended: Boolean,
    val serverError: String?,
    val receivedData: Boolean,
)

internal fun parseInterviewSse(
    source: BufferedSource,
    json: Json,
    onEvent: (InterviewStreamEvent) -> Unit,
): InterviewSseParseResult {
    val content = StringBuilder()
    val frameLines = mutableListOf<String>()
    var sessionId: String? = null
    var done = false
    var ended = false
    var serverError: String? = null
    var receivedData = false

    fun dispatch(frame: String) {
        val data = frame.lineSequence()
            .filter { it.startsWith("data:") }
            .map { it.removePrefix("data:").trimStart() }
            .joinToString("\n")
            .trim()
        if (data.isBlank()) return
        receivedData = true
        if (data == "[DONE]") {
            done = true
            onEvent(InterviewStreamEvent.Done)
            return
        }

        val payload = runCatching { json.parseToJsonElement(data) as? JsonObject }
            .getOrNull()
            ?: return
        when (payload.optionalString("type")) {
            "chunk" -> {
                val delta = payload.optionalString("content").orEmpty()
                if (delta.isNotEmpty()) {
                    content.append(delta)
                    onEvent(InterviewStreamEvent.Chunk(delta))
                }
            }
            "audio" -> {
                val audio = payload.optionalString("audio").orEmpty()
                if (audio.isNotEmpty()) onEvent(InterviewStreamEvent.Audio(audio))
            }
            "end" -> {
                sessionId = payload.optionalString("session_id")?.takeIf(String::isNotBlank)
                ended = sessionId != null
                sessionId?.let { onEvent(InterviewStreamEvent.Session(it)) }
            }
            "error" -> {
                serverError = payload.optionalString("content")
                    ?: payload.optionalString("message")
                    ?: "模型生成失败。"
                onEvent(InterviewStreamEvent.ServerError(serverError))
            }
        }
    }

    while (true) {
        val line = source.readUtf8Line() ?: break
        if (line.isEmpty()) {
            dispatch(frameLines.joinToString("\n"))
            frameLines.clear()
        } else {
            frameLines += line
        }
    }
    if (frameLines.isNotEmpty()) dispatch(frameLines.joinToString("\n"))

    return InterviewSseParseResult(
        content = content.toString(),
        sessionId = sessionId,
        done = done,
        ended = ended,
        serverError = serverError,
        receivedData = receivedData,
    )
}
