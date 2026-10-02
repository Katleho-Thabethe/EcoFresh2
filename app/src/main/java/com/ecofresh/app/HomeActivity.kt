package com.ecofresh.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class HomeActivity : Activity() {

    private lateinit var database: EcoFreshDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                200
            )
        }

        val btnScan = findViewById<Button>(R.id.btnScan)
        val btnProducts = findViewById<Button>(R.id.btnProducts)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        database = Room.databaseBuilder(
            applicationContext,
            EcoFreshDatabase::class.java,
            "ecofresh_database"
        ).build()

        val notificationWork =
            PeriodicWorkRequestBuilder<ExpiryNotificationWorker>(
                1,
                TimeUnit.DAYS
            ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "EcoFreshExpiryCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork
        )

        btnScan.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }

        btnProducts.setOnClickListener {
            val intent = Intent(this, ProductsActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        if (::database.isInitialized) {
            loadExpirySummary()
        }
    }

    private fun loadExpirySummary() {

        CoroutineScope(Dispatchers.IO).launch {

            val products = database.productDao().getAllProducts()

            var expired = 0
            var expiringSoon = 0
            var fresh = 0

            for (product in products) {

                when (ExpiryStatus.getStatus(product.expiryDate)) {

                    "EXPIRED" -> expired++

                    "EXPIRING SOON" -> expiringSoon++

                    "FRESH" -> fresh++
                }
            }

            runOnUiThread {

                val expiredCount =
                    findViewById<TextView>(R.id.expiredCount)

                val expiringSoonCount =
                    findViewById<TextView>(R.id.expiringSoonCount)

                val freshCount =
                    findViewById<TextView>(R.id.freshCount)

                expiredCount.text = getString(R.string.summary_expired, expired)

                expiringSoonCount.text = getString(R.string.summary_expiring_soon, expiringSoon)

                freshCount.text = getString(R.string.summary_fresh, fresh)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::database.isInitialized) {
            database.close()
        }
    }
}