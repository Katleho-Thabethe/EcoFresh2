package com.ecofresh.app

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ProductDetailsActivity : Activity() {

    private lateinit var database: EcoFreshDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        val barcodeText = findViewById<TextView>(R.id.barcodeText)
        val productName = findViewById<EditText>(R.id.productName)
        val category = findViewById<EditText>(R.id.category)
        val expiryDate = findViewById<EditText>(R.id.expiryDate)
        val saveProductButton = findViewById<Button>(R.id.saveProductButton)

        val barcode = intent.getStringExtra("BARCODE")

        barcodeText.text = barcode ?: "No barcode detected"

        database = Room.databaseBuilder(
            applicationContext,
            EcoFreshDatabase::class.java,
            "ecofresh_database",
        ).build()

        saveProductButton.setOnClickListener {

            val name = productName.text.toString().trim()
            val categoryText = category.text.toString().trim()
            val expiry = expiryDate.text.toString().trim()

            // Check that all fields are completed
            if (name.isEmpty() ||
                categoryText.isEmpty() ||
                expiry.isEmpty() ||
                barcode == null
            ) {

                Toast.makeText(
                    this,
                    "Please complete all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Validate DD/MM/YYYY
            val dateFormat = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

            dateFormat.isLenient = false

            try {

                dateFormat.parse(expiry)

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Please enter a valid date as DD/MM/YYYY",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            // Create product
            val product = Product(
                barcode = barcode,
                productName = name,
                category = categoryText,
                expiryDate = expiry
            )

            // Save product to Room database
            CoroutineScope(Dispatchers.IO).launch {

                database.productDao().insertProduct(product)

                runOnUiThread {

                    Toast.makeText(
                        this@ProductDetailsActivity,
                        "Product saved successfully",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                }
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