package com.fay.currencywidget.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)

        return when {
            minutes < 1 -> "À l'instant"
            minutes < 60 -> "Il y a $minutes min"
            hours < 24 -> "Il y a $hours h"
            else -> {
                // Vérifie si c'était hier
                val yesterday = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                val updateDay = Calendar.getInstance().apply {
                    timeInMillis = timestamp
                }
                if (updateDay.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) &&
                    updateDay.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)
                ) {
                    "Hier ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))}"
                } else {
                    SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(timestamp))
                }
            }
        }
    }
}