package com.ecofresh.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ExpiryNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        val database = Room.databaseBuilder(
            applicationContext,
            EcoFreshDatabase::class.java,
            "ecofresh_database"
        ).build()

        val products = database.productDao().getAllProducts()

        for (product in products) {

            val daysRemaining =
                ExpiryStatus.getDaysRemaining(product.expiryDate)

            if ((daysRemaining != null) && (daysRemaining <= 7)) {

                showNotification(
                    product.productName,
                    product.expiryDate,
                    daysRemaining
                )
            }
        }

        database.close()

        return Result.success()
    }

    private fun showNotification(
        productName: String,
        expiryDate: String,
        daysRemaining: Long
    ) {

        if (
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationManager =
            applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channelId = "expiry_notifications"

        val channel = NotificationChannel(
            channelId,
            "Expiry Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationManager.createNotificationChannel(channel)

        val message = when {

            daysRemaining < 0 ->
                "$productName has expired. Expiry date: $expiryDate"

            daysRemaining == 0L ->
                "$productName expires today. Expiry date: $expiryDate"

            daysRemaining == 1L ->
                "$productName expires tomorrow. Expiry date: $expiryDate"

            else ->
                "$productName expires in $daysRemaining days. Expiry date: $expiryDate"
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            channelId
        )
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("EcoFresh Expiry Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            productName.hashCode(),
            notification
        )
    }
}