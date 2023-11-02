package com.example.teeya_li

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myruns.R
import com.example.teeya_li.database.HistoryDatabase
import com.example.teeya_li.database.HistoryDatabaseDao
import com.example.teeya_li.database.HistoryRepository
import com.example.teeya_li.database.HistoryViewModel
import com.example.teeya_li.database.HistoryViewModelFactory

class HistoryDetails : AppCompatActivity() {

    private lateinit var database: HistoryDatabase
    private lateinit var databaseDao: HistoryDatabaseDao
    private lateinit var repository: HistoryRepository
    private lateinit var viewModelFactory: HistoryViewModelFactory
    private lateinit var historyViewModel: HistoryViewModel

    private var activity_type_options = arrayOf(
        "Running",
        "Walking",
        "Standing",
        "Cycling",
        "Hiking",
        "Downhill Skiing",
        "Cross-Country Skiing",
        "Snowboarding",
        "Skating",
        "Swimming",
        "Mountain Biking",
        "Wheelchair",
        "Elliptical",
        "Other"
    )

    private var databaseID: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setTitle("MyRuns")
        toolbar.setTitleTextColor(Color.WHITE)

        database = HistoryDatabase.getInstance(this)
        databaseDao = database.historyDatabaseDao
        repository = HistoryRepository(databaseDao)
        viewModelFactory = HistoryViewModelFactory(repository)
        historyViewModel = ViewModelProvider(this, viewModelFactory).get(HistoryViewModel::class.java)

        val delBtn = findViewById<ImageButton>(R.id.btnDelete)
        delBtn.setOnClickListener {
            deleteEntry()
        }

        // Retrieve the values passed from the previous activity
        val selectedEntryExtras = intent.getParcelableArrayListExtra<Bundle>("selectedEntry")

        if (selectedEntryExtras != null) {
            for (bundle in selectedEntryExtras) {
                val key = bundle.getString("key")
                val value = bundle.getString("value")
                when (key) {
                    "databaseID" ->{
                        if (value != null) {
                            databaseID = value.toLong()
                            Toast.makeText(this, "$databaseID", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "Input Type" -> {
                        val updatedValue =
                            if (key == "Input Type" && value == "0") "Manual Input" else value
                        findViewById<TextView>(R.id.InputTypeTV).text = updatedValue
                    }

                    "Activity Type" -> {
                        // Convert the integer value to the corresponding activity string
                        val activityType = try {
                            activity_type_options[value?.toInt() ?: -1]
                        } catch (e: NumberFormatException) {
                            "Unknown"
                        }
                        findViewById<TextView>(R.id.ActivityTypeTV).text = activityType
                    }

                    "Date and Time" -> findViewById<TextView>(R.id.DateTimeTV).text = value
                    "Duration" -> findViewById<TextView>(R.id.DurationTV).text = value
                    "Distance" -> findViewById<TextView>(R.id.DistanceTV).text = value
                    "Calories" -> findViewById<TextView>(R.id.CaloriesTV).text = value
                    "Heart Rate" -> findViewById<TextView>(R.id.HeartRateTV).text = value
                }
            }
        }
    }

    private fun deleteEntry() {
        // Check if the `databaseID` is valid, and if it is, call the deleteEntry method in the ViewModel.
        Toast.makeText(this, "$databaseID", Toast.LENGTH_SHORT).show()
        if (databaseID != 0L) {
            val entryLiveData = historyViewModel.getEntryById(databaseID)
            entryLiveData.observe(this) { entry ->
                if (entry != null) {
                    historyViewModel.deleteEntry(entry)
                    Toast.makeText(this, "Entry deleted successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close this activity after deleting the entry
                }
            }
        }
    }
}