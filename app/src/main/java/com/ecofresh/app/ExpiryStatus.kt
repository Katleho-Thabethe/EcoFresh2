package com.ecofresh.app

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object ExpiryStatus {

    private const val DATE_FORMAT = "dd/MM/yyyy"

    fun getStatus(expiryDate: String): String {

        val daysRemaining = getDaysRemaining(expiryDate)

        return when {

            daysRemaining == null ->
                "INVALID DATE"

            daysRemaining < 0 ->
                "EXPIRED"

            daysRemaining <= 7 ->
                "EXPIRING SOON"

            else ->
                "FRESH"
        }
    }

    fun getDaysRemaining(expiryDate: String): Long? {

        return try {

            val formatter = SimpleDateFormat(
                DATE_FORMAT,
                Locale.getDefault()
            )

            formatter.isLenient = false

            val expiry = formatter.parse(expiryDate)
                ?: return null

            // Get today's date at midnight
            val todayCalendar = Calendar.getInstance()

            todayCalendar.set(
                Calendar.HOUR_OF_DAY,
                0
            )

            todayCalendar.set(
                Calendar.MINUTE,
                0
            )

            todayCalendar.set(
                Calendar.SECOND,
                0
            )

            todayCalendar.set(
                Calendar.MILLISECOND,
                0
            )

            val today = todayCalendar.time

            val difference =
                expiry.time - today.time

            TimeUnit.MILLISECONDS.toDays(difference)

        } catch (_: Exception) {

            null
        }
    }
}