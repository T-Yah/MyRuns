package com.example.teeya_li

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import com.example.myruns.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var imagePickerLauncher: ActivityResultLauncher<Intent>? = null
    private var tempImagePath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(Color.WHITE);

        val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        val unSavedProfile = sharedPref.getBoolean("unsavedProfile", false)
        Log.d("unSavedProfileCreate", unSavedProfile.toString())


        //for testing shared preferences: clear save data
        //val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        //sharedPref.edit().clear().commit()

        if (savedInstanceState != null) {
            tempImagePath = savedInstanceState.getString("tempImagePath")
            // If tempImagePath is not null, load the temporary image
            if (!tempImagePath.isNullOrEmpty()) {
                val imageFile = File(tempImagePath)
                if (imageFile.exists()) {
                    val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    val photoImageView = findViewById<ImageView>(R.id.profilePhoto)
                    photoImageView.setImageBitmap(imageBitmap)
                }
            }
        }

        loadProfile()

        //Beginning logic for if user selected the gallery option
//        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data = result.data
//                if (data != null) {
//                    val selectedImageUri = data.data
//                    if (selectedImageUri != null) {
//                        val galleryImage = findViewById<ImageView>(R.id.profilePhoto)
//
//                        // Load the selected image URI into a Bitmap
//                        val contentResolver = contentResolver
//                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
//
//                        // Set the Bitmap to the ImageView
//                        galleryImage.setImageBitmap(bitmap)
//                    }
//                }
//            }
//        }

        //Restore the image path in onCreate in case of a screen rotate


        //On Click Listeners for Buttons
        val cancelBtn = findViewById<Button>(R.id.cancelBtn)
        cancelBtn.setOnClickListener{
            finishAffinity()
            //cancelCheck() //implimented incorrect functionality
        }
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        saveBtn.setOnClickListener{
            if (ifSavable()){
                saveProfile()
            }
        }
        val changeBtn = findViewById<Button>(R.id.changePfpBtn)
        changeBtn.setOnClickListener{
            changePfpButton()
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
        val photoImageView = findViewById<ImageView>(R.id.profilePhoto)

        //reference the shared preferences object
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
            val genderF = sharedPref.getBoolean("female", false)
            val genderM = sharedPref.getBoolean("male", false)

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

        // Load the profile photo based on whether it's the temporary or saved image
        val unSavedProfile = sharedPref.getBoolean("unsavedProfile", false)
        val imagePath: String?

        if (unSavedProfile) {
            imagePath = sharedPref.getString("temp_image_path", "")
        } else {
            imagePath = sharedPref.getString("save_image_path", "")
        }

        if (!imagePath.isNullOrEmpty()) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                photoImageView.setImageBitmap(imageBitmap)
            }
        }

//        val unSavedProfile = sharedPref.getBoolean("unsavedProfile", false)
//        val tempImagePath = sharedPref.getString("temp_image_path", "")
//        val saveImagePath = sharedPref.getString("save_image_path", "")
//
//        Log.d("unSavedProfileLoad", unSavedProfile.toString())
//
//        //we took a photo (not saved) and rotated the screen
//        if (unSavedProfile == true){
//            if (tempImagePath?.isNotEmpty() == true) {
//                val imageFile = File(tempImagePath)
//                if (imageFile.exists()) {
//                    val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
//                    photoImageView.setImageBitmap(imageBitmap)
//                }
//            }
//        }
//        //upload the save pfp
//        else if (unSavedProfile == false) {
//            if (saveImagePath?.isNotEmpty() == true) {
//                val imageFile = File(saveImagePath)
//                if (imageFile.exists()) {
//                    val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
//                    photoImageView.setImageBitmap(imageBitmap)
//                }
//            }
//        }
    }

    //Function to display a selection dialog for profile photo change
    private fun changePfpButton(){

        val options = arrayOf("Take from Camera", "Select from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Profile Image")

        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Open the camera to take a photo
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(takePictureIntent, 0)

                }
                1 -> {
                    // Open the gallery to choose a photo
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    imagePickerLauncher?.launch(galleryIntent)
                }
                2 -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    //Helper function to place the profile image into the imageview after taking a new photo
    //ref: https://www.geeksforgeeks.org/how-to-open-camera-through-intent-and-display-captured-image-in-android/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val photoImageView = findViewById<ImageView>(R.id.profilePhoto)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            // Check if the data Intent is not null
            if (data != null) {
                val photo = data.extras?.get("data") as Bitmap
                photoImageView.setImageBitmap(photo)

                val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
                var editor = sharedPref.edit()
                val imagePath = saveImageToInternalStorage(photoImageView.drawable.toBitmap(), "temp_profile.png")
                tempImagePath = imagePath

                editor.putString("temp_image_path", imagePath)
                editor.putBoolean("unsavedProfile", true)
                editor.apply()
                val unSavedProfile = sharedPref.getBoolean("unsavedProfile", true)
                Log.d("unSavedProfileCamera", unSavedProfile.toString())
            }
        }
    }

    //Function to check if all fields contain data, if so it is savable
    private fun ifSavable() : Boolean {
        //Grab Objects on Page
        val nameField = findViewById<EditText>(R.id.enterName)
        val emailField = findViewById<EditText>(R.id.enterEmail)
        val phoneField = findViewById<EditText>(R.id.enterPhone)
        val classField = findViewById<EditText>(R.id.enterClass)
        val majorField = findViewById<EditText>(R.id.enterMajor)
        val radioF = findViewById<RadioButton>(R.id.femaleRadioBtn)
        val radioM = findViewById<RadioButton>(R.id.maleRadioBtn)
        val email = emailField.text.toString()

        if(TextUtils.isEmpty(nameField.getText().toString())
            || TextUtils.isEmpty(emailField.getText().toString())
            ||TextUtils.isEmpty(phoneField.getText().toString())
            ||TextUtils.isEmpty(classField.getText().toString())
            ||TextUtils.isEmpty(majorField.getText().toString())
            ||(radioF.isChecked == false && radioM.isChecked == false)){
            Toast.makeText(this@MainActivity, "All fields must be filled before saving.", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(emailField.text.toString().contains("@") == false || emailField.text.toString().contains(".") == false){
            Toast.makeText(this@MainActivity, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }
        else{
            return true
        }
    }
    //Function to save inputted data into sharedprefernces
    private fun saveProfile(){
        //Grab Objects on Page
        val nameField = findViewById<EditText>(R.id.enterName)
        val emailField = findViewById<EditText>(R.id.enterEmail)
        val phoneField = findViewById<EditText>(R.id.enterPhone)
        val classField = findViewById<EditText>(R.id.enterClass)
        val majorField = findViewById<EditText>(R.id.enterMajor)
        val radioF = findViewById<RadioButton>(R.id.femaleRadioBtn)
        val radioM = findViewById<RadioButton>(R.id.maleRadioBtn)

        val photoImageView = findViewById<ImageView>(R.id.profilePhoto)
        val imagePath = saveImageToInternalStorage(photoImageView.drawable.toBitmap(), "save_profile.png")

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
        editor.putString("save_image_path", imagePath)
        editor.putString("temp_image_path", null)
        editor.putBoolean("unsavedProfile", false)
        editor.apply()

        tempImagePath = null

        val unSavedProfile = sharedPreference.getBoolean("unsavedProfile", false)
        Log.d("unSavedProfileSave", unSavedProfile.toString())

        Toast.makeText(this@MainActivity, "Data Saved", Toast.LENGTH_SHORT).show()
    }

    //Helper function to store the taken profile photo
    private fun saveImageToInternalStorage(bitmap: Bitmap, name: String): String {
        val contextWrapper = ContextWrapper(applicationContext)
        val directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)
        val file = File(directory, name)

        val stream: FileOutputStream
        try {
            stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the temporary image path
        outState.putString("tempImagePath", tempImagePath)
    }

    override fun onStop() {
        super.onStop()

        val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        val unSavedProfile = sharedPref.getBoolean("unsavedProfile", false)

        // Check if the user is leaving the app with an unsaved profile
        if (!isChangingConfigurations && unSavedProfile) {
            // Clear the temporary information
            sharedPref.edit()
                .putString("temp_image_path", null)
                .putBoolean("unsavedProfile", false)
                .apply()
        }
    }

}