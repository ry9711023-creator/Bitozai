package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Calendar

enum class AppScreen {
    LANDING,
    ONBOARDING,
    DASHBOARD,
    INVENTORY,
    SALES,
    CUSTOMERS,
    ASK_PILOT,
    MORE
}

class BizPilotViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = BizPilotRepository(db)

    // Screen navigation
    private val _currentScreen = MutableStateFlow(AppScreen.LANDING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Onboarding step (1 to 4)
    private val _onboardingStep = MutableStateFlow(1)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    // Loading & Feedback
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // OCR simulation state
    private val _ocrReviewData = MutableStateFlow<OcrResult?>(null)
    val ocrReviewData: StateFlow<OcrResult?> = _ocrReviewData.asStateFlow()

    // Expose Flows from Repository
    val business: StateFlow<Business?> = repository.businessFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val products: StateFlow<List<Product>> = repository.productsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.customersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.salesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.expensesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<Supplier>> = repository.suppliersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<Alert>> = repository.alertsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<AIChatMessage>> = repository.chatMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val campaigns: StateFlow<List<Campaign>> = repository.campaignsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Check if business profile exists to auto-navigate to Dashboard
        viewModelScope.launch {
            val b = repository.getBusiness()
            if (b != null) {
                _currentScreen.value = AppScreen.DASHBOARD
            }
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Auth & Onboarding Flow
    fun startFree() {
        _currentScreen.value = AppScreen.ONBOARDING
        _onboardingStep.value = 1
    }

    fun setOnboardingStep(step: Int) {
        _onboardingStep.value = step
    }

    fun completeOnboarding(
        name: String,
        owner: String,
        category: String,
        country: String,
        currency: String,
        phone: String,
        address: String,
        hours: String,
        employees: Int,
        salesRange: String,
        aiInstructions: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newBiz = Business(
                    name = name,
                    ownerName = owner,
                    category = category,
                    country = country,
                    currency = currency,
                    phone = phone,
                    address = address,
                    openingHours = hours,
                    numEmployees = employees,
                    salesRange = salesRange,
                    aiInstructions = aiInstructions,
                    isDemo = false
                )
                repository.saveBusiness(newBiz)
                
                // Add first welcome AI message
                val welcomeMsg = AIChatMessage(
                    role = "model",
                    content = "Welcome to **BizPilot AI**, $owner! 🚀 I am your dedicated AI Employee. " +
                              "I have configured your profile for **$name ($category)** in $country.\n\n" +
                              "To get started, try adding your first product, customer, or ask me directly to do something!",
                    timestamp = System.currentTimeMillis()
                )
                repository.addMessage(welcomeMsg)

                _currentScreen.value = AppScreen.DASHBOARD
                showToast("Business setup completed successfully!")
            } catch (e: Exception) {
                showToast("Failed to complete onboarding: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Try Demo Mode
    fun tryDemo() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.loadDemoDataset()
                _currentScreen.value = AppScreen.DASHBOARD
                showToast("Chai Tapri Café Demo loaded! 🍵 Enjoy.")
            } catch (e: Exception) {
                showToast("Error loading demo dataset: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearAllData()
            _currentScreen.value = AppScreen.LANDING
            showToast("Logged out successfully.")
        }
    }

    // Product CRM Actions
    fun addProduct(
        name: String,
        sku: String,
        category: String,
        purchasePrice: Double,
        sellingPrice: Double,
        currentStock: Int,
        minimumStock: Int,
        supplier: String,
        tax: Double,
        unit: String,
        description: String
    ) {
        viewModelScope.launch {
            val isDemoMode = business.value?.isDemo ?: false
            val prod = Product(
                name = name, sku = sku, category = category,
                purchasePrice = purchasePrice, sellingPrice = sellingPrice,
                currentStock = currentStock, minimumStock = minimumStock,
                supplier = supplier, tax = tax, unit = unit,
                description = description, isDemo = isDemoMode
            )
            repository.addProduct(prod)
            showToast("Product added successfully!")
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
            showToast("Product updated successfully!")
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showToast("Product removed.")
        }
    }

    // Customer Actions
    fun addCustomer(name: String, phone: String, email: String, address: String, notes: String, tags: String) {
        viewModelScope.launch {
            val isDemoMode = business.value?.isDemo ?: false
            val cust = Customer(
                name = name, phone = phone, email = email,
                address = address, notes = notes, tags = tags, isDemo = isDemoMode
            )
            repository.addCustomer(cust)
            showToast("Customer added successfully!")
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            showToast("Customer removed.")
        }
    }

    // Sale Recording
    fun recordSale(
        customerId: Int?,
        customerName: String,
        paymentMethod: String,
        discount: Double,
        tax: Double,
        items: List<Pair<Product, Int>>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val isDemoMode = business.value?.isDemo ?: false
                
                // Calculate total & profit
                var subtotal = 0.0
                var totalCost = 0.0
                for (item in items) {
                    subtotal += item.first.sellingPrice * item.second
                    totalCost += item.first.purchasePrice * item.second
                }
                val total = subtotal + tax - discount
                val profitEstimate = total - totalCost

                val sale = Sale(
                    customerId = customerId,
                    customerName = customerName,
                    paymentStatus = "Paid",
                    paymentMethod = paymentMethod,
                    discount = discount,
                    tax = tax,
                    total = total,
                    profitEstimate = profitEstimate,
                    isDemo = isDemoMode
                )

                val saleItems = items.map {
                    SaleItem(
                        saleId = 0,
                        productId = it.first.id,
                        productName = it.first.name,
                        quantity = it.second,
                        price = it.first.sellingPrice
                    )
                }

                repository.recordSale(sale, saleItems)
                showToast("Sale recorded! Total: ${business.value?.currency ?: "INR"} $total")
            } catch (e: Exception) {
                showToast("Error recording sale: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getSaleItems(saleId: Int): List<SaleItem> {
        return repository.getSaleItems(saleId)
    }

    // Expenses Actions
    fun addExpense(name: String, category: String, amount: Double, paymentMethod: String, notes: String, receiptUri: String?) {
        viewModelScope.launch {
            val isDemoMode = business.value?.isDemo ?: false
            val exp = Expense(
                name = name, category = category, amount = amount,
                paymentMethod = paymentMethod, notes = notes,
                receiptImageUri = receiptUri, isDemo = isDemoMode
            )
            repository.addExpense(exp)
            showToast("Expense recorded: $name")
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            showToast("Expense deleted.")
        }
    }

    // Suppliers Actions
    fun addSupplier(name: String, contact: String, email: String, products: String, dues: Double) {
        viewModelScope.launch {
            val isDemoMode = business.value?.isDemo ?: false
            val sup = Supplier(
                name = name, contactDetails = contact, email = email,
                productsSupplied = products, outstandingPayments = dues, isDemo = isDemoMode
            )
            repository.addSupplier(sup)
            showToast("Supplier added: $name")
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
            showToast("Supplier removed.")
        }
    }

    // Alerts
    fun dismissAlert(id: Int) {
        viewModelScope.launch {
            repository.deleteAlert(id)
        }
    }

    fun markAllAlertsRead() {
        viewModelScope.launch {
            repository.markAllAlertsAsRead()
            showToast("All alerts marked as read.")
        }
    }

    // Marketing Campaign Creation
    fun createCampaign(title: String, platform: String, content: String) {
        viewModelScope.launch {
            val isDemoMode = business.value?.isDemo ?: false
            val cam = Campaign(
                title = title, platform = platform, content = content, isDemo = isDemoMode
            )
            repository.addCampaign(cam)
            showToast("Campaign saved in AI Marketing Studio! 📢")
        }
    }

    // Ask BizPilot AI chat
    fun sendMessage(text: String) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            // Add user message to local state instantly
            val userMsg = AIChatMessage(role = "user", content = text)
            repository.addMessage(userMsg)

            _isLoading.value = true
            try {
                val responseMsg = repository.processAskBizPilot(text)
                repository.addMessage(responseMsg)
            } catch (e: Exception) {
                // Network/parsing fallback
                val errorMsg = AIChatMessage(
                    role = "model",
                    content = "Sorry, I encountered a connection issue. Your data is perfectly safe!\n" +
                              "Fallback Local Answer: today's recorded transactions are processed successfully."
                )
                repository.addMessage(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Execute Proposed AI Action
    fun executeAIProposedAction(messageId: Int, actionType: String, actionDataJson: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val json = JSONObject(actionDataJson)
                when (actionType) {
                    "CREATE_PRODUCT" -> {
                        val name = json.optString("name", "AI Product")
                        val category = json.optString("category", "General")
                        val buyPrice = json.optDouble("purchasePrice", 0.0)
                        val sellPrice = json.optDouble("sellingPrice", 0.0)
                        val stock = json.optInt("currentStock", 0)
                        val minStock = json.optInt("minimumStock", 5)
                        val isDemoMode = business.value?.isDemo ?: false
                        
                        repository.addProduct(
                            Product(
                                name = name, category = category,
                                purchasePrice = buyPrice, sellingPrice = sellPrice,
                                currentStock = stock, minimumStock = minStock,
                                isDemo = isDemoMode
                            )
                        )
                        showToast("Action Executed: Created product $name")
                    }
                    "RECORD_EXPENSE" -> {
                        val name = json.optString("name", "AI Expense")
                        val category = json.optString("category", "Other")
                        val amount = json.optDouble("amount", 0.0)
                        val isDemoMode = business.value?.isDemo ?: false
                        
                        repository.addExpense(
                            Expense(
                                name = name, category = category, amount = amount, isDemo = isDemoMode
                            )
                        )
                        showToast("Action Executed: Recorded expense of $amount for $name")
                    }
                    "REORDER_STOCK" -> {
                        val pId = json.optInt("productId")
                        val qty = json.optInt("quantity", 10)
                        val supplierName = json.optString("supplier", "General Supplier")
                        
                        // Execute reorder (simulate by adding quantity to current stock)
                        val currentProd = products.value.firstOrNull { it.id == pId }
                        if (currentProd != null) {
                            val updatedProduct = currentProd.copy(currentStock = currentProd.currentStock + qty)
                            repository.updateProduct(updatedProduct)
                            
                            // Outstanding supplier payments update if exists
                            val sup = suppliers.value.firstOrNull { it.name.equals(supplierName, ignoreCase = true) }
                            if (sup != null) {
                                repository.updateSupplier(
                                    sup.copy(outstandingPayments = sup.outstandingPayments + (currentProd.purchasePrice * qty))
                                )
                            }
                            
                            showToast("Purchase Order generated! Added $qty units to ${currentProd.name}")
                        } else {
                            showToast("Could not execute reorder: Product ID $pId not found")
                        }
                    }
                    "FOLLOW_UP" -> {
                        val cId = json.optInt("customerId")
                        val msg = json.optString("message", "Follow-up")
                        
                        val customer = customers.value.firstOrNull { it.id == cId }
                        if (customer != null) {
                            // Update customer notes to log follow-up
                            val currentNotes = customer.notes
                            val updatedNotes = "$currentNotes\n[Sent WhatsApp follow-up on ${Calendar.getInstance().time}]"
                            repository.updateCustomer(customer.copy(notes = updatedNotes))
                            showToast("WhatsApp follow-up simulated for ${customer.name}!")
                        } else {
                            showToast("Customer ID $cId not found")
                        }
                    }
                    "CREATE_CAMPAIGN" -> {
                        val title = json.optString("title", "AI Campaign")
                        val platform = json.optString("platform", "WhatsApp")
                        val content = json.optString("content", "")
                        
                        repository.addCampaign(
                            Campaign(
                                title = title, platform = platform, content = content, isDemo = business.value?.isDemo ?: false
                            )
                        )
                        showToast("Campaign '$title' saved in Marketing Studio!")
                    }
                    else -> {
                        showToast("Action type $actionType is not yet implemented.")
                    }
                }
                // Mark message action executed
                repository.markActionExecuted(messageId)
            } catch (e: Exception) {
                showToast("Failed to execute action: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Invoice/Quotation sharing simulation helper
    fun shareDocument(type: String, title: String) {
        showToast("Sharing $type: $title via system share sheet... 📲")
    }

    // Document/Image Receipt OCR Parsing simulation
    fun processReceiptOcr(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulating an advanced OCR scanning wait
                kotlinx.coroutines.delay(2000)
                
                // OCR result populated intelligently
                val mockResult = OcrResult(
                    supplier = "Amul Milk Distributor",
                    invoiceNumber = "AM-2026-9874",
                    productName = "Amul Fresh Butter 500g",
                    quantity = 20,
                    price = 220.0,
                    tax = 180.0,
                    total = 4580.0
                )
                _ocrReviewData.value = mockResult
                showToast("OCR scanning completed! Please review extracted data.")
            } catch (e: Exception) {
                showToast("Failed to scan document: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmOcrSave() {
        val ocr = _ocrReviewData.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val isDemoMode = business.value?.isDemo ?: false
                // Add supplier
                val exSupplier = suppliers.value.firstOrNull { it.name.equals(ocr.supplier, ignoreCase = true) }
                if (exSupplier == null) {
                    repository.addSupplier(
                        Supplier(
                            name = ocr.supplier,
                            contactDetails = "+91 (OCR Saved)",
                            outstandingPayments = ocr.total,
                            productsSupplied = ocr.productName,
                            isDemo = isDemoMode
                        )
                    )
                } else {
                    repository.updateSupplier(
                        exSupplier.copy(
                            outstandingPayments = exSupplier.outstandingPayments + ocr.total
                        )
                    )
                }

                // Add as expense transaction
                repository.addExpense(
                    Expense(
                        name = "Stock: ${ocr.productName}",
                        category = "Inventory",
                        amount = ocr.total,
                        notes = "OCR parsed Invoice #${ocr.invoiceNumber}. Qty: ${ocr.quantity}.",
                        isDemo = isDemoMode
                    )
                )

                _ocrReviewData.value = null
                showToast("Extracted transaction saved successfully!")
            } catch (e: Exception) {
                showToast("Error saving OCR transaction: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelOcr() {
        _ocrReviewData.value = null
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BizPilotViewModel::class.java)) {
                return BizPilotViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class OcrResult(
    val supplier: String,
    val invoiceNumber: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val tax: Double,
    val total: Double
)
