package com.example.agriplus.data.reminder

enum class ReminderType {
    SPRAYING,
    INSPECTION,
    FOLLOW_UP_SCAN,
    FERTILIZER
}

data class TreatmentReminder(
    val id: String = "",
    val title: String = "",
    val diseaseName: String = "Coffee Leaf Rust",
    val plantationName: String = "Main Estate",
    val type: ReminderType = ReminderType.SPRAYING,
    val scheduledTimeMillis: Long = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
