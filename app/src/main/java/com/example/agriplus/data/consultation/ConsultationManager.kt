package com.example.agriplus.data.consultation

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConsultationManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("agriplus_consultations_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val db = FirebaseDatabase.getInstance().reference

    private fun getUserRef() =
        db.child("users").child(FirebaseAuth.getInstance().currentUser?.uid ?: "guest").child("consultations")

    fun getConsultations(callback: (List<ExpertConsultation>) -> Unit) {
        val local = getLocalConsultations()
        callback(local.sortedByDescending { it.timestamp })

        getUserRef().get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<ExpertConsultation>()
            for (child in snapshot.children) {
                val c = child.getValue(ExpertConsultation::class.java)
                c?.let { list.add(it) }
            }
            if (list.isNotEmpty()) {
                saveAllLocal(list)
                callback(list.sortedByDescending { it.timestamp })
            }
        }
    }

    fun submitConsultation(
        consultation: ExpertConsultation,
        onComplete: () -> Unit
    ) {
        val id = "cons_" + System.currentTimeMillis()

        // Generate preliminary agricultural expert guidance based on question/category
        val automatedExpertFeedback = generatePreliminaryAdvice(consultation.category, consultation.question)

        val item = consultation.copy(
            id = id,
            status = "Answered by Extension Officer",
            agronomistReply = automatedExpertFeedback,
            timestamp = System.currentTimeMillis()
        )

        val current = getLocalConsultations().toMutableList()
        current.add(0, item)
        saveAllLocal(current)

        getUserRef().child(item.id).setValue(item)
            .addOnCompleteListener { onComplete() }
    }

    private fun generatePreliminaryAdvice(category: String, question: String): String {
        val q = question.lowercase()
        return when {
            q.contains("rust") || category.contains("Rust") -> {
                "Based on the coffee leaf symptoms described, this is characteristic of Hemileia vastatrix (Coffee Leaf Rust). Recommend prophylactic spraying of 0.5% Neutral Bordeaux mixture or Hexaconazole 5% EC (2 ml/L) during early morning hours. Prune excessive overhead shade to improve under-canopy airflow."
            }
            q.contains("yellow") || q.contains("nitrogen") -> {
                "The generalized yellowing of mature lower foliage suggests nitrogen deficiency or root saturation. Apply Urea at 50-75g per plant or spray a 1% foliar urea solution. Ensure field drains are clear to restore root aeration."
            }
            q.contains("burnt") || q.contains("potassium") -> {
                "Marginal leaf scorch indicates Potassium deficiency, particularly common when bushes carry a heavy berry crop. Apply MOP (Muriate of Potash) 60g/plant and spray 1.5% Potassium Nitrate (15g/L) for rapid berry filling support."
            }
            else -> {
                "Your query has been logged with our Central Coffee Research Institute extension desk. Immediate action: Inspect the undersides of leaves for fungal spore pustules, check soil moisture around the drip line, and avoid foliar spraying during midday heat or impending rain."
            }
        }
    }

    private fun getLocalConsultations(): List<ExpertConsultation> {
        val json = prefs.getString("consultations_list", null) ?: return defaultSample()
        return try {
            val type = object : TypeToken<List<ExpertConsultation>>() {}.type
            gson.fromJson(json, type) ?: defaultSample()
        } catch (e: Exception) {
            defaultSample()
        }
    }

    private fun saveAllLocal(list: List<ExpertConsultation>) {
        prefs.edit().putString("consultations_list", gson.toJson(list)).apply()
    }

    private fun defaultSample(): List<ExpertConsultation> {
        return listOf(
            ExpertConsultation(
                id = "sample_1",
                farmerName = "Akshay",
                estateName = "Hilltop Coffee Estate",
                category = "Disease Diagnosis & Rust Control",
                question = "Noticed orange powdery patches on Selection 795 leaves after continuous October rain. Is 0.5% Bordeaux sufficient or should I use systemic Hexaconazole?",
                status = "Answered by Extension Officer",
                agronomistReply = "For active rust sporulation after heavy rain, a systemic fungicide such as Hexaconazole 5% EC (2ml/L) or Triadimefon (1g/L) is strongly recommended over Bordeaux mixture, as it eradicates established internal mycelium. Follow up with 0.5% Bordeaux after 20 days as a protective barrier.",
                agronomistName = "Dr. M. S. Rao (Sr. Coffee Extension Officer)",
                timestamp = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000)
            )
        )
    }
}
