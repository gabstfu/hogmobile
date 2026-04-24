package com.example.smart_hog

import android.content.Context
import android.content.SharedPreferences

object BatchRepository {
    private const val PREFS_NAME = "batch_prefs"
    private const val KEY_BATCHES = "batches"

    fun getBatches(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val batchSet = prefs.getStringSet(KEY_BATCHES, emptySet())?.toMutableSet() ?: mutableSetOf()
        
        // Define unwanted/test batches to ignore
        val unwanted = setOf("B001", "B002", "B003", "batch001", "batch002")
        
        // Filter out unwanted batches (case-insensitive)
        val filteredList = batchSet.filter { batch ->
            unwanted.none { it.equals(batch, ignoreCase = true) }
        }.sorted()

        // Sync back to prefs if any were removed to keep storage clean
        if (filteredList.size < batchSet.size) {
            prefs.edit().putStringSet(KEY_BATCHES, filteredList.toSet()).apply()
        }
        
        return filteredList
    }

    fun addBatch(context: Context, batchCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val batchSet = prefs.getStringSet(KEY_BATCHES, emptySet())?.toMutableSet() ?: mutableSetOf()
        batchSet.add(batchCode)
        prefs.edit().putStringSet(KEY_BATCHES, batchSet).apply()
    }

    fun removeBatch(context: Context, batchCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val batchSet = prefs.getStringSet(KEY_BATCHES, emptySet())?.toMutableSet() ?: mutableSetOf()
        batchSet.remove(batchCode)
        prefs.edit().putStringSet(KEY_BATCHES, batchSet).apply()
    }

    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_BATCHES).apply()
    }
}
