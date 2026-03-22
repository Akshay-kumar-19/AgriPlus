package com.example.agriplus.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RealtimeManager {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val dbRef = FirebaseDatabase.getInstance().reference

    fun saveResult(item: HistoryItem) {
        if (userId == null) {
            Log.d("FIREBASE", "User is NULL ❌")
            return
        }

        dbRef.child("users")
            .child(userId)
            .child("history")
            .push()
            .setValue(item)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Data Saved ✅")
            }
            .addOnFailureListener {
                Log.d("FIREBASE", "Save Failed ❌")
            }
    }

    fun getResults(callback: (List<HistoryItem>) -> Unit) {
        if (userId == null) return

        dbRef.child("users")
            .child(userId)
            .child("history")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<HistoryItem>()

                    for (data in snapshot.children) {
                        val item = data.getValue(HistoryItem::class.java)
                        item?.let { list.add(it) }
                    }

                    callback(list.reversed())
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun clearAll() {
        if (userId == null) return

        dbRef.child("users")
            .child(userId)
            .child("history")
            .removeValue()
    }
    fun deleteItem(item: HistoryItem) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("history")

        ref.orderByChild("timestamp")
            .equalTo(item.timestamp.toDouble())
            .get()
            .addOnSuccessListener { snapshot ->

                for (child in snapshot.children) {
                    child.ref.removeValue()
                }
            }
    }
}