package com.example.teeya_li.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myruns.R
import com.example.teeya_li.database.HistoryDatabase
import com.example.teeya_li.database.HistoryDatabaseDao
import com.example.teeya_li.database.HistoryEntry
import com.example.teeya_li.database.HistoryRepository
import com.example.teeya_li.database.HistoryViewModel
import com.example.teeya_li.database.HistoryViewModelFactory

class HistoryFragment : Fragment() {
    private lateinit var database: HistoryDatabase
    private lateinit var databaseDao: HistoryDatabaseDao
    private lateinit var repository: HistoryRepository
    private lateinit var viewModelFactory: HistoryViewModelFactory
    private lateinit var historyViewModel: HistoryViewModel

    private lateinit var arrayList: ArrayList<HistoryEntry>
    private lateinit var arrayAdapter: HistoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val list = view.findViewById<ListView>(R.id.list)
        database = HistoryDatabase.getInstance(requireActivity())
        databaseDao = database.historyDatabaseDao
        repository = HistoryRepository(databaseDao)
        viewModelFactory = HistoryViewModelFactory(repository)
        historyViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(
            HistoryViewModel::class.java)

        arrayList = ArrayList()
        arrayAdapter = HistoryListAdapter(requireContext(), arrayList)
        list.adapter = arrayAdapter


        historyViewModel.allEntriesLiveData.observe(requireActivity(), Observer { it ->
            arrayAdapter.replace(it)
            arrayAdapter.notifyDataSetChanged()
        })

        return view
    }

}