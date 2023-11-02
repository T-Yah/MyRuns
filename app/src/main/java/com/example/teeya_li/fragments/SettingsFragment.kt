package com.example.teeya_li.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.myruns.R
import com.example.teeya_li.UserProfile


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val userProfile: Preference? = findPreference("userProfile") as Preference?
        if (userProfile != null) { // on user Profile click launch user profile activity
            userProfile.setOnPreferenceClickListener {
                val intent = Intent(activity, UserProfile::class.java)
                startActivity(intent)
                true
            }
        }
        val weblink: Preference? = findPreference("webpage") as Preference?
        if (weblink != null) { // on user Profile click launch user profile activity
            weblink.setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.sfu.ca/computing.html"))
                startActivity(intent)
                true
            }
        }
    }

    //helper function to let the rest of the app know when the user selects a different unit preference
    fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "unitPreference") {
            val unitPreference = sharedPreferences.getString("unitPreference", "metric")
        }
    }
}