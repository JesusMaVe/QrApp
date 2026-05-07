package com.example.qrapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedQr(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)