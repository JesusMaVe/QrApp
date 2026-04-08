package com.example.qrapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class ScanRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val contentType: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val parsedContentType: ContentType
        get() = runCatching { ContentType.valueOf(contentType) }.getOrDefault(ContentType.UNKNOWN)
}
