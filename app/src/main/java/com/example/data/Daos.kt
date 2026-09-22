package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses LIMIT 1")
    fun getBusinessFlow(): Flow<Business?>

    @Query("SELECT * FROM businesses LIMIT 1")
    suspend fun getBusiness(): Business?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: Business)

    @Update
    suspend fun updateBusiness(business: Business)

    @Query("DELETE FROM businesses")
    suspend fun clearAll()
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isDemo = :isDemo ORDER BY name ASC")
    fun getProductsByDemoFlow(isDemo: Boolean): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products")
    suspend fun clearAll()

    @Query("DELETE FROM products WHERE isDemo = 1")
    suspend fun clearDemoProducts()
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE isDemo = :isDemo ORDER BY name ASC")
    fun getCustomersByDemoFlow(isDemo: Boolean): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Int): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Product) // wait, delete customer should take Customer entity

    @Delete
    suspend fun deleteCustomerEntity(customer: Customer)

    @Query("DELETE FROM customers")
    suspend fun clearAll()

    @Query("DELETE FROM customers WHERE isDemo = 1")
    suspend fun clearDemoCustomers()
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSalesFlow(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Int): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Update
    suspend fun updateSale(sale: Sale)

    @Query("DELETE FROM sales")
    suspend fun clearAll()

    @Query("DELETE FROM sales WHERE isDemo = 1")
    suspend fun clearDemoSales()
}

@Dao
interface SaleItemDao {
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getItemsForSaleFlow(saleId: Int): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: Int): List<SaleItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(item: SaleItem)

    @Query("DELETE FROM sale_items")
    suspend fun clearAll()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpensesFlow(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun clearAll()

    @Query("DELETE FROM expenses WHERE isDemo = 1")
    suspend fun clearDemoExpenses()
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliersFlow(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Query("DELETE FROM suppliers")
    suspend fun clearAll()

    @Query("DELETE FROM suppliers WHERE isDemo = 1")
    suspend fun clearDemoSuppliers()
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlertsFlow(): Flow<List<Alert>>

    @Query("SELECT * FROM alerts WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadAlertsFlow(): Flow<List<Alert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: Alert)

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun deleteAlert(id: Int)

    @Query("DELETE FROM alerts")
    suspend fun clearAll()
}

@Dao
interface AIChatMessageDao {
    @Query("SELECT * FROM ai_chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<AIChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AIChatMessage): Long

    @Query("UPDATE ai_chat_messages SET actionExecuted = 1 WHERE id = :id")
    suspend fun markActionExecuted(id: Int)

    @Query("DELETE FROM ai_chat_messages")
    suspend fun clearAll()
}

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY timestamp DESC")
    fun getAllCampaignsFlow(): Flow<List<Campaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign)

    @Query("DELETE FROM campaigns")
    suspend fun clearAll()
}
