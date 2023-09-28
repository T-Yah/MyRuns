package com.example.teeya_li

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.myruns.R



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_activity)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setTitle("MyRuns")
        toolbar.setTitleTextColor(Color.WHITE);


    }
}
