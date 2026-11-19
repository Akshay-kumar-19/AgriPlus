package com.example.agriplus.data.plantation

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlantationManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("agriplus_plantations_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val db = FirebaseDatabase.getInstance().reference

    private fun getUserRef(): DatabaseReference {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        return db.child("users").child(userId).child("plantations")
    }

    fun getActivePlantation(): Plantation {
        val json = prefs.getString("active_plantation", null)
        if (json != null) {
            try {
                return gson.fromJson(json, Plantation::class.java)
            } catch (e: Exception) {
                // fallback
            }
        }
        return Plantation(
            id = "default_estate",
            name = "Hilltop Coffee Estate",
            location = "Chikkamagaluru, Karnataka",
            variety = "Arabica (Selection 795)",
            ageYears = 7,
            areaAcres = 12.0,
            treeCount = 5000,
            notes = "Main block under Silver Oak & Dadap shade canopy."
        )
    }

    fun setActivePlantation(plantation: Plantation) {
        prefs.edit().putString("active_plantation", gson.toJson(plantation)).apply()
    }

    fun addPlantation(plantation: Plantation, onComplete: (Boolean) -> Unit) {
        val ref = getUserRef().push()
        val id = ref.key ?: System.currentTimeMillis().toString()
        val itemWithId = plantation.copy(id = id)

        // Save locally first
        saveLocalPlantation(itemWithId)

        ref.setValue(itemWithId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(true) } // Still true because local cache succeeded
    }

    fun updatePlantation(plantation: Plantation, onComplete: (Boolean) -> Unit) {
        saveLocalPlantation(plantation)
        if (plantation.id.isNotEmpty()) {
            getUserRef().child(plantation.id).setValue(plantation)
                .addOnCompleteListener { onComplete(true) }
        } else {
            onComplete(true)
        }
    }

    fun deletePlantation(id: String, onComplete: (Boolean) -> Unit) {
        deleteLocalPlantation(id)
        if (id.isNotEmpty()) {
            getUserRef().child(id).removeValue()
                .addOnCompleteListener { onComplete(true) }
        } else {
            onComplete(true)
        }
    }

    fun getPlantations(callback: (List<Plantation>) -> Unit) {
        // Return local first for speed
        val localList = getLocalPlantations()
        if (localList.isNotEmpty()) {
            callback(localList)
        }

        getUserRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Plantation>()
                for (child in snapshot.children) {
                    val p = child.getValue(Plantation::class.java)
                    p?.let { list.add(it) }
                }

                if (list.isEmpty() && localList.isEmpty()) {
                    val defaultP = getActivePlantation()
                    list.add(defaultP)
                    addPlantation(defaultP) {}
                }

                if (list.isNotEmpty()) {
                    saveAllLocal(list)
                    callback(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (localList.isNotEmpty()) {
                    callback(localList)
                } else {
                    callback(listOf(getActivePlantation()))
                }
            }
        })
    }

    private fun getLocalPlantations(): List<Plantation> {
        val json = prefs.getString("cached_plantations_list", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Plantation>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveAllLocal(list: List<Plantation>) {
        prefs.edit().putString("cached_plantations_list", gson.toJson(list)).apply()
    }

    private fun saveLocalPlantation(plantation: Plantation) {
        val current = getLocalPlantations().toMutableList()
        val index = current.indexOfFirst { it.id == plantation.id }
        if (index >= 0) {
            current[index] = plantation
        } else {
            current.add(plantation)
        }
        saveAllLocal(current)
    }

    private fun deleteLocalPlantation(id: String) {
        val current = getLocalPlantations().filter { it.id != id }
        saveAllLocal(current)
    }
}
