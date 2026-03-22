package com.example.agriplus.data

data class HistoryItem(
    val result: String = "",
    val confidence: Double = 0.0,
    val imageUri: String = "",
    val timestamp: Long = 0L
)