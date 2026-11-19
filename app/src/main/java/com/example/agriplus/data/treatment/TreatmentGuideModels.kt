package com.example.agriplus.data.treatment

enum class ConditionCategory {
    ALL,
    FUNGAL_DISEASE,
    PEST,
    NUTRIENT_DEFICIENCY,
    HEALTHY,
    FAVORITES
}

data class TreatmentGuideItem(
    val id: String,
    val name: String,
    val scientificName: String,
    val category: ConditionCategory,
    val severity: String, // Mild, Moderate, Severe, Healthy
    val iconEmoji: String,
    val summary: String,
    val description: String,
    val causes: List<String>,
    val symptoms: List<String>,
    val treatmentSteps: List<String>,
    val preventionTechniques: List<String>,
    val fertilizerNutrientGuidance: String,
    val recommendedChemicals: List<String>,
    val organicAlternatives: List<String>,
    var isFavorite: Boolean = false,
    var customUserNote: String = ""
)
