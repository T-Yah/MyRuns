package com.example.teeya_li

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myruns.R
import com.example.teeya_li.database.HistoryDatabase
import com.example.teeya_li.database.HistoryDatabaseDao
import com.example.teeya_li.database.HistoryEntry
import com.example.teeya_li.database.HistoryRepository
import com.example.teeya_li.database.HistoryViewModel
import com.example.teeya_li.database.HistoryViewModelFactory
import java.util.Calendar

class ManualInput : AppCompatActivity() {
    private var durationInput = ""
    private var distanceInput = ""
    private var caloriesInput = ""
    private var heartRateInput = ""
    private var commentInput = ""
    private var selectedDate: Calendar? = null
    private var selectedTime: Calendar? = null
    private var combinedDateTime: Calendar? = null

    //variables use to ensure desired behaviour on screen rotate
    private var isDurationOpen = false
    private var isDistanceOpen = false
    private var isCaloriesOpen = false
    private var isHeartRateOpen = false
    private var isCommentOpen = false
    private var isTimePickerDialogOpen = false
    private var isDatePickerDialogOpen = false

    private lateinit var database: HistoryDatabase
    private lateinit var databaseDao: HistoryDatabaseDao
    private lateinit var repository: HistoryRepository
    private lateinit var viewModelFactory: HistoryViewModelFactory
    private lateinit var historyViewModel: HistoryViewModel
    private var selectedActivityPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        //define layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_input)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = "Manual Input"
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(Color.WHITE)

        //keep tabs on the selected activity type to be able to submit the activity type into the database
        selectedActivityPosition = intent.getIntExtra("activity_position", -1)
        val options =
            arrayOf("Date", "Time", "Duration", "Distance", "Calories", "Heart Rate", "Comment")

        //define database objects
        database = HistoryDatabase.getInstance(this)
        databaseDao = database.historyDatabaseDao
        repository = HistoryRepository(databaseDao)
        viewModelFactory = HistoryViewModelFactory(repository)
        historyViewModel = ViewModelProvider(this, viewModelFactory).get(
            HistoryViewModel::class.java)

        //set up and populate the listview
        val optionsListView = findViewById<ListView>(R.id.optionsListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        optionsListView.adapter = adapter

        //if the screen was rotated, Re-display the dialogs if they were open during a rotation
        savedInstanceState?.let {
            isTimePickerDialogOpen = it.getBoolean("isTimePickerDialogOpen", false)
            isDatePickerDialogOpen = it.getBoolean("isDatePickerDialogOpen", false)
            isDurationOpen = it.getBoolean("isDurationOpen", false)
            isDistanceOpen = it.getBoolean("isDistanceOpen", false)
            isCaloriesOpen = it.getBoolean("isCaloriesOpen", false)
            isHeartRateOpen = it.getBoolean("isHeartRateOpen", false)
            isCommentOpen = it.getBoolean("isCommentOpen", false)

            if (isTimePickerDialogOpen) {
                showTimePickerDialog()
            }
            if (isDatePickerDialogOpen) {
                showDatePickerDialog()

            }
            if (isDurationOpen) {
                setDuration()
            }
            if (isDistanceOpen) {
                setDistance()
            }
            if (isCaloriesOpen) {
                setCalories()
            }
            if (isHeartRateOpen) {
                setHeartRate()
            }
            if (isCommentOpen) {
                setComment()
            }
        }

        //if the user selects an item from the list, open the dialog for that section
        optionsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = options[position]
            when (position) {
                0 -> {
                    isDatePickerDialogOpen = true
                    showDatePickerDialog()
                }

                1 -> {
                    isTimePickerDialogOpen = true
                    showTimePickerDialog()
                }

                2 -> {
                    isDurationOpen = true
                    setDuration()
                }

                3 -> {
                    isDistanceOpen = true
                    setDistance()
                }

                4 -> {
                    isCaloriesOpen = true
                    setCalories()
                }

                5 -> {
                    isHeartRateOpen = true
                    setHeartRate()
                }

                6 -> {
                    isCommentOpen = true
                    setComment()
                }
            }
        }

        val saveBtn = findViewById<Button>(R.id.saveButton)
        saveBtn.setOnClickListener {
            var res = checkValidity() //make sure the entered values are save-able
            if (res == true){
                val entry = HistoryEntry(
                    //create a new history Entry
                    dateTime = combinedDateTime,
                    duration = durationInput?.toDoubleOrNull() ?: 0.0,
                    distance = distanceInput?.toDoubleOrNull() ?: 0.0,
                    calorie = caloriesInput?.toDoubleOrNull() ?: 0.0,
                    heartRate = heartRateInput?.toDoubleOrNull() ?: 0.0,
                    comment = commentInput ?: "",
                    avgPace = 0.0,
                    avgSpeed = 0.0,
                    climb = 0.0,
                    locationList = null,
                    activityType = selectedActivityPosition,
                    inputType = 0
                )
                historyViewModel.insert(entry)
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val cancelBtn = findViewById<Button>(R.id.cancelButton)
        cancelBtn.setOnClickListener{
            Toast.makeText(this, "Entry Discarded", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkValidity(): Boolean {
        // Check if all the inputs are valid
        if (selectedDate != null && selectedTime != null && durationInput != ""
            && distanceInput != "" && caloriesInput != "" && heartRateInput != "" && commentInput != "") {

            val combinedDateTime = Calendar.getInstance()
            combinedDateTime.timeInMillis = selectedDate!!.timeInMillis
            combinedDateTime.set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY))
            combinedDateTime.set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE))

            this.combinedDateTime = combinedDateTime
            return true

        } else {
            Toast.makeText(this, "Not all inputs are valid", Toast.LENGTH_SHORT).show()
            return false
        }
    }



    private fun setComment() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Comments")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "How did it go? Notes here."
        builder.setView(input)
        input.setText(commentInput)

        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                commentInput = inputValue
            }
            dialog.dismiss()
            isCommentOpen = false
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            isCommentOpen = false
            commentInput = ""
            input.setText("")
        }

        val dialog = builder.create()

        dialog.setOnDismissListener {
            isCommentOpen = false
        }

        dialog.show()
    }

    private fun setHeartRate() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Heart Rate")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)
        input.setText(heartRateInput)

        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                heartRateInput = inputValue
            }
            dialog.dismiss()
            isHeartRateOpen = false
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            isHeartRateOpen = false
            heartRateInput = ""
            input.setText("")
        }

        val dialog = builder.create()

        dialog.setOnDismissListener {
            isHeartRateOpen = false
        }

        dialog.show()
    }

    private fun setCalories() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Calories")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)
        input.setText(caloriesInput)

        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                caloriesInput = inputValue
            }
            dialog.dismiss()
            isCaloriesOpen = false
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            isCaloriesOpen = false
            caloriesInput = ""
            input.setText("")
        }

        val dialog = builder.create()

        dialog.setOnDismissListener {
            isCaloriesOpen = false
        }

        dialog.show()
    }

    private fun setDistance() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Distance")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)
        input.setText(distanceInput)

        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                distanceInput = inputValue
            }
            dialog.dismiss()
            isDistanceOpen = false
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            isDistanceOpen = false
            distanceInput = ""
            input.setText("")
        }

        val dialog = builder.create()

        dialog.setOnDismissListener {
            isDistanceOpen = false
        }

        dialog.show()
    }

    private fun setDuration() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Duration")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)
        input.setText(durationInput)

        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                durationInput = inputValue
            }
            dialog.dismiss()
            isDurationOpen = false
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            durationInput = ""
            input.setText("")
            dialog.cancel()
            isDurationOpen = false
        }

        val dialog = builder.create()

        dialog.setOnDismissListener {
            isDurationOpen = false
        }

        dialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                // When the user hits "OK," set the selected time to the chosen time
                selectedTime = selectedTime ?: Calendar.getInstance()
                selectedTime?.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime?.set(Calendar.MINUTE, minute)
            },
            hourOfDay,
            minute,
            false // 24-hour format
        )
        timePickerDialog.setOnDismissListener {
            isTimePickerDialogOpen = false
        }

        timePickerDialog.show()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                // When the user hits "OK," set the selected date to the chosen date
                selectedDate = selectedDate ?: Calendar.getInstance()
                selectedDate?.set(year, monthOfYear, dayOfMonth)
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.setOnDismissListener {
            isDatePickerDialogOpen = false
        }
        datePickerDialog.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the dialog states and selected timestamps in case of rotation
        outState.putBoolean("isTimePickerDialogOpen", isTimePickerDialogOpen)
        outState.putBoolean("isDatePickerDialogOpen", isDatePickerDialogOpen)
        outState.putBoolean("isDurationOpen", isDurationOpen)
        outState.putBoolean("isDistanceOpen", isDistanceOpen)
        outState.putBoolean("isCaloriesOpen", isCaloriesOpen)
        outState.putBoolean("isHeartRateOpen", isHeartRateOpen)
        outState.putBoolean("isCommentOpen", isCommentOpen)
    }
}