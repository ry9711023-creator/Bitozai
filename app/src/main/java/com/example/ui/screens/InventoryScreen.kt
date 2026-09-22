package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.viewmodel.BizPilotViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: BizPilotViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val business by viewModel.business.collectAsState()
    val currencySymbol = if (business?.currency == "INR") "₹" else business?.currency ?: "$"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Dialog state
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    // Aggregate statistics
    val totalProducts = products.size
    val totalStockValue = products.sumOf { it.currentStock * it.purchasePrice }
    val lowStockCount = products.count { it.currentStock <= it.minimumStock }
    val outOfStockCount = products.count { it.currentStock == 0 }

    // Categories derived dynamically from products + defaults
    val categories = remember(products) {
        val list = products.map { it.category }.filter { it.isNotEmpty() }.distinct().toMutableList()
        if (!list.contains("Beverages")) list.add("Beverages")
        if (!list.contains("Bakery")) list.add("Bakery")
        if (!list.contains("Snacks")) list.add("Snacks")
        list.add(0, "All")
        list
    }

    val filteredProducts = products.filter { prod ->
        val matchesSearch = prod.name.contains(searchQuery, ignoreCase = true) || 
                            prod.sku.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Management", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.showToast("Barcode Reader: Camera permission required. Coming soon!") }) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan SKU Barcode")
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp).testTag("add_product_action_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 13.sp)
                    }
                }
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
            // Stats summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InventoryStatItem(label = "Total Items", value = totalProducts.toString())
                    InventoryStatItem(label = "Value", value = "$currencySymbol${String.format(Locale.US, "%,.0f", totalStockValue)}")
                    InventoryStatItem(label = "Low Stock", value = lowStockCount.toString(), isAlert = lowStockCount > 0)
                    InventoryStatItem(label = "Out of Stock", value = outOfStockCount.toString(), isAlert = outOfStockCount > 0, critical = true)
                }
            }

            // Search Bar & Filters
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search product name or SKU...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("product_search_input"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Category filter row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_chip_$cat")
                    )
                }
            }

            // Products list
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No products found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try clearing search filter or add a product.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemRow(
                            product = product,
                            currencySymbol = currencySymbol,
                            onEdit = { editingProduct = product },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }
    }

    // Add Product Modal Sheet / Dialog
    if (showAddDialog) {
        ProductFormDialog(
            title = "Add New Product",
            currencySymbol = currencySymbol,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, sku, cat, purchase, sell, stock, min, supplier, unit, desc ->
                viewModel.addProduct(
                    name = name, sku = sku, category = cat,
                    purchasePrice = purchase, sellingPrice = sell,
                    currentStock = stock, minimumStock = min,
                    supplier = supplier, tax = 0.0, unit = unit, description = desc
                )
                showAddDialog = false
            }
        )
    }

    // Edit Product Modal Dialog
    if (editingProduct != null) {
        val prod = editingProduct!!
        ProductFormDialog(
            title = "Edit Product",
            product = prod,
            currencySymbol = currencySymbol,
            onDismiss = { editingProduct = null },
            onConfirm = { name, sku, cat, purchase, sell, stock, min, supplier, unit, desc ->
                viewModel.updateProduct(
                    prod.copy(
                        name = name, sku = sku, category = cat,
                        purchasePrice = purchase, sellingPrice = sell,
                        currentStock = stock, minimumStock = min,
                        supplier = supplier, unit = unit, description = desc
                    )
                )
                editingProduct = null
            }
        )
    }
}

@Composable
fun InventoryStatItem(
    label: String,
    value: String,
    isAlert: Boolean = false,
    critical: Boolean = false
) {
    val valColor = if (critical && isAlert) {
        MaterialTheme.colorScheme.error
    } else if (isAlert) {
        Color(0xFFFF9800)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valColor)
    }
}

@Composable
fun ProductItemRow(
    product: Product,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = product.currentStock <= product.minimumStock
    val isOutOfStock = product.currentStock == 0

    val badgeColor = if (isOutOfStock) {
        MaterialTheme.colorScheme.errorContainer
    } else if (isLowStock) {
        Color(0xFFFFE0B2)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    }

    val badgeTextColor = if (isOutOfStock) {
        MaterialTheme.colorScheme.onErrorContainer
    } else if (isLowStock) {
        Color(0xFFE65100)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isOutOfStock) "Out of Stock" 
                                   else if (isLowStock) "Low Stock" 
                                   else "${product.currentStock} ${product.unit}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "SKU: ${product.sku.ifEmpty { "N/A" }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Sale: $currencySymbol${product.sellingPrice}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Cost: $currencySymbol${product.purchasePrice}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Product",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    title: String,
    product: Product? = null,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String, sku: String, category: String,
        purchasePrice: Double, sellingPrice: Double,
        currentStock: Int, minimumStock: Int,
        supplier: String, unit: String, description: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var purchasePrice by remember { mutableStateOf(product?.purchasePrice?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var currentStock by remember { mutableStateOf(product?.currentStock?.toString() ?: "") }
    var minimumStock by remember { mutableStateOf(product?.minimumStock?.toString() ?: "") }
    var supplier by remember { mutableStateOf(product?.supplier ?: "") }
    var unit by remember { mutableStateOf(product?.unit ?: "pcs") }
    var description by remember { mutableStateOf(product?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product/Service Name") },
                    modifier = Modifier.fillMaxWidth().testTag("form_product_name")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f).testTag("form_product_category")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("Cost Price ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("form_product_purchase")
                    )
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = { Text("Selling Price ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("form_product_selling")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentStock,
                        onValueChange = { currentStock = it },
                        label = { Text("Current Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("form_product_stock")
                    )
                    OutlinedTextField(
                        value = minimumStock,
                        onValueChange = { minimumStock = it },
                        label = { Text("Min Stock Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("form_product_min")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (e.g. pcs, cups)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("Supplier") },
                        modifier = Modifier.weight(1.5f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        onConfirm(
                            name,
                            sku,
                            category,
                            purchasePrice.toDoubleOrNull() ?: 0.0,
                            sellingPrice.toDoubleOrNull() ?: 0.0,
                            currentStock.toIntOrNull() ?: 0,
                            minimumStock.toIntOrNull() ?: 5,
                            supplier,
                            unit,
                            description
                        )
                    }
                },
                modifier = Modifier.testTag("form_confirm_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
