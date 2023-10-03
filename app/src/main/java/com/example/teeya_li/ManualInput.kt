package com.example.teeya_li

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.myruns.R
import java.util.Calendar

class ManualInput : AppCompatActivity() {
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
            }
            else if (position == 3 ){
                //distance
            }
            else if (position == 4 ){
                //calories
            }
            else if (position == 5 ){
                //heart rate
            }
            else if (position == 6 ){
                //comment
            }
            Toast.makeText(this, "Selected Option: $selectedOption", Toast.LENGTH_SHORT).show()
        }
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