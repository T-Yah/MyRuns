package com.example.myruns

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)

        //for testing shared preferences: clear save data
//        val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
//        sharedPref.edit().clear().commit()

        loadProfile()

        //On Click Listeners for Buttons
        val cancelBtn = findViewById<Button>(R.id.cancelBtn)
        cancelBtn.setOnClickListener{
            cancelCheck()
        }
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        saveBtn.setOnClickListener{
            if (ifSavable()){
                saveAction()
            }
        }
        val changeBtn = findViewById<Button>(R.id.changePfpBtn)
        changeBtn.setOnClickListener{
            Toast.makeText(this@MainActivity, "You clicked me.", Toast.LENGTH_SHORT).show()
        }
    }

    //Function to check if there is a previous saved instance, if so --> load it in, if not --> create empty activity
    private fun loadProfile() {
        // Grab Objects on Page
        val nameField = findViewById<EditText>(R.id.enterName)
        val emailField = findViewById<EditText>(R.id.enterEmail)
        val phoneField = findViewById<EditText>(R.id.enterPhone)
        val classField = findViewById<EditText>(R.id.enterClass)
        val majorField = findViewById<EditText>(R.id.enterMajor)
        val radioF = findViewById<RadioButton>(R.id.femaleRadioBtn)
        val radioM = findViewById<RadioButton>(R.id.maleRadioBtn)

        //reference the shared prefernce object
        val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)

        // Check if there is any previous data
        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)

        if (isFirstRun) { //if there is no previous data

            //set fields to empty
            nameField.setText("")
            emailField.setText("")
            phoneField.setText("")
            classField.setText("")
            majorField.setText("")
            radioF.isChecked = false
            radioM.isChecked = false

            //Mark so we know it is no longer the first run
            sharedPref.edit().putBoolean("isFirstRun", false).apply()
        }
        else { //we have previous data, must load into fields
            // Retrieve data from SharedPreferences
            val name = sharedPref.getString("name", "")
            val email = sharedPref.getString("email", "")
            val phone = sharedPref.getString("phone", "")
            val className = sharedPref.getString("class", "")
            val major = sharedPref.getString("major", "")
            val genderF = sharedPref.getBoolean("female", true)
            val genderM = sharedPref.getBoolean("male", true)

            // Set the retrieved data in the fields
            nameField.setText(name)
            emailField.setText(email)
            phoneField.setText(phone)
            classField.setText(className)
            majorField.setText(major)
            if (genderF) {
                radioF.isChecked = true
            }
            if (genderM) {
                radioM.isChecked = true
            }
        }
    }

    //ref: https://www.javatpoint.com/kotlin-android-alertdialog#:~:text=Builder%20class%20call%20the%20setTitle,neutral%20and%20negative%20action%20respectively.
    //Function to check if user actually wants to do the cancel option
    private fun cancelCheck(){
        //Make sure user wants to clear with alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Clear Inputted Data")
        builder.setMessage("This will clear all unsaved data. Do you wish to continue?")

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

    //Function to preform the cancel option, will clear all displayed input
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
    //Function to check if all fields contain data, if so it is saveable
    private fun ifSavable() : Boolean {
        //Grab Objects on Page
        val nameField = findViewById<EditText>(R.id.enterName)
        val emailField = findViewById<EditText>(R.id.enterEmail)
        val phoneField = findViewById<EditText>(R.id.enterPhone)
        val classField = findViewById<EditText>(R.id.enterClass)
        val majorField = findViewById<EditText>(R.id.enterMajor)
        val radioF = findViewById<RadioButton>(R.id.femaleRadioBtn)
        val radioM = findViewById<RadioButton>(R.id.maleRadioBtn)

        if(TextUtils.isEmpty(nameField.getText().toString())
            || TextUtils.isEmpty(emailField.getText().toString())
            ||TextUtils.isEmpty(phoneField.getText().toString())
            ||TextUtils.isEmpty(classField.getText().toString())
            ||TextUtils.isEmpty(majorField.getText().toString())
            ||(radioF.isChecked == false && radioM.isChecked == false)) {
            Toast.makeText(this@MainActivity, "All fields must be filled before saving.", Toast.LENGTH_SHORT).show()
            return false
        }
        else{
            return true
        }
    }
    //Function to save inputted data into sharedprefernces
    private fun saveAction(){
        //Grab Objects on Page
        val nameField = findViewById<EditText>(R.id.enterName)
        val emailField = findViewById<EditText>(R.id.enterEmail)
        val phoneField = findViewById<EditText>(R.id.enterPhone)
        val classField = findViewById<EditText>(R.id.enterClass)
        val majorField = findViewById<EditText>(R.id.enterMajor)
        val radioF = findViewById<RadioButton>(R.id.femaleRadioBtn)
        val radioM = findViewById<RadioButton>(R.id.maleRadioBtn)

        val sharedPreference = getSharedPreferences("sharedPref", MODE_PRIVATE)
        var editor = sharedPreference.edit()
        //.getText().toString() changes from edittext to string
        editor.putString("name", nameField.getText().toString())
        editor.putString("email", emailField.getText().toString())
        editor.putString("phone", phoneField.getText().toString())
        editor.putString("class", classField.getText().toString())
        editor.putString("major", majorField.getText().toString())
        editor.putBoolean("female", radioF.isChecked());
        editor.putBoolean("male", radioM.isChecked());
        editor.commit()

        Toast.makeText(this@MainActivity, "Data Saved", Toast.LENGTH_SHORT).show()
    }
}
