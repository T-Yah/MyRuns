package com.example.teeya_li.fragments

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.myruns.R
import com.example.teeya_li.database.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryListAdapter(private val context: Context, private var historyList: List<HistoryEntry>) : BaseAdapter(){

    private var activity_type_options = arrayOf("Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing",
        "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical",
        "Other")

    override fun getItem(position: Int): Any {
        return historyList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return historyList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.adapter,null)

        //grab xml elements
        val entryType = view.findViewById(R.id.EntryType) as TextView
        val activityType = view.findViewById(R.id.ActivityType) as TextView
        val dateTime = view.findViewById(R.id.datetime) as TextView
        val distance = view.findViewById(R.id.Distance) as TextView
        val duration = view.findViewById(R.id.Duration) as TextView

        //populate the listview elements with information from the database
        val entryTypeValue = historyList.get(position).inputType
        if (entryTypeValue == 0) {
            entryType.text = "Manual Entry"
        }
        if (entryTypeValue == 1){
            entryType.text = "GPS"
        }
//        if (entryTypeValue == 2){
//            entryType.text = "GPS"
//        }
        activityType.text = getActivityTypeString(historyList.get(position).activityType)

        dateTime.text = historyList.get(position).dateTime.toString()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateTimeText = historyList.get(position)?.dateTime
        if (dateTimeText != null) {
            dateTime.text = sdf.format(dateTimeText.time)
        }

        val distanceValue = historyList.get(position).distance
        distance.text = "Distance: $distanceValue"

        val durationValue = historyList.get(position).duration
        duration.text = "Duration: $durationValue"

        return view
    }

    fun replace(newHistoryList: List<HistoryEntry>){
        historyList = newHistoryList
    }

    //helper function to find and return the selected activity to display the correct string

    private fun getActivityTypeString(activityType: Int): String {
        if (activityType in 0 until activity_type_options.size) {
            return activity_type_options[activityType]
        }
        return "Unknown"
    }

}