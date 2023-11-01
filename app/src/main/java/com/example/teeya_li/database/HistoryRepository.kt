package com.example.teeya_li.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HistoryRepository (private val historyDao: HistoryDatabaseDao){
    // Get all exercise entries from the database in a flow format.
    val allHistory: Flow<List<HistoryEntry>> = historyDao.getAllEntries()

    // Insert a new exercise entry into the database.
    fun insert(entry: HistoryEntry) {
        CoroutineScope(IO).launch {
            historyDao.insert(entry)
        }
    }

    // Retrieve an exercise entry from the database by its ID.
    fun getEntryById(entryId: Long): Flow<HistoryEntry?> {
        return historyDao.getEntryById(entryId)
    }

    // Delete an exercise entry from the database.
    fun delete(entry: HistoryEntry) {
        CoroutineScope(IO).launch {
            historyDao.deleteEntry(entry)
        }
    }

    // Delete all exercise entries from the database.
    fun deleteAll() {
        CoroutineScope(IO).launch {
            historyDao.deleteAll()
        }
    }
}