package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class Business(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val ownerName: String = "",
    val category: String = "",
    val country: String = "",
    val currency: String = "INR",
    val phone: String = "",
    val address: String = "",
    val openingHours: String = "",
    val numEmployees: Int = 1,
    val salesRange: String = "",
    val aiInstructions: String = "",
    val isDemo: Boolean = false
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val category: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStock: Int = 0,
    val minimumStock: Int = 0,
    val supplier: String = "",
    val tax: Double = 0.0,
    val unit: String = "pcs",
    val description: String = "",
    val isDemo: Boolean = false
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val tags: String = "", // e.g. "VIP, Inactive"
    val isDemo: Boolean = false
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int? = null,
    val customerName: String = "Walk-in Customer",
    val date: Long = System.currentTimeMillis(),
    val paymentStatus: String = "Paid", // Paid, Pending, Partially Paid
    val paymentMethod: String = "Cash", // Cash, UPI, Card, Bank Transfer, Credit
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val profitEstimate: Double = 0.0,
    val isDemo: Boolean = false
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val saleId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val price: Double
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Rent, Electricity, Salary, Transport, Marketing, Inventory, Packaging, Other
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val receiptImageUri: String? = null,
    val isDemo: Boolean = false
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contactDetails: String = "",
    val outstandingPayments: Double = 0.0,
    val productsSupplied: String = "",
    val email: String = "",
    val isDemo: Boolean = false
)

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // Critical (Red), Warning (Orange), Info (Blue), Opportunity (Green)
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDemo: Boolean = false
)

@Entity(tableName = "ai_chat_messages")
data class AIChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // user, model, system
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null, // e.g. "CREATE_PRODUCT", "RECORD_SALE", "REORDER_STOCK", "FOLLOW_UP", "CREATE_CAMPAIGN"
    val actionData: String? = null,  // JSON representation of action arguments
    val actionExecuted: Boolean = false
)

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val platform: String, // WhatsApp, Instagram, Facebook, SMS, Email
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)
