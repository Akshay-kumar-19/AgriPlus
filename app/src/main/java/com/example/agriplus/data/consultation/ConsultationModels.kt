package com.example.agriplus.data.consultation

data class ExpertConsultation(
    val id: String = "",
    val farmerName: String = "Coffee Grower",
    val estateName: String = "Estate Block A",
    val category: String = "Disease Diagnosis & Rust Control",
    val question: String = "",
    val leafImageBase64: String = "",
    val status: String = "Under Agronomist Review",
    val agronomistReply: String = "",
    val agronomistName: String = "Dr. M. S. Rao (Sr. Coffee Agronomist)",
    val timestamp: Long = System.currentTimeMillis()
)
