package com.example.teeya_li

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myruns.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//TODO: handle screen rotations
//TODO: display correct date and time after a rotation
//TODO: in horizontal make list scrollable and fix button layout
//TODO: make alertdialogs close properly after rotation

class ManualInput : AppCompatActivity() {
    private var durationInput = ""
    private var distanceInput = ""
    private var caloriesInput = ""
    private var heartRateInput = ""
    private var commentInput = ""

    private var isDurationOpen = false
    private var isDistanceOpen = false
    private var isCaloriesOpen = false
    private var isHeartRateOpen = false
    private var isCommentOpen = false

    private var isTimePickerDialogOpen = false
    private var isDatePickerDialogOpen = false
    private var selectedHour = 0
    private var selectedMinute = 0
    private var selectedYear = 0;
    private var selectedMonth = 0
    private var selectedDay = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_input)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = "Manual Input"
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(Color.WHITE);

        val optionsListView = findViewById<ListView>(R.id.optionsListView)
        // Create an array of the options
        val options = arrayOf("Date", "Time", "Duration", "Distance", "Calories", "Heart Rate", "Comment")
        // Create an ArrayAdapter to populate the ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        // Set the adapter for the ListView
        optionsListView.adapter = adapter

        savedInstanceState?.let {
            isTimePickerDialogOpen = it.getBoolean("isTimePickerDialogOpen", false)
            isDatePickerDialogOpen = it.getBoolean("isDatePickerDialogOpen", false)
            isDurationOpen = it.getBoolean("isDurationOpen", false)
            isDistanceOpen = it.getBoolean("isDistanceOpen", false)
            isCaloriesOpen = it.getBoolean("isCaloriesOpen", false)
            isHeartRateOpen = it.getBoolean("isHeartRateOpen", false)
            isCommentOpen = it.getBoolean("isCommentOpen", false)


            // Re-display the dialogs if they were open during a rotation
            if (isTimePickerDialogOpen) {
                selectedHour = it.getInt("selectedHour", 0)
                selectedMinute = it.getInt("selectedMinute", 0)
                reloadTimePickerDialog(selectedHour, selectedMinute)
            }
            if (isDatePickerDialogOpen) {
                selectedYear = it.getInt("selectedYear", 0)
                selectedMonth = it.getInt("selectedMonth", 0)
                selectedDay = it.getInt("selectedDay", 0)
                Log.d("dateYear", selectedYear.toString())
                Log.d("dateMonth", selectedMonth.toString())
                Log.d("dateDate", selectedDay.toString())
                reloadDatePickerDialog(selectedYear, selectedMonth, selectedDay)
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

        optionsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = options[position]
            when (position) {
                0 -> {
                    //date
                    isDatePickerDialogOpen = true
                    showDatePickerDialog()
                    //isDatePickerDialogOpen = false
                }
                1 -> {
                    //time
                    isTimePickerDialogOpen = true
                    showTimePickerDialog()
                    //isTimePickerDialogOpen = false
                }
                2 -> {
                    //duration
                    isDurationOpen = true
                    setDuration()
                }
                3 -> {
                    //distance
                    isDistanceOpen = true
                    setDistance()
                }
                4 -> {
                    //calories
                    isCaloriesOpen = true
                    setCalories()
                }
                5 -> {
                    //heart rate
                    isHeartRateOpen = true
                    setHeartRate()
                }
                6 -> {
                    //comment
                    isCommentOpen = true
                    setComment()
                }
            }
            Toast.makeText(this, "Selected Option: $selectedOption", Toast.LENGTH_SHORT).show()
        }

        val saveBtn = findViewById<Button>(R.id.saveButton)
        saveBtn.setOnClickListener{
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
        val cancelBtn = findViewById<Button>(R.id.cancelButton)
        cancelBtn.setOnClickListener{
            Toast.makeText(this, "Entry Discarded", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setComment() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Comment")

        // Set up the input
        val input = EditText(this)
        // Specify the type of input expected; this, for example, sets the input as a password and will mask the text
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "How did it go? Notes here."
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                commentInput = inputValue
            }
            Toast.makeText(this, "Comment: $commentInput", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            isCommentOpen = false
        }

        val dialog = builder.create()

        dialog.setOnDismissListener {
            isCommentOpen = false
        }

        builder.show()
    }

    private fun setHeartRate() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Heart Rate")

        // Set up the input
        val input = EditText(this)
        // Specify the type of input expected; this, for example, sets the input as a password and will mask the text
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Set up the buttons
        val dialog = builder.create()
        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                heartRateInput = inputValue
            }
            Toast.makeText(this, "Heart Rate: $heartRateInput", Toast.LENGTH_SHORT).show()
            isHeartRateOpen = false // Dialog is closed when OK is clicked
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            isHeartRateOpen = false // Dialog is canceled
            dialog.cancel()
        }

        // Set the dialog to be not cancelable by clicking outside of it
        dialog.setCancelable(false)

        dialog.setOnDismissListener {
            isHeartRateOpen = false // Dialog is dismissed
        }

        builder.show()
    }

    private fun setCalories() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Calories")

        // Set up the input
        val input = EditText(this)
        // Specify the type of input expected; this, for example, sets the input as a password and will mask the text
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Set up the buttons
        val dialog = builder.create()
        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                caloriesInput = inputValue
            }
            Toast.makeText(this, "Calories: $caloriesInput", Toast.LENGTH_SHORT).show()
            isCaloriesOpen = false // Dialog is closed when OK is clicked
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            isCaloriesOpen = false // Dialog is canceled
            dialog.cancel()
        }

        // Set the dialog to be not cancelable by clicking outside of it
        dialog.setCancelable(false)

        dialog.setOnDismissListener {
            isCaloriesOpen = false // Dialog is dismissed
        }

        builder.show()
    }



    private fun setDistance() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Distance")

        // Set up the input
        val input = EditText(this)
        // Specify the type of input expected; this, for example, sets the input as a password and will mask the text
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                distanceInput = inputValue
            }
            Toast.makeText(this, "Distance: $distanceInput", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = builder.create()

        dialog.setOnDismissListener {
            isDistanceOpen = false
        }

        builder.show()
    }

    private fun setDuration() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Duration")

        // Set up the input
        val input = EditText(this)
        // Specify the type of input expected; this, for example, sets the input as a password and will mask the text
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                durationInput = inputValue
            }
            Toast.makeText(this, "Duration: $durationInput", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = builder.create()

        dialog.setOnDismissListener {
            isDurationOpen = false
        }

        builder.show()
    }

    private fun reloadTimePickerDialog(hourOfDay: Int, minute: Int) {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)

                // Save the selected time
                this.selectedHour = hourOfDay
                this.selectedMinute = minute

                // Calculate the timestamp by converting hours and minutes to milliseconds
                val timestamp = selectedTime.timeInMillis
                Toast.makeText(this, "Selected Option: $timestamp", Toast.LENGTH_SHORT).show()
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

    private fun reloadDatePickerDialog(year: Int, monthOfYear: Int, dayOfMonth: Int){
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, monthOfYear, dayOfMonth)

                this.selectedYear = year
                this.selectedMonth = monthOfYear
                this.selectedDay = dayOfMonth
                Log.d("dateYearRM", selectedYear.toString())
                Log.d("dateMonthRM", selectedMonth.toString())
                Log.d("dateDateRM", selectedDay.toString())

                // Calculate the timestamp by converting the selected date to milliseconds
                val timestamp = selectedCalendar.timeInMillis
                Toast.makeText(this, "Selected Option: $timestamp", Toast.LENGTH_SHORT).show()
            },
            year,
            monthOfYear,
            dayOfMonth
        )
        datePickerDialog.setOnDismissListener {//dialog dismissed
            isDatePickerDialogOpen = false
        }
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance() //get current date and time
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)

                // Save the selected time
                this.selectedHour = hourOfDay
                this.selectedMinute = minute

                // Calculate the timestamp by converting hours and minutes to milliseconds, stored as a long, dunno if right
               val timestamp = selectedTime.timeInMillis
                Toast.makeText(this, "Selected Option: $timestamp", Toast.LENGTH_SHORT).show()

            },
            hourOfDay,
            minute,
            false // 24-hour format
        )
        timePickerDialog.setOnDismissListener {//dialog dismissed
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
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, monthOfYear, dayOfMonth)

                this.selectedYear = year
                this.selectedMonth = monthOfYear
                this.selectedDay = dayOfMonth
                Log.d("dateYearOG", selectedYear.toString())
                Log.d("dateMonthOG", selectedMonth.toString())
                Log.d("dateDateOG", selectedDay.toString())

                // Calculate the timestamp by converting the selected date to milliseconds
                val timestamp = selectedCalendar.timeInMillis
                Toast.makeText(this, "Selected Option: $timestamp", Toast.LENGTH_SHORT).show()
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.setOnDismissListener {//dialog dismissed
            isDatePickerDialogOpen = false
        }
        datePickerDialog.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the dialog states and selected timestamps in case of rotation
        outState.putBoolean("isTimePickerDialogOpen", isTimePickerDialogOpen)
        outState.putBoolean("isDatePickerDialogOpen", isDatePickerDialogOpen)
        outState.putInt("selectedHour", selectedHour)
        outState.putInt("selectedMinute", selectedMinute)
        outState.putInt("selectedYear", selectedYear)
        outState.putInt("selectedMonth", selectedMonth)
        outState.putInt("selectedDay", selectedDay)
        outState.putBoolean("isDurationOpen", isDurationOpen)
        outState.putBoolean("isDistanceOpen", isDistanceOpen)
        outState.putBoolean("isCaloriesOpen", isCaloriesOpen)
        outState.putBoolean("isHeartRateOpen", isHeartRateOpen)
        outState.putBoolean("isCommentOpen", isCommentOpen)
    }

}