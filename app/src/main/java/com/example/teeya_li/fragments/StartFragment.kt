package com.example.teeya_li.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.example.myruns.R
import com.example.teeya_li.ManualInput
import com.example.teeya_li.MapActivity

class StartFragment : Fragment() {
    private lateinit var InputSpinner: Spinner
    private lateinit var ActivitySpinner: Spinner
    private var input_type_options = arrayOf("Manual Entry", "GPS", "Automatic")
    private var activity_type_options = arrayOf("Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing",
        "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical",
        "Other")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        // Initialize the spinner & set it up
        InputSpinner = view.findViewById(R.id.input_type_spinner)
        setInputSpinner()

        ActivitySpinner = view.findViewById(R.id.activity_type_spinner)
        setActivitySpinner()

        val startBtn = view.findViewById<Button>(R.id.startBtn)
        startBtn.setOnClickListener{
            val selectedInputPosition = InputSpinner.selectedItemPosition

            if (selectedInputPosition == 0){
                val intent = Intent(requireContext(), ManualInput::class.java) //launch map activity
                startActivity(intent)
            }
            else if (selectedInputPosition == 1 || selectedInputPosition == 2){ //if GPS or Automatic selected
                val intent = Intent(requireContext(), MapActivity::class.java) //launch map activity
                startActivity(intent)
            }
        }

        return view
    }

    private fun setActivitySpinner() {

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activity_type_options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set the created adapter to the Spinner
        ActivitySpinner.adapter = adapter
    }

    private fun setInputSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, input_type_options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set the created adapter to the Spinner
        InputSpinner.adapter = adapter
    }

}
