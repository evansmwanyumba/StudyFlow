package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    private val fullDisplayFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    fun todayDateString(): String {
        return dateFormat.format(Date())
    }

    fun formatDate(cal: Calendar): String {
        return dateFormat.format(cal.time)
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr) ?: return dateStr
            displayFormat.format(date)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatFullDisplayDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr) ?: return dateStr
            fullDisplayFormat.format(date)
        } catch (_: Exception) {
            dateStr
        }
    }

    /**
     * Converts Java Calendar DAY_OF_WEEK (Sun=1, Mon=2, ...)
     * to ISO standard 1=Mon, ..., 7=Sun
     */
    fun currentDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        return when (dow) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    /**
     * Given an ISO day of week (1=Mon..7=Sun) for current week,
     * returns date string "yyyy-MM-dd"
     */
    fun getDateStringForDayOfWeek(targetDayOfWeek: Int): String {
        val cal = Calendar.getInstance()
        val currentDay = currentDayOfWeek()
        val diff = targetDayOfWeek - currentDay
        cal.add(Calendar.DAY_OF_YEAR, diff)
        return dateFormat.format(cal.time)
    }

    /**
     * Returns list of dates for current Monday-Sunday week
     */
    fun getCurrentWeekDates(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        for (day in 1..7) {
            list.add(Pair(day, getDateStringForDayOfWeek(day)))
        }
        return list
    }

    fun dayNameFromNumber(day: Int): String {
        return when (day) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Day $day"
        }
    }

    fun shortDayName(day: Int): String {
        return when (day) {
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            7 -> "Sun"
            else -> "Day"
        }
    }
}
