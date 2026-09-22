package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Expense
import com.example.data.Supplier
import com.example.ui.viewmodel.BizPilotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: BizPilotViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val business by viewModel.business.collectAsState()
    val ocrReviewData by viewModel.ocrReviewData.collectAsState()
    val currencySymbol = if (business?.currency == "INR") "₹" else business?.currency ?: "$"

    var currentView by remember { mutableStateOf("MENU") } // MENU, EXPENSES, SUPPLIERS, BILLING, SETTINGS
    
    // Expenses fields
    var showAddExpense by remember { mutableStateOf(false) }
    var expenseName by remember { mutableStateOf("") }
    var expenseCategory by remember { mutableStateOf("Rent") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseMethod by remember { mutableStateOf("Cash") }
    var expenseNotes by remember { mutableStateOf("") }

    // Supplier fields
    var showAddSupplier by remember { mutableStateOf(false) }
    var supplierName by remember { mutableStateOf("") }
    var supplierContact by remember { mutableStateOf("") }
    var supplierEmail by remember { mutableStateOf("") }
    var supplierProducts by remember { mutableStateOf("") }
    var supplierDues by remember { mutableStateOf("") }

    // Settings fields
    var customInstructions by remember { mutableStateOf(business?.aiInstructions ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (currentView) {
                            "EXPENSES" -> "Expenses Ledger"
                            "SUPPLIERS" -> "Supplier Management"
                            "BILLING" -> "SaaS Plans & Billing"
                            "SETTINGS" -> "BizPilot Settings"
                            else -> "More Operations"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (currentView != "MENU") {
                        IconButton(onClick = { currentView = "MENU" }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentView) {
                "MENU" -> MoreMenuListView(
                    onExpenses = { currentView = "EXPENSES" },
                    onSuppliers = { currentView = "SUPPLIERS" },
                    onBilling = { currentView = "BILLING" },
                    onSettings = { currentView = "SETTINGS" },
                    onLogout = { viewModel.logout() }
                )

                "EXPENSES" -> ExpensesModuleView(
                    expenses = expenses,
                    currencySymbol = currencySymbol,
                    showAddForm = showAddExpense,
                    onShowForm = { showAddExpense = it },
                    expenseName = expenseName, onNameChange = { expenseName = it },
                    expenseCategory = expenseCategory, onCategoryChange = { expenseCategory = it },
                    expenseAmount = expenseAmount, onAmountChange = { expenseAmount = it },
                    expenseMethod = expenseMethod, onMethodChange = { expenseMethod = it },
                    expenseNotes = expenseNotes, onNotesChange = { expenseNotes = it },
                    ocrReviewData = ocrReviewData,
                    onSimulateOcr = { viewModel.processReceiptOcr(Uri.EMPTY) },
                    onConfirmOcr = { viewModel.confirmOcrSave() },
                    onCancelOcr = { viewModel.cancelOcr() },
                    onSaveExpense = {
                        if (expenseName.isNotEmpty() && expenseAmount.isNotEmpty()) {
                            viewModel.addExpense(
                                name = expenseName,
                                category = expenseCategory,
                                amount = expenseAmount.toDoubleOrNull() ?: 0.0,
                                paymentMethod = expenseMethod,
                                notes = expenseNotes,
                                receiptUri = null
                            )
                            expenseName = ""
                            expenseAmount = ""
                            expenseNotes = ""
                            showAddExpense = false
                        } else {
                            viewModel.showToast("Please enter name and amount.")
                        }
                    },
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )

                "SUPPLIERS" -> SuppliersModuleView(
                    suppliers = suppliers,
                    currencySymbol = currencySymbol,
                    showAddForm = showAddSupplier,
                    onShowForm = { showAddSupplier = it },
                    supplierName = supplierName, onNameChange = { supplierName = it },
                    supplierContact = supplierContact, onContactChange = { supplierContact = it },
                    supplierEmail = supplierEmail, onEmailChange = { supplierEmail = it },
                    supplierProducts = supplierProducts, onProductsChange = { supplierProducts = it },
                    supplierDues = supplierDues, onDuesChange = { supplierDues = it },
                    onSaveSupplier = {
                        if (supplierName.isNotEmpty()) {
                            viewModel.addSupplier(
                                name = supplierName,
                                contact = supplierContact,
                                email = supplierEmail,
                                products = supplierProducts,
                                dues = supplierDues.toDoubleOrNull() ?: 0.0
                            )
                            supplierName = ""
                            supplierContact = ""
                            supplierEmail = ""
                            supplierProducts = ""
                            supplierDues = ""
                            showAddSupplier = false
                        } else {
                            viewModel.showToast("Please enter supplier name.")
                        }
                    },
                    onDeleteSupplier = { viewModel.deleteSupplier(it) }
                )

                "BILLING" -> SaaSPlanView(viewModel)

                "SETTINGS" -> SettingsModuleView(
                    business = business,
                    instructions = customInstructions,
                    onInstructionsChange = { customInstructions = it },
                    onSave = {
                        val current = business ?: return@SettingsModuleView
                        // Update
                        viewModel.completeOnboarding(
                            name = current.name,
                            owner = current.ownerName,
                            category = current.category,
                            country = current.country,
                            currency = current.currency,
                            phone = current.phone,
                            address = current.address,
                            hours = current.openingHours,
                            employees = current.numEmployees,
                            salesRange = current.salesRange,
                            aiInstructions = customInstructions
                        )
                        currentView = "MENU"
                    }
                )
            }
        }
    }
}

@Composable
fun MoreMenuListView(
    onExpenses: () -> Unit,
    onSuppliers: () -> Unit,
    onBilling: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MenuNavigationItem(
                title = "Expense Tracking",
                subtitle = "Log daily operations, rent, bills, or scan OCR receipts",
                icon = Icons.Default.Receipt,
                onClick = onExpenses,
                testTag = "menu_expenses_button"
            )
        }
        item {
            MenuNavigationItem(
                title = "Supplier Directory",
                subtitle = "Manage product distributors and outstanding dues",
                icon = Icons.Default.LocalShipping,
                onClick = onSuppliers,
                testTag = "menu_suppliers_button"
            )
        }
        item {
            MenuNavigationItem(
                title = "SaaS Billing & Subscriptions",
                subtitle = "Current plan: PRO Beta Adopters. Manage options",
                icon = Icons.Outlined.CreditCard,
                onClick = onBilling,
                testTag = "menu_billing_button"
            )
        }
        item {
            MenuNavigationItem(
                title = "BizPilot Settings",
                subtitle = "Change business profile parameters and custom AI guidelines",
                icon = Icons.Outlined.Settings,
                onClick = onSettings,
                testTag = "menu_settings_button"
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("menu_logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Database & Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MenuNavigationItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ExpensesModuleView(
    expenses: List<Expense>,
    currencySymbol: String,
    showAddForm: Boolean,
    onShowForm: (Boolean) -> Unit,
    expenseName: String, onNameChange: (String) -> Unit,
    expenseCategory: String, onCategoryChange: (String) -> Unit,
    expenseAmount: String, onAmountChange: (String) -> Unit,
    expenseMethod: String, onMethodChange: (String) -> Unit,
    expenseNotes: String, onNotesChange: (String) -> Unit,
    ocrReviewData: com.example.ui.viewmodel.OcrResult?,
    onSimulateOcr: () -> Unit,
    onConfirmOcr: () -> Unit,
    onCancelOcr: () -> Unit,
    onSaveExpense: () -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Expenses Ledger", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSimulateOcr,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("ocr_button")
                    ) {
                        Icon(Icons.Outlined.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI OCR", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onShowForm(!showAddForm) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("add_expense_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp)
                    }
                }
            }
        }

        // Render OCR Review block if active
        if (ocrReviewData != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Extracted Invoice Data", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("• Supplier: **${ocrReviewData.supplier}**", fontSize = 13.sp)
                        Text("• Product: **${ocrReviewData.productName}**", fontSize = 13.sp)
                        Text("• Qty: **${ocrReviewData.quantity}** • Rate: **$currencySymbol${ocrReviewData.price}**", fontSize = 13.sp)
                        Text("• Tax: **$currencySymbol${ocrReviewData.tax}**", fontSize = 13.sp)
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Extracted Total: $currencySymbol${ocrReviewData.total}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = onCancelOcr) { Text("Discard") }
                                Button(onClick = onConfirmOcr, shape = RoundedCornerShape(6.dp)) { Text("Save Transaction", fontSize = 11.sp) }
                            }
                        }
                    }
                }
            }
        }

        // Render manual Add Expense form if active
        if (showAddForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Record Manual Expense", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        OutlinedTextField(
                            value = expenseName,
                            onValueChange = onNameChange,
                            label = { Text("Expense Title / Supplier") },
                            modifier = Modifier.fillMaxWidth().testTag("form_expense_name")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = expenseAmount,
                                onValueChange = onAmountChange,
                                label = { Text("Amount ($currencySymbol)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("form_expense_amount")
                            )
                            OutlinedTextField(
                                value = expenseCategory,
                                onValueChange = onCategoryChange,
                                label = { Text("Category") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = expenseNotes,
                            onValueChange = onNotesChange,
                            label = { Text("Notes / Receipt Ref") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onShowForm(false) }) { Text("Cancel") }
                            Button(onClick = onSaveExpense, modifier = Modifier.testTag("form_expense_save_button")) { Text("Save") }
                        }
                    }
                }
            }
        }

        // Expenses list ledger
        if (expenses.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No expenses recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        } else {
            items(expenses, key = { it.id }) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(exp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Category: ${exp.category} • notes: ${exp.notes.ifEmpty { "None" }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$currencySymbol${exp.amount}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                            IconButton(onClick = { onDeleteExpense(exp) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Expense", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuppliersModuleView(
    suppliers: List<Supplier>,
    currencySymbol: String,
    showAddForm: Boolean,
    onShowForm: (Boolean) -> Unit,
    supplierName: String, onNameChange: (String) -> Unit,
    supplierContact: String, onContactChange: (String) -> Unit,
    supplierEmail: String, onEmailChange: (String) -> Unit,
    supplierProducts: String, onProductsChange: (String) -> Unit,
    supplierDues: String, onDuesChange: (String) -> Unit,
    onSaveSupplier: () -> Unit,
    onDeleteSupplier: (Supplier) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Supplier Directory", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { onShowForm(!showAddForm) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("add_supplier_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 12.sp)
                }
            }
        }

        if (showAddForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add Supplier Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        OutlinedTextField(
                            value = supplierName,
                            onValueChange = onNameChange,
                            label = { Text("Distributor Name") },
                            modifier = Modifier.fillMaxWidth().testTag("form_supplier_name")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = supplierContact,
                                onValueChange = onContactChange,
                                label = { Text("Contact Phone") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = supplierEmail,
                                onValueChange = onEmailChange,
                                label = { Text("Email Address") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = supplierProducts,
                            onValueChange = onProductsChange,
                            label = { Text("Products Supplied") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = supplierDues,
                            onValueChange = onDuesChange,
                            label = { Text("Outstanding Payments ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onShowForm(false) }) { Text("Cancel") }
                            Button(onClick = onSaveSupplier, modifier = Modifier.testTag("form_supplier_save_button")) { Text("Save") }
                        }
                    }
                }
            }
        }

        if (suppliers.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No suppliers profiles linked.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        } else {
            items(suppliers, key = { it.id }) { sup ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(sup.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Products: ${sup.productsSupplied}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Contact: ${sup.contactDetails} • ${sup.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (sup.outstandingPayments > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Due: $currencySymbol${sup.outstandingPayments}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteSupplier(sup) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Supplier", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaaSPlanView(viewModel: BizPilotViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("SaaS Pricing Plans", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("We believe in clean pricing with maximum utility.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACTIVE PLAN: PRO SUITE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹1,499 / Month", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Free Trial active for early beta adopters • Local Sandbox Mode enabled.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))
                    PlanBullet(text = "All AI Employee Chat Conversations")
                    PlanBullet(text = "Unlimited product catalogs & custom categories")
                    PlanBullet(text = "Intelligent WhatsApp promotional builders")
                    PlanBullet(text = "Local SQLite SQLite transactional sandboxing")
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { viewModel.showToast("Stripe Payment Provider integration initialized!") },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Manage Active Credit Cards / Subscriptions 💳", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PlanBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp)
    }
}

@Composable
fun SettingsModuleView(
    business: com.example.data.Business?,
    instructions: String,
    onInstructionsChange: (String) -> Unit,
    onSave: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Business Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Fine-tune your personal AI business operator model memory.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("AI Prompts Preferences", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = onInstructionsChange,
                        label = { Text("Guidelines & Operating Rules") },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        maxLines = 10
                    )
                }
            }
        }

        item {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_settings_button")
            ) {
                Text("Save Guidelines Preferences", fontWeight = FontWeight.Bold)
            }
        }
    }
}
