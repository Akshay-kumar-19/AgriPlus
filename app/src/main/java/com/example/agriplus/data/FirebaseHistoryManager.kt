package com.example.agriplus.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class HistoryItem(
    val result: String = "",
    val confidence: Float = 0f,
    val imageUri: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class FirebaseHistoryManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun saveResult(item: HistoryItem) {
        val userId = getUserId() ?: return

        db.collection("users")
            .document(userId)
            .collection("history")
            .add(item)
            .await()
    }

    suspend fun getResults(): List<HistoryItem> {
        val userId = getUserId() ?: return emptyList()

        val snapshot = db.collection("users")
            .document(userId)
            .collection("history")
            .orderBy("timestamp")
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.toObject(HistoryItem::class.java)
        }
    }

    suspend fun deleteResult(docId: String) {
        val userId = getUserId() ?: return

        db.collection("users")
            .document(userId)
            .collection("history")
            .document(docId)
            .delete()
            .await()
    }

    suspend fun clearAll() {
        val userId = getUserId() ?: return

        val snapshot = db.collection("users")
            .document(userId)
            .collection("history")
            .get()
            .await()

        snapshot.documents.forEach {
            it.reference.delete()
        }
    }
}