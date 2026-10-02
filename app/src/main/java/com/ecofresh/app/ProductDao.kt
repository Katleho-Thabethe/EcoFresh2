package com.ecofresh.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProductDao {

    @Insert
    suspend fun insertProduct(product: Product)

    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    suspend fun getAllProducts(): List<Product>
}