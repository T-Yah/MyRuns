package com.example.teeya_li.database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Calendar

class Converters {
    @TypeConverter
    fun fromLatLngList(value: ArrayList<LatLng>?): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(outputStream)
        objectOutputStream.writeObject(value)
        objectOutputStream.close()
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toLatLngList(value: ByteArray?): ArrayList<LatLng>? {
        val inputStream = ByteArrayInputStream(value)
        val objectInputStream = ObjectInputStream(inputStream)
        return objectInputStream.readObject() as? ArrayList<LatLng>
    }

    @TypeConverter
    fun calendarToTimestamp(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun timestampToCalendar(value: Long?): Calendar? {
        return value?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        }
    }
}