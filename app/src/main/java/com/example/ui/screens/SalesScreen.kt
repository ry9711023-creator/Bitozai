package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Share
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
import com.example.data.Customer
import com.example.data.Product
import com.example.data.Sale
import com.example.ui.viewmodel.BizPilotViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: BizPilotViewModel,
    modifier: Modifier = Modifier
) {
    val sales by viewModel.sales.collectAsState()
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val business by viewModel.business.collectAsState()

    val currencySymbol = if (business?.currency == "INR") "₹" else business?.currency ?: "$"

    var activeTab by remember { mutableStateOf(0) } // 0: Record Sale (POS), 1: Sales History (Ledger)

    // Current invoice builders
    val selectedItems = remember { mutableStateListOf<Pair<Product, Int>>() }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var paymentMethod by remember { mutableStateOf("UPI") }
    var discountInput by remember { mutableStateOf("") }
    var taxInput by remember { mutableStateOf("") }

    // Dialog & Detail states
    var activeInvoiceDetails by remember { mutableStateOf<Sale?>(null) }
    var invoiceItems by remember { mutableStateOf<List<com.example.data.SaleItem>>(emptyList()) }

    LaunchedEffect(activeInvoiceDetails) {
        val details = activeInvoiceDetails
        if (details != null) {
            invoiceItems = viewModel.getSaleItems(details.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales & Invoicing", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tabs
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("POS Terminal", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("pos_tab")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Sales Ledger", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("ledger_tab")
                )
            }

            if (activeTab == 0) {
                // POS TERMINAL VIEW
                POSView(
                    products = products,
                    customers = customers,
                    selectedItems = selectedItems,
                    selectedCustomer = selectedCustomer,
                    paymentMethod = paymentMethod,
                    discountInput = discountInput,
                    taxInput = taxInput,
                    currencySymbol = currencySymbol,
                    onCustomerSelect = { selectedCustomer = it },
                    onPaymentMethodSelect = { paymentMethod = it },
                    onDiscountChange = { discountInput = it },
                    onTaxChange = { taxInput = it },
                    onItemQuantityChange = { prod, qty ->
                        val existingIndex = selectedItems.indexOfFirst { it.first.id == prod.id }
                        if (existingIndex != -1) {
                            if (qty <= 0) {
                                selectedItems.removeAt(existingIndex)
                            } else {
                                selectedItems[existingIndex] = Pair(prod, qty)
                            }
                        } else if (qty > 0) {
                            selectedItems.add(Pair(prod, qty))
                        }
                    },
                    onCompleteSale = {
                        if (selectedItems.isNotEmpty()) {
                            viewModel.recordSale(
                                customerId = selectedCustomer?.id,
                                customerName = selectedCustomer?.name ?: "Walk-in Customer",
                                paymentMethod = paymentMethod,
                                discount = discountInput.toDoubleOrNull() ?: 0.0,
                                tax = taxInput.toDoubleOrNull() ?: 0.0,
                                items = selectedItems.toList()
                            )
                            // Clear
                            selectedItems.clear()
                            selectedCustomer = null
                            discountInput = ""
                            taxInput = ""
                            activeTab = 1 // Switch to ledger to see invoice
                        } else {
                            viewModel.showToast("Please add at least 1 item to checkout!")
                        }
                    }
                )
            } else {
                // SALES LEDGER VIEW
                SalesLedgerView(
                    sales = sales,
                    currencySymbol = currencySymbol,
                    onViewInvoice = { activeInvoiceDetails = it }
                )
            }
        }
    }

    // Invoice Detail modal dialog (Share, Preview, Details)
    if (activeInvoiceDetails != null) {
        val sale = activeInvoiceDetails!!
        AlertDialog(
            onDismissRequest = { activeInvoiceDetails = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Invoice #${sale.id}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    IconButton(onClick = { viewModel.shareDocument("Invoice", "#Invoice-${sale.id}") }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share Invoice")
                    }
                }
            },
            text = {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Business Branding Header
                        Text(
                            text = business?.name ?: "BizPilot Store",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = "GSTIN: 09AAPCB1234F1Z1 • Contact: ${business?.phone}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Customer Metadata
                        Text(text = "Billed To: **${sale.customerName}**", fontSize = 12.sp)
                        Text(text = "Date: ${SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date(sale.date))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Payment Mode: ${sale.paymentMethod} (${sale.paymentStatus})", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Items
                        Text(text = "Items Ordered:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        invoiceItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${item.productName} (x${item.quantity})", fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                                Text(text = "$currencySymbol${item.price}", fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                Text(text = "$currencySymbol${item.price * item.quantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Financial totals
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Discount:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "-$currencySymbol${sale.discount}", fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Tax:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "+$currencySymbol${sale.tax}", fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total Paid:", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "$currencySymbol${sale.total}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { activeInvoiceDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun POSView(
    products: List<Product>,
    customers: List<Customer>,
    selectedItems: List<Pair<Product, Int>>,
    selectedCustomer: Customer?,
    paymentMethod: String,
    discountInput: String,
    taxInput: String,
    currencySymbol: String,
    onCustomerSelect: (Customer?) -> Unit,
    onPaymentMethodSelect: (String) -> Unit,
    onDiscountChange: (String) -> Unit,
    onTaxChange: (String) -> Unit,
    onItemQuantityChange: (Product, Int) -> Unit,
    onCompleteSale: () -> Unit
) {
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }

    val subtotal = selectedItems.sumOf { it.first.sellingPrice * it.second }
    val discount = discountInput.toDoubleOrNull() ?: 0.0
    val tax = taxInput.toDoubleOrNull() ?: 0.0
    val grandTotal = (subtotal + tax - discount).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // POS Terminal Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Checkout POS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { showProductPicker = true },
                    modifier = Modifier.testTag("pos_add_item_button")
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item")
                }
            }
        }

        // Active invoice items
        if (selectedItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Receipt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("POS is empty", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Select products to add to invoice", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        } else {
            items(selectedItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(text = item.first.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Rate: $currencySymbol${item.first.sellingPrice}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Quantity counter controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = { onItemQuantityChange(item.first, item.second - 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrement")
                        }
                        Text(text = item.second.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { onItemQuantityChange(item.first, item.second + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Increment")
                        }
                    }

                    Text(
                        text = "$currencySymbol${item.first.sellingPrice * item.second}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.7f),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Customer mapping card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = selectedCustomer?.name ?: "Walk-in Customer", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = selectedCustomer?.phone ?: "No details linked", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Button(
                        onClick = { showCustomerPicker = true },
                        modifier = Modifier.testTag("pos_customer_picker_button")
                    ) {
                        Text("Link Customer", fontSize = 11.sp)
                    }
                }
            }
        }

        // Payment configurations and discounts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Payment & Ledger Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    // Payment method choices chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf("UPI", "Cash", "Card", "Credit")
                        modes.forEach { m ->
                            val active = m == paymentMethod
                            FilterChip(
                                selected = active,
                                onClick = { onPaymentMethodSelect(m) },
                                label = { Text(m, fontSize = 11.sp) },
                                modifier = Modifier.testTag("payment_chip_$m")
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = onDiscountChange,
                            label = { Text("Discount ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("discount_input")
                        )
                        OutlinedTextField(
                            value = taxInput,
                            onValueChange = onTaxChange,
                            label = { Text("Tax ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("tax_input")
                        )
                    }
                }
            }
        }

        // Summary details total
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currencySymbol$subtotal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("-$currencySymbol$discount", fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tax", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+$currencySymbol$tax", fontSize = 13.sp)
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("$currencySymbol$grandTotal", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Complete sale
        item {
            Button(
                onClick = onCompleteSale,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("pos_checkout_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Complete & Print Invoice 🧾", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal pickers
    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text("Select Customer") },
            text = {
                LazyColumn(modifier = Modifier.height(250.dp)) {
                    items(customers) { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCustomerSelect(c)
                                    showCustomerPicker = false
                                }
                                .padding(12.dp)
                                .testTag("picker_customer_${c.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(c.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(c.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerPicker = false }) { Text("Cancel") }
            }
        )
    }

    if (showProductPicker) {
        AlertDialog(
            onDismissRequest = { showProductPicker = false },
            title = { Text("Add Products") },
            text = {
                LazyColumn(modifier = Modifier.height(250.dp)) {
                    items(products) { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemQuantityChange(p, 1)
                                    showProductPicker = false
                                }
                                .padding(12.dp)
                                .testTag("picker_product_${p.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Price: $currencySymbol${p.sellingPrice} • Stock: ${p.currentStock}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProductPicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SalesLedgerView(
    sales: List<Sale>,
    currencySymbol: String,
    onViewInvoice: (Sale) -> Unit
) {
    if (sales.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Receipt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No sales records yet.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Complete checkouts in POS Terminal to populate.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sales, key = { it.id }) { sale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewInvoice(sale) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Invoice #${sale.id}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = sale.paymentMethod,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sale.customerName,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = SimpleDateFormat("dd-MMM hh:mm a", Locale.getDefault()).format(Date(sale.date)),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$currencySymbol${sale.total}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${sale.paymentStatus}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (sale.paymentStatus == "Paid") Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }
        }
    }
}
