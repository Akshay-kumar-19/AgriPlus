package com.example.agriplus.data

data class HistoryItem(
    val result: String = "",
    val confidence: Float = 0f,
    val imageUri: String = "",
    val timestamp: Long = 0L
)