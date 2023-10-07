package com.example.teeya_li

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.ArrayList

//Adapted from class material
class MyFragmentStateAdapter(activity: FragmentActivity, var list: ArrayList<Fragment>)
    : FragmentStateAdapter(activity){

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }
}