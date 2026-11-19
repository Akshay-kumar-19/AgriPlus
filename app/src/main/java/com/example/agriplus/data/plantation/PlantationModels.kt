package com.example.agriplus.data.plantation

data class Plantation(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val variety: String = "Arabica (Selection 795)",
    val ageYears: Int = 5,
    val areaAcres: Double = 5.0,
    val treeCount: Int = 2000,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
