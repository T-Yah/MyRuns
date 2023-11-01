package com.example.teeya_li.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

@Dao
interface HistoryDatabaseDao {

    @Insert
    suspend fun insert(entry: HistoryEntry): Long

    @Query("SELECT * FROM history_table")
    fun getAllEntries(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history_table WHERE id = :entryId")
    fun getEntryById(entryId: Long): Flow<HistoryEntry?>

    @Delete
    suspend fun deleteEntry(entry: HistoryEntry)

    @Query("DELETE FROM history_table")
    suspend fun deleteAll()



}