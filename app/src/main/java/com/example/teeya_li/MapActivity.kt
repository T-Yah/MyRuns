package com.example.teeya_li

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.myruns.R

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(Color.WHITE);
    }
}
