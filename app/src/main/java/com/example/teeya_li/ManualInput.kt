package com.example.teeya_li

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myruns.R
import java.util.Calendar

//TODO: handle screen rotations
//TODO: update title on toolbar

class ManualInput : AppCompatActivity() {
    var durationInput = ""
    var distanceInput = ""
    var caloriesInput = ""
    var heartRateInput = ""
    var commentInput = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_input)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(Color.WHITE);

        val optionsListView = findViewById<ListView>(R.id.optionsListView)
        // Create an array of the options
        val options = arrayOf("Date", "Time", "Duration", "Distance", "Calories", "Heart Rate", "Comment")
        // Create an ArrayAdapter to populate the ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        // Set the adapter for the ListView
        optionsListView.adapter = adapter


        // Optionally, you can set an item click listener to handle item clicks
        optionsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = options[position]
            if (position == 0 ){
                //date
                showDatePickerDialog()
            }
            else if (position == 1 ){
                //time
                showTimePickerDialog()
            }
            else if (position == 2 ){
                //duration
                setDuration()
            }
            else if (position == 3 ){
                //distance
                setDistance()
            }
            else if (position == 4 ){
                //calories
                setCalories()
            }
            else if (position == 5 ){
                //heart rate
                setHeartRate()
            }
            else if (position == 6 ){
                //comment
                setComment()
            }
            Toast.makeText(this, "Selected Option: $selectedOption", Toast.LENGTH_SHORT).show()
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
        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                heartRateInput = inputValue
            }
            Toast.makeText(this, "Heart Rate: $heartRateInput", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
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
        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                caloriesInput = inputValue
            }
            Toast.makeText(this, "Calories: $caloriesInput", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
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

        builder.show()
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

                // Calculate the timestamp by converting hours and minutes to milliseconds, stored as a long, dunno if right
                val timestamp = selectedTime.timeInMillis
                Toast.makeText(this, "Selected Option: $timestamp", Toast.LENGTH_SHORT).show()

            },
            hourOfDay,
            minute,
            false // 24-hour format
        )
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

                // Calculate the timestamp by converting the selected date to milliseconds
                val timestamp = selectedCalendar.timeInMillis
                Toast.makeText(this, "Selected Option: $timestamp", Toast.LENGTH_SHORT).show()
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }
}