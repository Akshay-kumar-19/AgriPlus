package com.example.agriplus.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryManager(context: Context) {

    private val prefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveResult(result: ScanResult) {
        val list = getResults().toMutableList()
        list.add(0, result)
        saveList(list)
    }

    fun getResults(): List<ScanResult> {
        val json = prefs.getString("data", null) ?: return emptyList()
        val type = object : TypeToken<List<ScanResult>>() {}.type
        return gson.fromJson(json, type)
    }

    fun deleteResult(result: ScanResult) {
        val list = getResults().toMutableList()
        list.remove(result)
        saveList(list)
    }

    fun clearAll() {
        prefs.edit().remove("data").apply()
    }

    private fun saveList(list: List<ScanResult>) {
        val json = gson.toJson(list)
        prefs.edit().putString("data", json).apply()
    }
}