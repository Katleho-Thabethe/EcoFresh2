package com.ecofresh.app

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductsActivity : Activity() {

    private lateinit var database: EcoFreshDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        val productsContainer =
            findViewById<LinearLayout>(R.id.productsContainer)

        database = Room.databaseBuilder(
            applicationContext,
            EcoFreshDatabase::class.java,
            "ecofresh_database",
        ).build()

        CoroutineScope(Dispatchers.IO).launch {

            val products = database.productDao().getAllProducts()

            runOnUiThread {

                if (products.isEmpty()) {

                    val emptyMessage = TextView(this@ProductsActivity)

                    emptyMessage.text = getString(R.string.empty_products)
                    emptyMessage.textSize = 18f

                    productsContainer.addView(emptyMessage)

                } else {

                    for (product in products) {

                        val productText = TextView(this@ProductsActivity)

                        val status = ExpiryStatus.getStatus(product.expiryDate)

                        productText.text = getString(
                            R.string.product_summary,
                            product.productName,
                            product.category,
                            product.barcode,
                            product.expiryDate,
                            status
                        )

                        productText.textSize = 18f

                        productText.setPadding(
                            0,
                            16,
                            0,
                            24
                        )

                        productsContainer.addView(productText)
                    }
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