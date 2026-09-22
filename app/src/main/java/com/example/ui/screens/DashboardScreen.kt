package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Alert
import com.example.data.Business
import com.example.data.Product
import com.example.data.Sale
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BizPilotViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BizPilotViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val business by viewModel.business.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val unreadAlertsCount = remember(alerts) { alerts.count { !it.isRead } }
    
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Row(modifier = modifier.fillMaxSize()) {
        // Desktop / Tablet Sidebar Navigation
        if (isTablet) {
            NavigationSidebar(
                currentScreen = currentScreen,
                businessName = business?.name ?: "BizPilot",
                onNavigate = { viewModel.navigateTo(it) },
                unreadAlerts = unreadAlertsCount,
                onLogout = { viewModel.logout() }
            )
            VerticalDivider()
        }

        // Main App Scaffold (Holds Screen Content + Mobile Bottom Nav)
        Scaffold(
            bottomBar = {
                if (!isTablet) {
                    NavigationBottomBar(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) },
                        unreadAlerts = unreadAlertsCount
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Multi-screen Router
                when (currentScreen) {
                    AppScreen.DASHBOARD -> HomeView(viewModel)
                    AppScreen.INVENTORY -> InventoryScreen(viewModel)
                    AppScreen.SALES -> SalesScreen(viewModel)
                    AppScreen.CUSTOMERS -> CustomersScreen(viewModel)
                    AppScreen.ASK_PILOT -> AskPilotScreen(viewModel)
                    AppScreen.MORE -> MoreScreen(viewModel)
                    else -> HomeView(viewModel)
                }
            }
        }
    }
}

@Composable
fun NavigationBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    unreadAlerts: Int
) {
    NavigationBar(
        modifier = Modifier.testTag("mobile_bottom_nav"),
        windowInsets = WindowInsets.navigationBars
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onNavigate(AppScreen.DASHBOARD) },
            icon = { Icon(Icons.Outlined.Dashboard, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.SALES,
            onClick = { onNavigate(AppScreen.SALES) },
            icon = { Icon(Icons.Outlined.ReceiptLong, contentDescription = "Sales") },
            label = { Text("Sales", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.INVENTORY,
            onClick = { onNavigate(AppScreen.INVENTORY) },
            icon = { Icon(Icons.Outlined.Inventory, contentDescription = "Inventory") },
            label = { Text("Inventory", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.CUSTOMERS,
            onClick = { onNavigate(AppScreen.CUSTOMERS) },
            icon = { Icon(Icons.Outlined.People, contentDescription = "Customers") },
            label = { Text("Customers", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.ASK_PILOT,
            onClick = { onNavigate(AppScreen.ASK_PILOT) },
            icon = { 
                BadgedBox(
                    badge = {
                        if (unreadAlerts > 0) {
                            Badge { Text(unreadAlerts.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Outlined.SmartToy, contentDescription = "Ask Pilot")
                }
            },
            label = { Text("Ask Pilot", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.MORE,
            onClick = { onNavigate(AppScreen.MORE) },
            icon = { Icon(Icons.Outlined.Menu, contentDescription = "More") },
            label = { Text("More", fontSize = 11.sp) }
        )
    }
}

@Composable
fun NavigationSidebar(
    currentScreen: AppScreen,
    businessName: String,
    onNavigate: (AppScreen) -> Unit,
    unreadAlerts: Int,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Brand Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = businessName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Divider(modifier = Modifier.padding(bottom = 16.dp))

            // Sidebar Items
            SidebarItem(
                selected = currentScreen == AppScreen.DASHBOARD,
                icon = Icons.Outlined.Dashboard,
                label = "Home Dashboard",
                onClick = { onNavigate(AppScreen.DASHBOARD) }
            )
            SidebarItem(
                selected = currentScreen == AppScreen.SALES,
                icon = Icons.Outlined.ReceiptLong,
                label = "Sales Ledger",
                onClick = { onNavigate(AppScreen.SALES) }
            )
            SidebarItem(
                selected = currentScreen == AppScreen.INVENTORY,
                icon = Icons.Outlined.Inventory,
                label = "Inventory & Stock",
                onClick = { onNavigate(AppScreen.INVENTORY) }
            )
            SidebarItem(
                selected = currentScreen == AppScreen.CUSTOMERS,
                icon = Icons.Outlined.People,
                label = "Customer CRM",
                onClick = { onNavigate(AppScreen.CUSTOMERS) }
            )
            SidebarItem(
                selected = currentScreen == AppScreen.ASK_PILOT,
                icon = Icons.Outlined.SmartToy,
                label = "Ask BizPilot",
                badgeCount = unreadAlerts,
                onClick = { onNavigate(AppScreen.ASK_PILOT) }
            )
            SidebarItem(
                selected = currentScreen == AppScreen.MORE,
                icon = Icons.Outlined.Menu,
                label = "More Features",
                onClick = { onNavigate(AppScreen.MORE) }
            )
        }

        // Bottom profile
        Column {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLogout() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Clear Database",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SidebarItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onError,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun HomeView(viewModel: BizPilotViewModel) {
    val business by viewModel.business.collectAsState()
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    val currency = business?.currency ?: "INR"
    val currencySymbol = if (currency == "INR") "₹" else currency

    // Calculations for Today
    val startOfDay = getStartOfDay()
    val todaySales = sales.filter { it.date >= startOfDay }
    val todaySalesAmount = todaySales.sumOf { it.total }
    val todaySalesCount = todaySales.size
    val todayProfit = todaySales.sumOf { it.profitEstimate }

    val lowStockCount = products.count { it.currentStock <= it.minimumStock }
    val pendingPayments = customers.sumOf { 
        if (it.tags.contains("Pending", ignoreCase = true)) 450.0 else 0.0 // mockup dues or tags parsing
    }
    val newCustomersCount = customers.count { it.tags.contains("New", ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Good morning, ${business?.name ?: "Store Owner"} 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${business?.category ?: "Retail"} Operator • ${business?.openingHours ?: "8:00 AM"}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (business?.isDemo == true) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DEMO MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Today's Sales",
                        value = "$currencySymbol${String.format(Locale.US, "%,.0f", todaySalesAmount)}",
                        compareText = "Today vs Yesterday",
                        isPositive = true,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Today's Orders",
                        value = todaySalesCount.toString(),
                        compareText = "Avg. ticket: $currencySymbol${if (todaySalesCount > 0) (todaySalesAmount/todaySalesCount).toInt() else 0}",
                        isPositive = null,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Gross Profit",
                        value = "$currencySymbol${String.format(Locale.US, "%,.0f", todayProfit)}",
                        compareText = "Margin: ${if (todaySalesAmount > 0) ((todayProfit / todaySalesAmount) * 100).toInt() else 0}%",
                        isPositive = true,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Low Stock Items",
                        value = lowStockCount.toString(),
                        compareText = "Needs reorder",
                        isPositive = lowStockCount == 0,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Fast Action Buttons Row
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    FastActionButton(
                        icon = Icons.Default.Chat,
                        label = "Ask Pilot",
                        onClick = { viewModel.navigateTo(AppScreen.ASK_PILOT) }
                    )
                    FastActionButton(
                        icon = Icons.Default.AddShoppingCart,
                        label = "Add Sale",
                        onClick = { viewModel.navigateTo(AppScreen.SALES) }
                    )
                    FastActionButton(
                        icon = Icons.Default.AddBox,
                        label = "Add Product",
                        onClick = { viewModel.navigateTo(AppScreen.INVENTORY) }
                    )
                    FastActionButton(
                        icon = Icons.Default.PersonAdd,
                        label = "Add Customer",
                        onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) }
                    )
                }
            }
        }

        // AI Business Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BizPilot AI Insights",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (business?.isDemo == true) {
                            "Your sales are up 18% this week compared to last week! **Butter Croissant** is selling faster than usual and may run out in 2 days. Also, **Amit Verma** has an overdue credit of ₹450 pending."
                        } else {
                            "Setup completed! Once you record transactions, BizPilot AI will analyze purchase speeds, cash-flows, and segments to generate intelligent opportunities."
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.ASK_PILOT) },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Discuss Actions", fontSize = 12.sp)
                    }
                }
            }
        }

        // Custom Sales Trend Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weekly Sales Trend",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sales trend custom columns representation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val mockWeeklySales = listOf(1400, 2200, 1800, 2900, 2400, 3100, todaySalesAmount.toInt())
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        val maxSale = (mockWeeklySales.maxOrNull() ?: 1).coerceAtLeast(1)
                        
                        mockWeeklySales.forEachIndexed { idx, amt ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                val barHeightPercentage = amt.toFloat() / maxSale
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .fillMaxHeight(barHeightPercentage.coerceIn(0.1f, 1f))
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (idx == 6) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = days[idx], fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Smart Alerts Center (Unread Alerts)
        val unreadAlerts = alerts.filter { !it.isRead }
        if (unreadAlerts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Alerts (${unreadAlerts.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Mark all read",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.markAllAlertsRead() }
                    )
                }
            }

            items(unreadAlerts) { alert ->
                AlertItemRow(alert = alert, onDismiss = { viewModel.dismissAlert(alert.id) })
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    compareText: String,
    isPositive: Boolean?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPositive != null) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = compareText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FastActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AlertItemRow(
    alert: Alert,
    onDismiss: () -> Unit
) {
    val containerColor = when (alert.type) {
        "Critical" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        "Warning" -> Color(0xFFFF9800).copy(alpha = 0.12f)
        "Opportunity" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    }

    val iconColor = when (alert.type) {
        "Critical" -> MaterialTheme.colorScheme.error
        "Warning" -> Color(0xFFE65100)
        "Opportunity" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    val icon = when (alert.type) {
        "Critical" -> Icons.Default.Error
        "Warning" -> Icons.Default.Warning
        "Opportunity" -> Icons.Default.Lightbulb
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alert.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun getStartOfDay(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
