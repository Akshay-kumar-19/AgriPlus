package com.example.agriplus.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.example.agriplus.utils.ReminderReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReminderManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("agriplus_reminders_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val db = FirebaseDatabase.getInstance().reference

    private fun getUserRef() =
        db.child("users").child(FirebaseAuth.getInstance().currentUser?.uid ?: "guest").child("reminders")

    fun getReminders(callback: (List<TreatmentReminder>) -> Unit) {
        val local = getLocalReminders()
        callback(local.sortedBy { it.scheduledTimeMillis })

        // Fetch from Firebase for sync
        getUserRef().get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<TreatmentReminder>()
            for (child in snapshot.children) {
                val r = child.getValue(TreatmentReminder::class.java)
                r?.let { list.add(it) }
            }
            if (list.isNotEmpty()) {
                saveAllLocal(list)
                callback(list.sortedBy { it.scheduledTimeMillis })
            }
        }
    }

    fun addReminder(reminder: TreatmentReminder, onComplete: () -> Unit) {
        val id = if (reminder.id.isEmpty()) "rem_" + System.currentTimeMillis() else reminder.id
        val item = reminder.copy(id = id)

        val current = getLocalReminders().toMutableList()
        current.add(item)
        saveAllLocal(current)

        // Schedule system notification alarm
        scheduleAlarm(item)

        // Sync with Firebase
        getUserRef().child(item.id).setValue(item)
            .addOnCompleteListener { onComplete() }
    }

    fun toggleComplete(id: String, onComplete: () -> Unit) {
        val current = getLocalReminders().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val updated = current[index].copy(isCompleted = !current[index].isCompleted)
            current[index] = updated
            saveAllLocal(current)
            getUserRef().child(id).setValue(updated)
                .addOnCompleteListener { onComplete() }
        } else {
            onComplete()
        }
    }

    fun deleteReminder(id: String, onComplete: () -> Unit) {
        val current = getLocalReminders().filter { it.id != id }
        saveAllLocal(current)
        cancelAlarm(id)
        getUserRef().child(id).removeValue()
            .addOnCompleteListener { onComplete() }
    }

    private fun scheduleAlarm(item: TreatmentReminder) {
        if (item.scheduledTimeMillis <= System.currentTimeMillis() || alarmManager == null) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", item.id.hashCode())
            putExtra("title", item.title)
            putExtra("notes", item.notes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    item.scheduledTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    item.scheduledTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Android 12+ exact alarm permission fallback
            alarmManager.set(AlarmManager.RTC_WAKEUP, item.scheduledTimeMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(id: String) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun getLocalReminders(): List<TreatmentReminder> {
        val json = prefs.getString("reminders_list", null) ?: return defaultReminders()
        return try {
            val type = object : TypeToken<List<TreatmentReminder>>() {}.type
            gson.fromJson(json, type) ?: defaultReminders()
        } catch (e: Exception) {
            defaultReminders()
        }
    }

    private fun saveAllLocal(list: List<TreatmentReminder>) {
        prefs.edit().putString("reminders_list", gson.toJson(list)).apply()
    }

    private fun defaultReminders(): List<TreatmentReminder> {
        val now = System.currentTimeMillis()
        return listOf(
            TreatmentReminder(
                id = "sample_rem_1",
                title = "2nd Hexaconazole Spray for Rust",
                diseaseName = "Coffee Leaf Rust",
                plantationName = "Hilltop Coffee Estate",
                type = ReminderType.SPRAYING,
                scheduledTimeMillis = now + 4L * 24 * 60 * 60 * 1000,
                notes = "Ensure spray nozzles hit undersides of leaves where stomata reside.",
                isCompleted = false
            ),
            TreatmentReminder(
                id = "sample_rem_2",
                title = "Foliar Zinc + Boron Application",
                diseaseName = "Micronutrient Flush",
                plantationName = "Hilltop Coffee Estate",
                type = ReminderType.FERTILIZER,
                scheduledTimeMillis = now + 9L * 24 * 60 * 60 * 1000,
                notes = "Apply during morning calm hours (wind < 15 km/h).",
                isCompleted = false
            )
        )
    }
}
