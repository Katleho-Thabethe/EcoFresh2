package com.ecofresh.app

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Product::class],
    version = 1,
    exportSchema = false,
)
abstract class EcoFreshDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
}