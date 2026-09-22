package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Business::class,
        Product::class,
        Customer::class,
        Sale::class,
        SaleItem::class,
        Expense::class,
        Supplier::class,
        Alert::class,
        AIChatMessage::class,
        Campaign::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun supplierDao(): SupplierDao
    abstract fun alertDao(): AlertDao
    abstract fun aiChatMessageDao(): AIChatMessageDao
    abstract fun campaignDao(): CampaignDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bizpilot_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
