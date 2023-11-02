package com.example.teeya_li.database

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    val allEntriesLiveData: LiveData<List<HistoryEntry>> = repository.allHistory.asLiveData()

    fun insert(entry: HistoryEntry) {
        viewModelScope.launch {
            repository.insert(entry)
        }
    }

    fun deleteEntry(entry: HistoryEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun getEntryById(entryId: Long): LiveData<HistoryEntry?> {
        return repository.getEntryById(entryId).asLiveData()
    }
}

class HistoryViewModelFactory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}