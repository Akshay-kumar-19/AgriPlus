package com.example.agriplus.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryManager(context: Context) {

    private val prefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveResult(result: ScanResult) {

        val currentList = getResults().toMutableList()
        currentList.add(0, result) // latest on top

        val json = gson.toJson(currentList)
        prefs.edit().putString("data", json).apply()
    }

    fun getResults(): List<ScanResult> {

        val json = prefs.getString("data", null) ?: return emptyList()

        val type = object : TypeToken<List<ScanResult>>() {}.type
        return gson.fromJson(json, type)
    }
}