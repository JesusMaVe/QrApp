package com.example.qrapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class QrFolder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)