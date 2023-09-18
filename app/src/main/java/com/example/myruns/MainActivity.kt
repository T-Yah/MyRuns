package com.example.myruns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //On Click Listeners for Buttons
        val cancelBtn = findViewById<Button>(R.id.cancelBtn);
        cancelBtn.setOnClickListener{
            cancelCheck()
        }
        val saveBtn = findViewById<Button>(R.id.saveBtn);
        saveBtn.setOnClickListener{
            Toast.makeText(this@MainActivity, "You clicked me.", Toast.LENGTH_SHORT).show()
        }
        val changeBtn = findViewById<Button>(R.id.changePfpBtn);
        changeBtn.setOnClickListener{
            Toast.makeText(this@MainActivity, "You clicked me.", Toast.LENGTH_SHORT).show()
        }
    }
    //ref: https://www.javatpoint.com/kotlin-android-alertdialog#:~:text=Builder%20class%20call%20the%20setTitle,neutral%20and%20negative%20action%20respectively.
    private fun cancelCheck(){
        //Make sure user wants to clear with alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Clear Inputted Data")
        builder.setMessage("This will clear all unsaved data. Do you wish to continue")

        builder.setPositiveButton("Yes"){dialogInterface, which ->
            cancelAction() //on yes click
        }
        builder.setNegativeButton("No"){dialogInterface, which ->
            //on no click do nothing
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false) //cannot click out, must select an option
        alertDialog.show()
    }

    private fun cancelAction(){
        //Grab Objects on Page
        val nameField = findViewById<EditText>(R.id.enterName)
        val emailField = findViewById<EditText>(R.id.enterEmail)
        val phoneField = findViewById<EditText>(R.id.enterPhone)
        val classField = findViewById<EditText>(R.id.enterClass)
        val majorField = findViewById<EditText>(R.id.enterMajor)
        val radioF = findViewById<RadioButton>(R.id.femaleRadioBtn)
        val radioM = findViewById<RadioButton>(R.id.maleRadioBtn)

        //Clear all fields
        radioF.setChecked(false)
        radioM.setChecked(false)
        nameField.text.clear()
        emailField.text.clear()
        phoneField.text.clear()
        classField.text.clear()
        majorField.text.clear()
    }
}
