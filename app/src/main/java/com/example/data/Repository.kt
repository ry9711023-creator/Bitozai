package com.example.data

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BizPilotRepository(private val db: AppDatabase) {

    // Exposure of Flows
    val businessFlow: Flow<Business?> = db.businessDao().getBusinessFlow()
    val productsFlow: Flow<List<Product>> = db.productDao().getAllProductsFlow()
    val customersFlow: Flow<List<Customer>> = db.customerDao().getAllCustomersFlow()
    val salesFlow: Flow<List<Sale>> = db.saleDao().getAllSalesFlow()
    val expensesFlow: Flow<List<Expense>> = db.expenseDao().getAllExpensesFlow()
    val suppliersFlow: Flow<List<Supplier>> = db.supplierDao().getAllSuppliersFlow()
    val alertsFlow: Flow<List<Alert>> = db.alertDao().getAllAlertsFlow()
    val unreadAlertsFlow: Flow<List<Alert>> = db.alertDao().getUnreadAlertsFlow()
    val chatMessagesFlow: Flow<List<AIChatMessage>> = db.aiChatMessageDao().getAllMessagesFlow()
    val campaignsFlow: Flow<List<Campaign>> = db.campaignDao().getAllCampaignsFlow()

    // CRUD operations
    suspend fun getBusiness(): Business? = db.businessDao().getBusiness()
    suspend fun saveBusiness(business: Business) = db.businessDao().insertBusiness(business)
    suspend fun updateBusiness(business: Business) = db.businessDao().updateBusiness(business)
    
    suspend fun addProduct(product: Product) = db.productDao().insertProduct(product)
    suspend fun updateProduct(product: Product) = db.productDao().updateProduct(product)
    suspend fun deleteProduct(product: Product) = db.productDao().deleteProduct(product)

    suspend fun addCustomer(customer: Customer) = db.customerDao().insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = db.customerDao().updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = db.customerDao().deleteCustomerEntity(customer)

    suspend fun addExpense(expense: Expense) = db.expenseDao().insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = db.expenseDao().deleteExpense(expense)

    suspend fun addSupplier(supplier: Supplier) = db.supplierDao().insertSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = db.supplierDao().updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = db.supplierDao().deleteSupplier(supplier)

    suspend fun addCampaign(campaign: Campaign) = db.campaignDao().insertCampaign(campaign)

    suspend fun addAlert(alert: Alert) = db.alertDao().insertAlert(alert)
    suspend fun markAlertAsRead(id: Int) = db.alertDao().markAsRead(id)
    suspend fun markAllAlertsAsRead() = db.alertDao().markAllAsRead()
    suspend fun deleteAlert(id: Int) = db.alertDao().deleteAlert(id)

    suspend fun recordSale(sale: Sale, items: List<SaleItem>) = withContext(Dispatchers.IO) {
        val saleId = db.saleDao().insertSale(sale).toInt()
        for (item in items) {
            val saleItem = item.copy(saleId = saleId)
            db.saleItemDao().insertSaleItem(saleItem)
            
            // Update inventory stock
            val product = db.productDao().getProductById(item.productId)
            if (product != null) {
                val updatedStock = (product.currentStock - item.quantity).coerceAtLeast(0)
                db.productDao().updateProduct(product.copy(currentStock = updatedStock))
                
                // Trigger low stock alerts if threshold breached
                if (updatedStock <= product.minimumStock) {
                    db.alertDao().insertAlert(
                        Alert(
                            title = "Low Stock Alert: ${product.name}",
                            type = "Warning",
                            description = "Stock of ${product.name} is down to $updatedStock ${product.unit}. Min stock is ${product.minimumStock}.",
                            isDemo = sale.isDemo
                        )
                    )
                }
            }
        }
        
        // Trigger high value sale alert if total > 5000
        if (sale.total > 5000) {
            db.alertDao().insertAlert(
                Alert(
                    title = "High Value Sale recorded!",
                    type = "Opportunity",
                    description = "A sale of ${sale.total} was recorded for ${sale.customerName}.",
                    isDemo = sale.isDemo
                )
            )
        }
    }

    suspend fun getSaleItems(saleId: Int): List<SaleItem> = db.saleItemDao().getItemsForSale(saleId)

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        db.businessDao().clearAll()
        db.productDao().clearAll()
        db.customerDao().clearAll()
        db.saleDao().clearAll()
        db.saleItemDao().clearAll()
        db.expenseDao().clearAll()
        db.supplierDao().clearAll()
        db.alertDao().clearAll()
        db.aiChatMessageDao().clearAll()
        db.campaignDao().clearAll()
    }

    suspend fun clearDemoDataOnly() = withContext(Dispatchers.IO) {
        db.productDao().clearDemoProducts()
        db.customerDao().clearDemoCustomers()
        db.saleDao().clearDemoSales()
        db.expenseDao().clearDemoExpenses()
        db.supplierDao().clearDemoSuppliers()
        db.alertDao().clearAll() // Alerts generated by demo can clear
    }

    suspend fun addMessage(message: AIChatMessage) = db.aiChatMessageDao().insertMessage(message)
    suspend fun markActionExecuted(id: Int) = db.aiChatMessageDao().markActionExecuted(id)

    // Demo Initializer (20 products, 10 customers, 5 suppliers, sales, etc.)
    suspend fun loadDemoDataset() = withContext(Dispatchers.IO) {
        clearAllData()

        val demoBusiness = Business(
            name = "Chai Tapri Café",
            ownerName = "Rajesh Kumar",
            category = "Cafe",
            country = "India",
            currency = "INR",
            phone = "+91 98765 43210",
            address = "G-14, Sector 62, Noida, UP, India",
            openingHours = "8:00 AM - 10:00 PM",
            numEmployees = 3,
            salesRange = "₹50,000 - ₹1,00,000",
            aiInstructions = "Always recommend reordering items when stock falls below min. Use cheerful Hinglish for user answers.",
            isDemo = true
        )
        db.businessDao().insertBusiness(demoBusiness)

        val suppliers = listOf(
            Supplier(name = "Amul Milk Distributor", contactDetails = "+91 99112 23344", outstandingPayments = 1500.0, productsSupplied = "Milk, Cream, Paneer, Butter", isDemo = true),
            Supplier(name = "Assam Tea Estates", contactDetails = "+91 88776 65544", outstandingPayments = 0.0, productsSupplied = "Masala Tea powder, Green Tea", isDemo = true),
            Supplier(name = "Local Bakers Union", contactDetails = "+91 77665 54433", outstandingPayments = 500.0, productsSupplied = "Croissants, Muffin, Toast", isDemo = true)
        )
        for (sup in suppliers) {
            db.supplierDao().insertSupplier(sup)
        }

        val products = listOf(
            Product(name = "Special Masala Chai", sku = "CH-001", category = "Beverages", purchasePrice = 8.0, sellingPrice = 20.0, currentStock = 120, minimumStock = 30, supplier = "Assam Tea Estates", unit = "cups", isDemo = true),
            Product(name = "Elachi Chai", sku = "CH-002", category = "Beverages", purchasePrice = 9.0, sellingPrice = 25.0, currentStock = 90, minimumStock = 20, supplier = "Assam Tea Estates", unit = "cups", isDemo = true),
            Product(name = "Butter Croissant", sku = "BA-001", category = "Bakery", purchasePrice = 30.0, sellingPrice = 70.0, currentStock = 5, minimumStock = 10, supplier = "Local Bakers Union", unit = "pcs", isDemo = true), // Low Stock
            Product(name = "Chocolate Muffin", sku = "BA-002", category = "Bakery", purchasePrice = 25.0, sellingPrice = 60.0, currentStock = 15, minimumStock = 8, supplier = "Local Bakers Union", unit = "pcs", isDemo = true),
            Product(name = "Cold Coffee", sku = "CB-001", category = "Beverages", purchasePrice = 18.0, sellingPrice = 50.0, currentStock = 45, minimumStock = 15, supplier = "Amul Milk Distributor", unit = "bottles", isDemo = true),
            Product(name = "Club Sandwich", sku = "SN-001", category = "Snacks", purchasePrice = 35.0, sellingPrice = 90.0, currentStock = 22, minimumStock = 10, supplier = "Local Bakers Union", unit = "pcs", isDemo = true),
            Product(name = "Bun Maska", sku = "SN-002", category = "Snacks", purchasePrice = 12.0, sellingPrice = 30.0, currentStock = 50, minimumStock = 15, supplier = "Local Bakers Union", unit = "pcs", isDemo = true),
            Product(name = "Hot Ginger Tea", sku = "CH-003", category = "Beverages", purchasePrice = 8.0, sellingPrice = 22.0, currentStock = 80, minimumStock = 25, supplier = "Assam Tea Estates", unit = "cups", isDemo = true),
            Product(name = "Paneer Tikka Roll", sku = "SN-003", category = "Snacks", purchasePrice = 45.0, sellingPrice = 110.0, currentStock = 2, minimumStock = 8, supplier = "Amul Milk Distributor", unit = "pcs", isDemo = true), // Low stock
            Product(name = "Samosa (Plate of 2)", sku = "SN-004", category = "Snacks", purchasePrice = 10.0, sellingPrice = 25.0, currentStock = 35, minimumStock = 12, supplier = "Local Bakers Union", unit = "plates", isDemo = true),
            Product(name = "Filter Coffee", sku = "CB-002", category = "Beverages", purchasePrice = 12.0, sellingPrice = 35.0, currentStock = 60, minimumStock = 15, supplier = "Karnataka Coffee Roasters", unit = "cups", isDemo = true),
            Product(name = "Brownie with Ice Cream", sku = "BA-003", category = "Bakery", purchasePrice = 40.0, sellingPrice = 95.0, currentStock = 12, minimumStock = 5, supplier = "Local Bakers Union", unit = "plates", isDemo = true),
            Product(name = "Iced Lemon Tea", sku = "CB-003", category = "Beverages", purchasePrice = 14.0, sellingPrice = 45.0, currentStock = 30, minimumStock = 10, supplier = "Assam Tea Estates", unit = "glasses", isDemo = true),
            Product(name = "Green Tea", sku = "CB-004", category = "Beverages", purchasePrice = 6.0, sellingPrice = 25.0, currentStock = 110, minimumStock = 15, supplier = "Assam Tea Estates", unit = "cups", isDemo = true),
            Product(name = "Assam Tea Bag Box", sku = "PK-001", category = "Packaged", purchasePrice = 120.0, sellingPrice = 180.0, currentStock = 25, minimumStock = 6, supplier = "Assam Tea Estates", unit = "boxes", isDemo = true),
            Product(name = "Aloo Tikki Burger", sku = "SN-005", category = "Snacks", purchasePrice = 22.0, sellingPrice = 55.0, currentStock = 18, minimumStock = 8, supplier = "Local Bakers Union", unit = "pcs", isDemo = true),
            Product(name = "Aero Cookies Pack", sku = "PK-002", category = "Packaged", purchasePrice = 15.0, sellingPrice = 35.0, currentStock = 40, minimumStock = 10, supplier = "Local Bakers Union", unit = "packs", isDemo = true),
            Product(name = "Garlic Bread", sku = "SN-006", category = "Snacks", purchasePrice = 28.0, sellingPrice = 75.0, currentStock = 1, minimumStock = 5, supplier = "Local Bakers Union", unit = "pcs", isDemo = true), // Low Stock
            Product(name = "Sugar Packets Box", sku = "PK-003", category = "Packaged", purchasePrice = 50.0, sellingPrice = 75.0, currentStock = 8, minimumStock = 2, supplier = "Assam Tea Estates", unit = "boxes", isDemo = true),
            Product(name = "Amul Fresh Cream", sku = "PK-004", category = "Packaged", purchasePrice = 40.0, sellingPrice = 55.0, currentStock = 14, minimumStock = 4, supplier = "Amul Milk Distributor", unit = "packs", isDemo = true)
        )
        for (prod in products) {
            db.productDao().insertProduct(prod)
        }

        val customers = listOf(
            Customer(name = "Rahul Sharma", phone = "+91 91234 56789", email = "rahul@gmail.com", address = "Sector 15, Noida", notes = "Regular customer, loves Masala Chai.", tags = "VIP, Returning", isDemo = true),
            Customer(name = "Priya Patel", phone = "+91 92345 67890", email = "priya@yahoo.com", address = "Sector 50, Noida", notes = "Prefers non-sugar options.", tags = "Returning", isDemo = true),
            Customer(name = "Amit Verma", phone = "+91 93456 78901", email = "amit@outlook.com", address = "Indirapuram, Ghaziabad", notes = "Unpaid dues of ₹450.", tags = "Payment Pending", isDemo = true),
            Customer(name = "Pooja Gupta", phone = "+91 94567 89012", email = "pooja@gmail.com", address = "Sector 62, Noida", notes = "Visits during weekends with friends.", tags = "Returning", isDemo = true),
            Customer(name = "Sanjay Mehra", phone = "+91 95678 90123", email = "sanjay@mehra.com", address = "Vasundhara, Ghaziabad", notes = "Has not visited in 45 days.", tags = "Inactive", isDemo = true),
            Customer(name = "Karan Johar", phone = "+91 96789 01234", email = "karan@dharmaprod.com", address = "Film City, Noida", notes = "Always orders Filter Coffee.", tags = "New Customer", isDemo = true),
            Customer(name = "Sneha Rao", phone = "+91 97890 12345", email = "sneha@rediff.com", address = "Sector 22, Noida", notes = "Loves Bun Maska and Ginger tea.", tags = "Returning", isDemo = true),
            Customer(name = "Anil Kapoor", phone = "+91 98901 23456", email = "anil@gmail.com", address = "Sector 44, Noida", notes = "Visits once a month.", tags = "Returning", isDemo = true),
            Customer(name = "Meera Nair", phone = "+91 99012 34567", email = "meera@gmail.com", address = "Sector 12, Noida", notes = "Prefers Green Tea.", tags = "Returning", isDemo = true),
            Customer(name = "Vijay Mallya", phone = "+91 90123 45678", email = "vijay@forceindia.com", address = "London (Formerly Delhi)", notes = "Large party order overdue of ₹12,000.", tags = "Payment Pending, Inactive", isDemo = true)
        )
        for (cust in customers) {
            db.customerDao().insertCustomer(cust)
        }

        // Add historical Sales
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        
        val sales = listOf(
            Sale(customerId = 1, customerName = "Rahul Sharma", date = now - 2 * oneDayMs, paymentStatus = "Paid", paymentMethod = "UPI", discount = 0.0, tax = 5.0, total = 175.0, profitEstimate = 95.0, isDemo = true),
            Sale(customerId = 2, customerName = "Priya Patel", date = now - oneDayMs, paymentStatus = "Paid", paymentMethod = "Card", discount = 10.0, tax = 8.0, total = 240.0, profitEstimate = 130.0, isDemo = true),
            Sale(customerId = 3, customerName = "Amit Verma", date = now - 5 * oneDayMs, paymentStatus = "Pending", paymentMethod = "Credit", discount = 0.0, tax = 12.0, total = 450.0, profitEstimate = 210.0, isDemo = true),
            Sale(customerId = 4, customerName = "Pooja Gupta", date = now - oneDayMs, paymentStatus = "Paid", paymentMethod = "Cash", discount = 0.0, tax = 4.0, total = 145.0, profitEstimate = 80.0, isDemo = true),
            Sale(customerId = 7, customerName = "Sneha Rao", date = now, paymentStatus = "Paid", paymentMethod = "UPI", discount = 0.0, tax = 10.0, total = 320.0, profitEstimate = 175.0, isDemo = true),
            Sale(customerId = null, customerName = "Walk-in Customer", date = now, paymentStatus = "Paid", paymentMethod = "Cash", discount = 0.0, tax = 2.0, total = 65.0, profitEstimate = 35.0, isDemo = true)
        )
        for (s in sales) {
            val sId = db.saleDao().insertSale(s).toInt()
            // insert a couple of items for each sale
            if (s.customerName == "Rahul Sharma") {
                db.saleItemDao().insertSaleItem(SaleItem(saleId = sId, productId = 1, productName = "Special Masala Chai", quantity = 5, price = 20.0))
                db.saleItemDao().insertSaleItem(SaleItem(saleId = sId, productId = 6, productName = "Club Sandwich", quantity = 1, price = 75.0))
            } else if (s.customerName == "Priya Patel") {
                db.saleItemDao().insertSaleItem(SaleItem(saleId = sId, productId = 5, productName = "Cold Coffee", quantity = 3, price = 50.0))
                db.saleItemDao().insertSaleItem(SaleItem(saleId = sId, productId = 7, productName = "Bun Maska", quantity = 3, price = 30.0))
            } else {
                db.saleItemDao().insertSaleItem(SaleItem(saleId = sId, productId = 1, productName = "Special Masala Chai", quantity = 2, price = 20.0))
            }
        }

        // Add Expenses
        val expenses = listOf(
            Expense(name = "September Rent", category = "Rent", amount = 12000.0, date = now - 10 * oneDayMs, paymentMethod = "Bank Transfer", notes = "Paid to Landlord.", isDemo = true),
            Expense(name = "August Electricity Bill", category = "Electricity", amount = 3500.0, date = now - 12 * oneDayMs, paymentMethod = "UPI", notes = "Noida Power Company.", isDemo = true),
            Expense(name = "Milk Supply Bill", category = "Inventory", amount = 4200.0, date = now - 2 * oneDayMs, paymentMethod = "Cash", notes = "Amul distributor supply payment.", isDemo = true),
            Expense(name = "Chai Tapri Pamflets", category = "Marketing", amount = 1500.0, date = now - 15 * oneDayMs, paymentMethod = "UPI", notes = "Pamphlets printed for distribution.", isDemo = true)
        )
        for (exp in expenses) {
            db.expenseDao().insertExpense(exp)
        }

        // Initial Alerts
        val alerts = listOf(
            Alert(title = "Stock Alert: Butter Croissant", type = "Warning", description = "Butter Croissant is low on stock! Only 5 pcs left. Minimum required stock is 10.", isDemo = true),
            Alert(title = "Overdue Payment: Amit Verma", type = "Critical", description = "Amit Verma has outstanding credit of ₹450 pending for over 5 days.", isDemo = true),
            Alert(title = "Sales Milestone Reached! 🚀", type = "Opportunity", description = "Your weekly sales are up 18% compared to last week! Cafe is trending well.", isDemo = true)
        )
        for (al in alerts) {
            db.alertDao().insertAlert(al)
        }

        // Sample Welcome chat messages
        val welcomeMsg = AIChatMessage(
            role = "model",
            content = "Hello! I am your **BizPilot AI** Business Assistant. I have loaded the **Chai Tapri Café Demo** dataset. Try asking me:\n\n" +
                      "1. *आज कितनी sale हुई?*\n" +
                      "2. *Which products need restocking?*\n" +
                      "3. *Create customer follow-up list.*",
            timestamp = System.currentTimeMillis()
        )
        db.aiChatMessageDao().insertMessage(welcomeMsg)
    }

    // AI Reasoning & Chat Flow (with direct Retrofit API call and rule-based fallback!)
    suspend fun processAskBizPilot(userQuery: String): AIChatMessage = withContext(Dispatchers.IO) {
        // First retrieve current context
        val business = db.businessDao().getBusiness()
        val products = db.productDao().getAllProductsFlow().firstOrNull() ?: emptyList()
        val customers = db.customerDao().getAllCustomersFlow().firstOrNull() ?: emptyList()
        val sales = db.saleDao().getAllSalesFlow().firstOrNull() ?: emptyList()
        val expenses = db.expenseDao().getAllExpensesFlow().firstOrNull() ?: emptyList()
        val suppliers = db.supplierDao().getAllSuppliersFlow().firstOrNull() ?: emptyList()
        val alerts = db.alertDao().getAllAlertsFlow().firstOrNull() ?: emptyList()

        val currencySymbol = if (business?.currency == "INR") "₹" else business?.currency ?: "$"

        // Context serialization
        val contextStr = buildString {
            append("Business Name: ${business?.name ?: "BizPilot Business"}\n")
            append("Category: ${business?.category ?: "Retail"}\n")
            append("Currency: $currencySymbol\n")
            append("Products/Services Stock:\n")
            products.forEach { p ->
                append("- ${p.name} (ID: ${p.id}, Stock: ${p.currentStock} ${p.unit}, Min: ${p.minimumStock}, Selling Price: $currencySymbol${p.sellingPrice}, Cost: $currencySymbol${p.purchasePrice}, SKU: ${p.sku})\n")
            }
            append("Customers:\n")
            customers.forEach { c ->
                append("- ${c.name} (ID: ${c.id}, Tags: ${c.tags}, Notes: ${c.notes})\n")
            }
            append("Sales Summary:\n")
            sales.take(10).forEach { s ->
                append("- Sale ID: ${s.id}, Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(s.date))}, Customer: ${s.customerName}, Total: $currencySymbol${s.total}, Status: ${s.paymentStatus}, Method: ${s.paymentMethod}\n")
            }
            append("Expenses Summary:\n")
            expenses.take(5).forEach { e ->
                append("- ${e.name} (Amount: $currencySymbol${e.amount}, Category: ${e.category})\n")
            }
            append("Suppliers:\n")
            suppliers.forEach { s ->
                append("- ${s.name} (Products: ${s.productsSupplied})\n")
            }
            append("Alerts:\n")
            alerts.filter { !it.isRead }.forEach { a ->
                append("- ${a.title} (${a.type}): ${a.description}\n")
            }
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                // Call actual Retrofit Gemini Service!
                val systemPrompt = "You are BizPilot AI, an expert AI Business Operator and Advisor for small businesses.\n" +
                        "The user's current business profile and state are as follows:\n\n$contextStr\n\n" +
                        "Provide extremely actionable insights in Hinglish or English as appropriate.\n" +
                        "If the user asks a question, answer strictly using the provided data. NEVER invent business data. If data is missing, say: 'I don't have enough data to calculate this yet.'\n" +
                        "You can also propose actions to the user! If you propose an action, you MUST end your message with a structured block:\n" +
                        "```ACTION:[ACTION_TYPE]:[JSON_ARGUMENTS]```\n" +
                        "Where JSON_ARGUMENTS is a valid JSON string (double-quoted keys/values).\n" +
                        "Supported action types:\n" +
                        "- `CREATE_PRODUCT`: `{\"name\":\"Product Name\",\"category\":\"Category\",\"purchasePrice\":100,\"sellingPrice\":150,\"currentStock\":20,\"minimumStock\":5,\"unit\":\"pcs\"}`\n" +
                        "- `RECORD_EXPENSE`: `{\"name\":\"Expense Name\",\"category\":\"Category\",\"amount\":500}`\n" +
                        "- `REORDER_STOCK`: `{\"productId\":1,\"quantity\":50,\"supplier\":\"Supplier Name\"}`\n" +
                        "- `FOLLOW_UP`: `{\"customerId\":2,\"message\":\"Message text\"}`\n" +
                        "- `CREATE_CAMPAIGN`: `{\"title\":\"Campaign Title\",\"platform\":\"WhatsApp\",\"content\":\"Campaign text\"}`\n\n" +
                        "Let your response be clean, with bullet points and tables where appropriate."

                val geminiRequest = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = userQuery)))),
                    generationConfig = GeminiGenerationConfig(temperature = 0.2f),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                )

                val response = RetrofitClient.service.generateContent(apiKey, geminiRequest)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!responseText.isNullOrEmpty()) {
                    // Parse action from response if any
                    val actionTuple = parseActionBlock(responseText)
                    return@withContext AIChatMessage(
                        role = "model",
                        content = cleanActionFromText(responseText),
                        timestamp = System.currentTimeMillis(),
                        actionType = actionTuple?.first,
                        actionData = actionTuple?.second
                    )
                }
            } catch (e: Exception) {
                // Fail gracefully to local fallback
            }
        }

        // Intelligent Rule-Based Local Fallback Engine if API key is missing or fails
        val query = userQuery.lowercase(Locale.getDefault())
        var aiContent = ""
        var actionType: String? = null
        var actionData: String? = null

        if (query.contains("sale") || query.contains("sales") || query.contains("आज कितनी")) {
            val todayStart = getStartOfDay()
            val todaySales = sales.filter { it.date >= todayStart }
            val totalToday = todaySales.sumOf { it.total }
            val profitToday = todaySales.sumOf { it.profitEstimate }
            val yesterdaySales = sales.filter { it.date >= todayStart - 24*60*60*1000L && it.date < todayStart }
            val totalYesterday = yesterdaySales.sumOf { it.total }
            
            aiContent = if (todaySales.isNotEmpty()) {
                "आज ${currencySymbol}${totalToday} की sales हुई है! इसमें से Gross Profit ${currencySymbol}${profitToday} है.\n\n" +
                "**Sales breakdown:**\n" +
                todaySales.joinToString("\n") { "- ${it.customerName}: ${currencySymbol}${it.total} (${it.paymentMethod})" } +
                (if (totalYesterday > 0) {
                    val change = ((totalToday - totalYesterday) / totalYesterday * 100).toInt()
                    "\n\nकल की तुलना में sales **${if (change >= 0) "+" else ""}$change%** है."
                } else "")
            } else {
                "आज अभी तक कोई sales record नहीं की गई है. आप [Add Sale] button से आज की पहली sale record कर सकते हैं! " +
                (if (totalYesterday > 0) "कल total sales ${currencySymbol}${totalYesterday} हुई थी." else "")
            }
        } 
        else if (query.contains("reorder") || query.contains("stock") || query.contains("product") || query.contains("खत्म होने")) {
            val lowStockProds = products.filter { it.currentStock <= it.minimumStock }
            if (lowStockProds.isNotEmpty()) {
                val firstLow = lowStockProds.first()
                aiContent = "यहाँ low-stock products की list है जो खत्म होने वाले हैं:\n\n" +
                        "| Product | Stock | Min Stock | Suggestion |\n" +
                        "| --- | --- | --- | --- |\n" +
                        lowStockProds.joinToString("\n") { "| ${it.name} | **${it.currentStock}** ${it.unit} | ${it.minimumStock} | Order ${it.minimumStock * 3} ${it.unit} |" } +
                        "\n\nक्या मैं **${firstLow.name}** के लिए **${firstLow.minimumStock * 3} units** का purchase order / stock request create करूँ?"
                
                actionType = "REORDER_STOCK"
                actionData = "{\"productId\":${firstLow.id},\"quantity\":${firstLow.minimumStock * 3},\"supplier\":\"${firstLow.supplier.ifEmpty { "General Supplier" }}\"}"
            } else {
                aiContent = "Great! आपके सारे products का stock level safe है. कोई भी product reorder threshold से नीचे नहीं है."
            }
        }
        else if (query.contains("follow-up") || query.contains("customer") || query.contains("inactive")) {
            val inactiveCustomers = customers.filter { it.tags.contains("Inactive", ignoreCase = true) || it.tags.contains("Pending", ignoreCase = true) }
            if (inactiveCustomers.isNotEmpty()) {
                val firstCust = inactiveCustomers.first()
                val messageText = "Hi ${firstCust.name}, we miss you at ${business?.name ?: "our store"}! Order your favorites today & get 10% off using coupon SAVE10."
                aiContent = "यहाँ inactive / pending payment customers की list है जिन्हें follow-up की आवश्यकता है:\n\n" +
                        "| Customer | Phone | Segment | Note |\n" +
                        "| --- | --- | --- | --- |\n" +
                        inactiveCustomers.joinToString("\n") { "| ${it.name} | ${it.phone} | ${it.tags} | ${it.notes} |" } +
                        "\n\nक्या मैं **${firstCust.name}** के लिए WhatsApp offer / follow-up message create करूँ?"
                
                actionType = "FOLLOW_UP"
                actionData = "{\"customerId\":${firstCust.id},\"message\":\"$messageText\"}"
            } else {
                aiContent = "सभी customers active हैं और किसी भी customer का payment overdue नहीं है!"
            }
        }
        else if (query.contains("whatsapp") || query.contains("offer") || query.contains("marketing") || query.contains("campaign")) {
            val targetProduct = products.firstOrNull { it.currentStock > 10 } ?: products.firstOrNull()
            val prodName = targetProduct?.name ?: "our Special Items"
            val offerText = "🎉 Special Monsoon Deal at ${business?.name ?: "BizPilot Store"}! 🎉\nGet 15% OFF on our best-selling $prodName today! Show this message at checkout to redeem."
            
            aiContent = "मैंने **$prodName** के लिए एक professional WhatsApp marketing campaign draft किया है:\n\n" +
                    "\"$offerText\"\n\n" +
                    "क्या आप इसे **Campaign List** में save करना चाहते हैं?"
            
            actionType = "CREATE_CAMPAIGN"
            actionData = "{\"title\":\"Special Deal: $prodName\",\"platform\":\"WhatsApp\",\"content\":\"$offerText\"}"
        }
        else if (query.contains("profit") || query.contains("कम क्यों")) {
            val totalS = sales.sumOf { it.total }
            val totalC = sales.sumOf { it.profitEstimate }
            val totalE = expenses.sumOf { it.amount }
            val netProfit = totalC - totalE

            aiContent = "इस महीने का financial analysis:\n" +
                    "- Total Revenue (Sales): ${currencySymbol}${totalS}\n" +
                    "- Estimated Cost of Goods: ${currencySymbol}${totalS - totalC}\n" +
                    "- Gross Profit: ${currencySymbol}${totalC}\n" +
                    "- Total Expenses: ${currencySymbol}${totalE}\n" +
                    "- Net Profit: **${currencySymbol}${netProfit}**\n\n" +
                    "**Analysis:** " +
                    (if (totalE > totalC * 0.4) {
                        "आपके expenses, gross profit के 40% से अधिक हैं, जिससे net profit margin कम हुआ है. Rent और electricity bills major chunks हैं. Expenses control करने की सलाह दी जाती है."
                    } else {
                        "Profit margin स्वस्थ है. और sales बढ़ाने के लिए WhatsApp campaigns का उपयोग करें!"
                    })
        }
        else {
            aiContent = "मैं आपकी मदद करने के लिए तैयार हूँ! आप मुझसे sales analysis, low stock items, customer follow-up, या marketing campaign के बारे में पूछ सकते हैं. " +
                    "जैसे: *'Which products need restocking?'* या *'Create customer follow-up list.'*"
        }

        AIChatMessage(
            role = "model",
            content = aiContent,
            timestamp = System.currentTimeMillis(),
            actionType = actionType,
            actionData = actionData
        )
    }

    private fun parseActionBlock(text: String): Pair<String, String>? {
        try {
            val startTag = "```ACTION:"
            if (text.contains(startTag)) {
                val index = text.indexOf(startTag) + startTag.length
                val endBlockIndex = text.indexOf("```", index)
                if (endBlockIndex != -1) {
                    val block = text.substring(index, endBlockIndex).trim()
                    // Block format: ACTION_TYPE:JSON_ARGUMENTS
                    val colonIndex = block.indexOf(":")
                    if (colonIndex != -1) {
                        val actionType = block.substring(0, colonIndex).trim()
                        val jsonArgs = block.substring(colonIndex + 1).trim()
                        return Pair(actionType, jsonArgs)
                    }
                }
            }
        } catch (e: Exception) {
            // parsing error
        }
        return null
    }

    private fun cleanActionFromText(text: String): String {
        val startTag = "```ACTION:"
        if (text.contains(startTag)) {
            val index = text.indexOf(startTag)
            return text.substring(0, index).trim()
        }
        return text
    }

    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
