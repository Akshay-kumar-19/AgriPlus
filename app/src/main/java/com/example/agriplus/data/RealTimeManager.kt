package com.example.agriplus.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RealtimeManager {

    private val db = FirebaseDatabase.getInstance().reference

    private fun getUserRef(): DatabaseReference {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        return db.child("users").child(userId).child("history")
    }

    // 🔥 SAVE RESULT
    fun saveResult(item: HistoryItem) {
        val ref = getUserRef().push()
        ref.setValue(item)
    }

    // 🔥 GET RESULTS
    fun getResults(callback: (List<HistoryItem>) -> Unit) {
        getUserRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<HistoryItem>()

                for (data in snapshot.children) {
                    val item = data.getValue(HistoryItem::class.java)
                    item?.let { list.add(it) }
                }

                callback(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 🔥 DELETE ONE
    fun deleteItem(item: HistoryItem) {
        getUserRef().get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                val data = child.getValue(HistoryItem::class.java)
                if (data?.timestamp == item.timestamp) {
                    child.ref.removeValue()
                }
            }
        }
    }

    // 🔥 CLEAR ALL
    fun clearAll() {
        getUserRef().removeValue()
    }

    fun clearHistory() {
        clearAll()
    }
}
