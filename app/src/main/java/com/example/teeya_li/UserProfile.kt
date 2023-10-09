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

class UserProfile : AppCompatActivity() {

    private var imagePickerLauncher: ActivityResultLauncher<Intent>? = null
    private var tempImagePath: String? = null
    private var imagePath: String? = null
    private val CAMERA = 0
    private val GALLERY = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.title = "User Profile"

        val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        val unSavedProfile = sharedPref.getBoolean("unsavedProfile", false)

        Log.d("unSavedProfileCreate", unSavedProfile.toString())


        //for testing shared preferences: clear save data
        //val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        //sharedPref.edit().clear().commit()

        //leave this block in
        if (savedInstanceState != null) {
            tempImagePath = sharedPref.getString("temp_image_path", "")
            if (unSavedProfile && !tempImagePath.isNullOrEmpty()){
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
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                var data = result.data
                if (data != null) {
                    val selectedImageUri = data.data
                    if (selectedImageUri != null) {
                        var galleryImage = findViewById<ImageView>(R.id.profilePhoto)

                        // Load the selected image URI into a Bitmap
                        var contentResolver = contentResolver
                        var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)

                        // Set the Bitmap to the ImageView
                        galleryImage.setImageBitmap(bitmap)
                        saveGalleryImage()

                    }
                }
            }
        }


        //On Click Listeners for Buttons
        var cancelBtn = findViewById<Button>(R.id.cancelBtn)
        cancelBtn.setOnClickListener{
            finish()
        }
        var saveBtn = findViewById<Button>(R.id.saveBtn)
        saveBtn.setOnClickListener{
            if (ifSavable()){
                saveProfile()
                finish()
            }
        }
        var changeBtn = findViewById<Button>(R.id.changePfpBtn)
        changeBtn.setOnClickListener{
            changePfpButton()
        }
    }

    //Function to check if there is a previous saved instance, if so --> load it in, if not --> create empty activity
    private fun loadProfile() {
        // Grab Objects on Page
        var nameField = findViewById<EditText>(R.id.enterName)
        var emailField = findViewById<EditText>(R.id.enterEmail)
        var phoneField = findViewById<EditText>(R.id.enterPhone)
        var classField = findViewById<EditText>(R.id.enterClass)
        var majorField = findViewById<EditText>(R.id.enterMajor)
        var radioF = findViewById<RadioButton>(R.id.femaleRadioBtn)
        var radioM = findViewById<RadioButton>(R.id.maleRadioBtn)
        var photoImageView = findViewById<ImageView>(R.id.profilePhoto)

        //reference the shared preferences object
        var sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)

        // Check if there is any previous data
        var isFirstRun = sharedPref.getBoolean("isFirstRun", true)

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
            var name = sharedPref.getString("name", "")
            var email = sharedPref.getString("email", "")
            var phone = sharedPref.getString("phone", "")
            var className = sharedPref.getString("class", "")
            var major = sharedPref.getString("major", "")
            var genderF = sharedPref.getBoolean("female", false)
            var genderM = sharedPref.getBoolean("male", false)

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
        var unSavedProfile = sharedPref.getBoolean("unsavedProfile", false)
        Log.d("unSavedProfileLOADING", unSavedProfile.toString())
        var imagePath: String?

        if (unSavedProfile) {
            imagePath = sharedPref.getString("temp_image_path", "")
        } else {
            imagePath = sharedPref.getString("save_image_path", "")
        }

        if (!imagePath.isNullOrEmpty()) {
            var imageFile = File(imagePath)
            if (imageFile.exists()) {
                var imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                photoImageView.setImageBitmap(imageBitmap)
            }
        }
    }

    //Function to display a selection dialog for profile photo change
    private fun changePfpButton(){

        val options = arrayOf("Take from Camera", "Select from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Profile Image")

        builder.setItems(options) { dialog, which ->
            when (which) {
                CAMERA -> {
                    // Open the camera to take a photo
                    var takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(takePictureIntent, CAMERA)

                }
                GALLERY -> {
                    var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    imagePickerLauncher?.launch(galleryIntent)

                    //saveGalleryImage()
                }
                2 -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun saveGalleryImage(){
        var photoImageView = findViewById<ImageView>(R.id.profilePhoto)
        var sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        var editor = sharedPref.edit()
        var imagePath = saveImageToInternalStorage(photoImageView.drawable.toBitmap(), "temp_profile.png")
        tempImagePath = imagePath

        editor.putString("temp_image_path", imagePath)
        editor.putBoolean("unsavedProfile", true)
        editor.apply()
        var unSavedProfile = sharedPref.getBoolean("unsavedProfile", true)
        Log.d("unSavedProfileGallery", unSavedProfile.toString())
    }

    //Helper function to place the profile image into the imageview after taking a new photo
    //ref: https://www.geeksforgeeks.org/how-to-open-camera-through-intent-and-display-captured-image-in-android/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var photoImageView = findViewById<ImageView>(R.id.profilePhoto)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA && resultCode == Activity.RESULT_OK) {
            // Check if the data Intent is not null
            if (data != null) {
                var photo = data.extras?.get("data") as Bitmap
                photoImageView.setImageBitmap(photo)

                var sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
                var editor = sharedPref.edit()
                var imagePath = saveImageToInternalStorage(
                    photoImageView.drawable.toBitmap(),
                    "temp_profile.png"
                )
                tempImagePath = imagePath

                editor.putString("temp_image_path", imagePath)
                editor.putBoolean("unsavedProfile", true)
                editor.apply()
                var unSavedProfile = sharedPref.getBoolean("unsavedProfile", true)
                Log.d("unSavedProfileCamera", unSavedProfile.toString())
            }
        }
//        else if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
//            Log.d("entered", "null")
//            if (data != null) {
//                // Handle the case when an image is selected from the gallery
//                val selectedImageUri = data.data
//                if (selectedImageUri != null) {
//                    val contentResolver = contentResolver
//                    val bitmap =
//                        MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
//                    photoImageView.setImageBitmap(bitmap)
//
//                    val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
//                    val editor = sharedPref.edit()
//                    val imagePath = saveImageToInternalStorage(bitmap, "temp_profile.png")
//                    tempImagePath = imagePath
//
//                    editor.putString("temp_image_path", imagePath)
//                    editor.putBoolean("unsavedProfile", true)
//                    editor.apply()
//                    val unSavedProfile = sharedPref.getBoolean("unsavedProfile", true)
//                    Log.d("unSavedProfileGallery", unSavedProfile.toString())
//                }
//            }
//        }
    }

    //Function to check if all fields contain data, if so it is savable
    private fun ifSavable() : Boolean {
        //Grab Objects on Page
        var nameField = findViewById<EditText>(R.id.enterName)
        var emailField = findViewById<EditText>(R.id.enterEmail)
        var phoneField = findViewById<EditText>(R.id.enterPhone)
        var classField = findViewById<EditText>(R.id.enterClass)
        var majorField = findViewById<EditText>(R.id.enterMajor)
        var radioF = findViewById<RadioButton>(R.id.femaleRadioBtn)
        var radioM = findViewById<RadioButton>(R.id.maleRadioBtn)
        var email = emailField.text.toString()

        if(TextUtils.isEmpty(nameField.getText().toString())
            || TextUtils.isEmpty(emailField.getText().toString())
            ||TextUtils.isEmpty(phoneField.getText().toString())
            ||TextUtils.isEmpty(classField.getText().toString())
            ||TextUtils.isEmpty(majorField.getText().toString())
            ||(radioF.isChecked == false && radioM.isChecked == false)){
            Toast.makeText(this@UserProfile, "All fields must be filled before saving.", Toast.LENGTH_SHORT).show()
            return false
        }
        else if(emailField.text.toString().contains("@") == false || emailField.text.toString().contains(".") == false){
            Toast.makeText(this@UserProfile, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }
        else{
            return true
        }
    }
    //Function to save inputted data into sharedprefernces
    private fun saveProfile(){
        //Grab Objects on Page
        var nameField = findViewById<EditText>(R.id.enterName)
        var emailField = findViewById<EditText>(R.id.enterEmail)
        var phoneField = findViewById<EditText>(R.id.enterPhone)
        var classField = findViewById<EditText>(R.id.enterClass)
        var majorField = findViewById<EditText>(R.id.enterMajor)
        var radioF = findViewById<RadioButton>(R.id.femaleRadioBtn)
        var radioM = findViewById<RadioButton>(R.id.maleRadioBtn)

        var photoImageView = findViewById<ImageView>(R.id.profilePhoto)
        var imagePath = saveImageToInternalStorage(photoImageView.drawable.toBitmap(), "save_profile.png")

        var sharedPreference = getSharedPreferences("sharedPref", MODE_PRIVATE)
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

        var unSavedProfile = sharedPreference.getBoolean("unsavedProfile", false)
        Log.d("unSavedProfileSave", unSavedProfile.toString())

        Toast.makeText(this@UserProfile, "Data Saved", Toast.LENGTH_SHORT).show()
    }

    //Helper function to store the taken profile photo
    private fun saveImageToInternalStorage(bitmap: Bitmap, name: String): String {
        var contextWrapper = ContextWrapper(applicationContext)
        var directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)
        var file = File(directory, name)

        var stream: FileOutputStream
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

        var sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        var unSavedProfile = sharedPref.getBoolean("unsavedProfile", false)

        if (isChangingConfigurations && unSavedProfile) { //keep temp variables
            // If the device configuration is changing with a temp image, save the actual temporary image path
            var editor = sharedPref.edit()
            editor.putString("temp_image_path", tempImagePath)
            editor.putBoolean("unsavedProfile", true)
            editor.apply()
            Log.d("unSavedProfileONSTOProtate", unSavedProfile.toString())
            Log.d("unSavedProfilePath", tempImagePath.toString())
        }

        else if (isChangingConfigurations && !unSavedProfile) { //rotate with saved data, clear temp
            // Clear the temporary information
            sharedPref.edit()
                .putString("temp_image_path", null)
                .putBoolean("unsavedProfile", false)
                .apply()
            Log.d("unSavedProfileONSTOPquit", unSavedProfile.toString())
            Log.d("unSavedProfilePath", tempImagePath.toString())
        }

        // Check if the user is leaving the app with an unsaved profile
        else if (!isChangingConfigurations && unSavedProfile) { // left with unsaved data, , clear anything temp
            // Clear the temporary information
            sharedPref.edit()
                .putString("temp_image_path", null)
                .putBoolean("unsavedProfile", false)
                .apply()
            Log.d("unSavedProfileONSTOPquit", unSavedProfile.toString())
            Log.d("unSavedProfilePath", tempImagePath.toString())
        }

        else if (!isChangingConfigurations && !unSavedProfile) { //left with saved data, clear anything temp
            // Clear the temporary information
            sharedPref.edit()
                .putString("temp_image_path", null)
                .putBoolean("unsavedProfile", false)
                .apply()
            Log.d("unSavedProfileONSTOPquit", unSavedProfile.toString())
            Log.d("unSavedProfilePath", tempImagePath.toString())
        }
    }
}