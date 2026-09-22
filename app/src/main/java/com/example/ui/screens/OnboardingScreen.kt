package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.BizPilotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: BizPilotViewModel,
    modifier: Modifier = Modifier
) {
    val step by viewModel.onboardingStep.collectAsState()
    
    // Step 1 states: Business Info
    var bizName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Step 2 states: Business Category
    val categories = listOf(
        "Retail Store", "Grocery Store", "Restaurant", "Cafe", 
        "Clothing Store", "Ecommerce Seller", "Wholesaler", 
        "Salon", "Repair Shop", "Service Business", "Custom Business"
    )
    var selectedCategory by remember { mutableStateOf("Cafe") }

    // Step 3 states: Details
    var country by remember { mutableStateOf("India") }
    var currency by remember { mutableStateOf("INR") }
    var openingHours by remember { mutableStateOf("9:00 AM - 9:00 PM") }
    var numEmployees by remember { mutableStateOf("2") }
    var salesRange by remember { mutableStateOf("₹50,000 - ₹1,00,000") }

    // Step 4 states: AI Prefs
    var aiInstructions by remember { mutableStateOf("Always suggest low-stock items first, use warm Hinglish, and keep customer messages short and professional.") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Setup Your BizPilot", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    if (step > 1) {
                        IconButton(onClick = { viewModel.setOnboardingStep(step - 1) }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..4) {
                    val isCompletedOrCurrent = i <= step
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isCompletedOrCurrent) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            // Screen content dependent on steps
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (step) {
                    1 -> StepBusinessInfo(
                        name = bizName, onNameChange = { bizName = it },
                        owner = ownerName, onOwnerChange = { ownerName = it },
                        ph = phone, onPhChange = { phone = it },
                        addr = address, onAddrChange = { address = it }
                    )
                    2 -> StepCategorySelection(
                        categories = categories,
                        selected = selectedCategory,
                        onSelect = { selectedCategory = it }
                    )
                    3 -> StepBusinessDefaults(
                        country = country, onCountryChange = { country = it },
                        currency = currency, onCurrencyChange = { currency = it },
                        hours = openingHours, onHoursChange = { openingHours = it },
                        employees = numEmployees, onEmployeesChange = { numEmployees = it },
                        sales = salesRange, onSalesChange = { salesRange = it }
                    )
                    4 -> StepAiPreferences(
                        instructions = aiInstructions,
                        onInstructionsChange = { aiInstructions = it }
                    )
                }
            }

            // Bottom Buttons
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (step < 4) {
                        // Validation
                        if (step == 1 && (bizName.isEmpty() || ownerName.isEmpty())) {
                            viewModel.showToast("Please enter Business and Owner name.")
                        } else {
                            viewModel.setOnboardingStep(step + 1)
                        }
                    } else {
                        // Complete onboarding
                        viewModel.completeOnboarding(
                            name = bizName,
                            owner = ownerName,
                            category = selectedCategory,
                            country = country,
                            currency = currency,
                            phone = phone,
                            address = address,
                            hours = openingHours,
                            employees = numEmployees.toIntOrNull() ?: 1,
                            salesRange = salesRange,
                            aiInstructions = aiInstructions
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (step == 4) "Deploy Your BizPilot 🚀" else "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StepBusinessInfo(
    name: String, onNameChange: (String) -> Unit,
    owner: String, onOwnerChange: (String) -> Unit,
    ph: String, onPhChange: (String) -> Unit,
    addr: String, onAddrChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tell us about your Business",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "This details help BizPilot personalize your local dashboard ledger and AI memory.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Business Name") },
            placeholder = { Text("e.g. Chai Tapri Café") },
            modifier = Modifier.fillMaxWidth().testTag("biz_name_input"),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) }
        )

        OutlinedTextField(
            value = owner,
            onValueChange = onOwnerChange,
            label = { Text("Owner Name") },
            placeholder = { Text("e.g. Rajesh Kumar") },
            modifier = Modifier.fillMaxWidth().testTag("owner_name_input"),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )

        OutlinedTextField(
            value = ph,
            onValueChange = onPhChange,
            label = { Text("Business Phone Number") },
            placeholder = { Text("e.g. +91 98765 43210") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
        )

        OutlinedTextField(
            value = addr,
            onValueChange = onAddrChange,
            label = { Text("Business Address") },
            placeholder = { Text("e.g. G-14, Sector 62, Noida") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
        )
    }
}

@Composable
fun StepCategorySelection(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Select your business category",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This configures tailored alerts, suggestions, and reorder patterns.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { cat ->
                val isSelected = cat == selected
                Card(
                    onClick = { onSelect(cat) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("category_card_$cat"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = CardDefaults.outlinedCardBorder(isSelected)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    ) {
                        Text(
                            text = cat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepBusinessDefaults(
    country: String, onCountryChange: (String) -> Unit,
    currency: String, onCurrencyChange: (String) -> Unit,
    hours: String, onHoursChange: (String) -> Unit,
    employees: String, onEmployeesChange: (String) -> Unit,
    sales: String, onSalesChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Operating Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Setup default values for billing invoices, currency conversion, and operating hours.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = country,
                onValueChange = onCountryChange,
                label = { Text("Country") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = currency,
                onValueChange = onCurrencyChange,
                label = { Text("Currency") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = hours,
            onValueChange = onHoursChange,
            label = { Text("Opening Hours") },
            placeholder = { Text("e.g. 9:00 AM - 9:00 PM") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) }
        )

        OutlinedTextField(
            value = employees,
            onValueChange = onEmployeesChange,
            label = { Text("Number of Employees") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.People, contentDescription = null) }
        )

        OutlinedTextField(
            value = sales,
            onValueChange = onSalesChange,
            label = { Text("Monthly Sales Range") },
            placeholder = { Text("e.g. ₹50k - ₹1 Lakh") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) }
        )
    }
}

@Composable
fun StepAiPreferences(
    instructions: String,
    onInstructionsChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configure Business Memory",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Teach BizPilot operating rules, message tones, or preferred languages. The AI references this context in future suggestions.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = instructions,
            onValueChange = onInstructionsChange,
            label = { Text("AI Rules & Instructions") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            maxLines = 10
        )
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.infoContainerView()
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tip: You can change these instructions, update your profile details, or provide a Gemini API key at any time in settings.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// Helper color extension
@Composable
fun ColorScheme.infoContainerView(): Color {
    return this.primaryContainer.copy(alpha = 0.2f)
}
