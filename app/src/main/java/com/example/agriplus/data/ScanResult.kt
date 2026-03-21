package com.example.agriplus.data

data class ScanResult(
    val disease: String,
    val confidence: Float,   // 🔥 NEW
    val date: String,
    val imageUri: String
)