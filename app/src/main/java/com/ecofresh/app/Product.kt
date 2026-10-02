package com.ecofresh.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val barcode: String,
    val productName: String,
    val category: String,
    val expiryDate: String,
)