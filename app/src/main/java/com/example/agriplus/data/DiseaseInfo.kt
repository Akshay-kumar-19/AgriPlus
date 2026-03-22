package com.example.agriplus.data

data class DiseaseInfo(
    val name: String,
    val cause: String,
    val symptoms: String,
    val treatment: String
)

object DiseaseRepository {

    val diseaseMap = mapOf(

        // 🌿 HEALTHY
        "healthy" to DiseaseInfo(
            name = "Healthy Leaf 🌿",
            cause = "No disease or deficiency detected",
            symptoms = "Leaf is green, uniform, and fresh",
            treatment = "Maintain proper irrigation, sunlight, and balanced fertilizer schedule."
        ),

        // 🍂 LEAF RUST
        "leaf_rust" to DiseaseInfo(
            name = "Leaf Rust",
            cause = "Fungal infection due to high humidity",
            symptoms = "Orange/brown powdery spots on leaves",
            treatment = "Spray Mancozeb (2g/L) or Copper fungicide every 7 days for 2–3 weeks. Remove infected leaves and avoid excess watering."
        ),

        // 🌫 POWDERY / PHOMA
        "phoma" to DiseaseInfo(
            name = "Phoma Disease",
            cause = "Fungal infection in wet conditions",
            symptoms = "Dark lesions and leaf drying",
            treatment = "Apply Carbendazim (1g/L) spray weekly for 2–3 weeks. Improve drainage and remove affected leaves."
        ),

        // 🐛 MITE
        "mite" to DiseaseInfo(
            name = "Mite Infestation",
            cause = "Tiny insects sucking leaf nutrients",
            symptoms = "Yellowing, curling, webbing under leaves",
            treatment = "Spray Neem oil or Abamectin every 5–7 days for 2 weeks. Wash leaves with water regularly."
        ),

        // 🟡 NITROGEN
        "nitrogen" to DiseaseInfo(
            name = "Nitrogen Deficiency",
            cause = "Lack of nitrogen in soil",
            symptoms = "Yellowing of older leaves",
            treatment = "Apply Urea or Ammonium Sulfate. Use 50–100g per plant. Repeat after 15 days."
        ),

        // 🟢 PHOSPHORUS
        "phosphorus" to DiseaseInfo(
            name = "Phosphorus Deficiency",
            cause = "Low phosphorus availability",
            symptoms = "Dark green leaves, slow growth",
            treatment = "Apply Single Super Phosphate (SSP) or DAP. Mix into soil and water properly."
        ),

        // 🟣 POTASSIUM
        "potassium" to DiseaseInfo(
            name = "Potassium Deficiency",
            cause = "Insufficient potassium",
            symptoms = "Leaf edges turn brown (burnt look)",
            treatment = "Apply Muriate of Potash (MOP). Repeat after 10–15 days."
        ),

        // 🟤 CALCIUM
        "calcium" to DiseaseInfo(
            name = "Calcium Deficiency",
            cause = "Low calcium in soil",
            symptoms = "Leaf deformation and tip burn",
            treatment = "Apply Calcium nitrate spray weekly for 2 weeks."
        ),

        // ⚪ IRON
        "iron" to DiseaseInfo(
            name = "Iron Deficiency",
            cause = "Iron unavailable in alkaline soil",
            symptoms = "Yellow leaves with green veins",
            treatment = "Apply Ferrous sulfate spray (0.5%) every 7 days for 2–3 weeks."
        ),

        // 🟡 MAGNESIUM
        "magnesium" to DiseaseInfo(
            name = "Magnesium Deficiency",
            cause = "Low magnesium",
            symptoms = "Yellowing between veins",
            treatment = "Spray Epsom salt (Magnesium sulfate) 5g/L every 10 days."
        ),

        // 🔴 MANGANESE
        "manganese" to DiseaseInfo(
            name = "Manganese Deficiency",
            cause = "Low manganese levels",
            symptoms = "Yellow spots between veins",
            treatment = "Apply Manganese sulfate spray every 7–10 days."
        ),

        // 🟠 BORON
        "boron" to DiseaseInfo(
            name = "Boron Deficiency",
            cause = "Lack of boron",
            symptoms = "Distorted leaves and poor growth",
            treatment = "Apply Borax (1–2g/L) spray once every 2 weeks."
        )
    )

    fun getInfo(label: String): DiseaseInfo {
        return diseaseMap[label.lowercase()] ?: DiseaseInfo(
            name = label,
            cause = "Unknown condition",
            symptoms = "Not identified",
            treatment = "Consult agricultural expert"
        )
    }
}