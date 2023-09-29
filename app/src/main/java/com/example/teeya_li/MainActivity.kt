package com.example.teeya_li

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.myruns.R
import com.example.teeya_li.fragments.HistoryFragment
import com.example.teeya_li.fragments.SettingsFragment
import com.example.teeya_li.fragments.StartFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var startFragment: StartFragment
    private lateinit var historyFragment: HistoryFragment
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var myFragmentStateAdapter: MyFragmentStateAdapter

    private var fragments = ArrayList<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_activity)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setTitle("MyRuns")
        toolbar.setTitleTextColor(Color.WHITE);

        viewPager2 = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tabLayout)

        startFragment = StartFragment()
        historyFragment = HistoryFragment()
        settingsFragment = SettingsFragment()

        fragments = ArrayList()
        fragments.add(startFragment)
        fragments.add(historyFragment)
        fragments.add(settingsFragment)

        myFragmentStateAdapter = MyFragmentStateAdapter(this, fragments)
        viewPager2.adapter = myFragmentStateAdapter

        val tabTitles = arrayOf("Start", "History", "Settings")

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            tab.text = tabTitles[position]
        }

        val tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabLayoutMediator.attach()

    }
}
