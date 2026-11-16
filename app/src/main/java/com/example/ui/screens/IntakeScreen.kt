package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EmergencyViewModel
import com.example.ui.components.AmbulanceConfirmationDialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntakeScreen(
    viewModel: EmergencyViewModel,
    onSuccessNavToQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var userName by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("USR-${(1000..9999).random()}") }
    var phone by remember { mutableStateOf("") }
    var userAddress by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var gpsCoordinates by remember { mutableStateOf("17.3850° N, 78.4867° E") }
    var serviceType by remember { mutableStateOf("Medical Emergency (Ambulance)") }
    var severity by remember { mutableIntStateOf(3) }
    var autoDetectedSeverity by remember { mutableIntStateOf(3) }
    var isManualSeverityOverride by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var serviceExpanded by remember { mutableStateOf(false) }
    var showAmbulanceConfirmDialog by remember { mutableStateOf(false) }

    val serviceOptions = listOf(
        "Medical Emergency (Ambulance)",
        "Fire Outbreak",
        "Electricity Failure / Grid Hazard",
        "Water Leakage / Pipe Burst",
        "Police / Crime in Progress",
        "Disaster Relief",
        "Gas Leak / Chemical Hazard",
        "Other Municipal Emergency"
    )

    // Feature 1: Automatic Priority Detection whenever service type or description updates
    LaunchedEffect(serviceType, description) {
        val detected = viewModel.detectPriority(serviceType, description)
        autoDetectedSeverity = detected
        if (!isManualSeverityOverride) {
            severity = detected
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Updated Title as requested: User Registration
        Text(
            text = "User Registration",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Register user identity and submit high-priority emergency service request.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Quick Incident Presets
        Text(
            text = "Quick Presets:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextTertiary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SuggestionChip(
                onClick = {
                    userName = "Dr. Sameer"
                    phone = "9876543210"
                    userAddress = "Quarter 14, Doctors Colony"
                    location = "Central Metro Station Gate 2"
                    serviceType = "Medical Emergency (Ambulance)"
                    description = "Cardiac arrest with loss of consciousness."
                    isManualSeverityOverride = false
                },
                label = { Text("🚑 Cardiac (S4)", fontSize = 11.sp) }
            )
            SuggestionChip(
                onClick = {
                    userName = "Ravi Kumar"
                    phone = "9848011223"
                    userAddress = "Sec-4, Power Colony"
                    location = "Power Grid Substation 3"
                    serviceType = "Electricity Failure / Grid Hazard"
                    description = "High-voltage transmission wire snapped on road, active sparks!"
                    isManualSeverityOverride = false
                },
                label = { Text("⚡ Wire Snap (S4)", fontSize = 11.sp) }
            )
            SuggestionChip(
                onClick = {
                    userName = "Security Desk"
                    phone = "9123455667"
                    userAddress = "Commercial Complex Fl 1"
                    location = "Commercial Complex Fl 4"
                    serviceType = "Fire Outbreak"
                    description = "Electrical transformer spark caught textile warehouse."
                    isManualSeverityOverride = false
                },
                label = { Text("🔥 Building Fire (S4)", fontSize = 11.sp) }
            )
            SuggestionChip(
                onClick = {
                    userName = "Ward Councillor"
                    phone = "9988771122"
                    userAddress = "Town Hall Road"
                    location = "Market Row Crossroad 12"
                    serviceType = "Water Leakage / Pipe Burst"
                    description = "Major freshwater main pipe burst, flooding roadway."
                    isManualSeverityOverride = false
                },
                label = { Text("💧 Pipe Burst (S3)", fontSize = 11.sp) }
            )
            SuggestionChip(
                onClick = {
                    userName = "Store Cashier"
                    phone = "9445566778"
                    userAddress = "Old City Lane"
                    location = "Jewellery Bazaar Lane 3"
                    serviceType = "Police / Crime in Progress"
                    description = "Armed robbery in progress, suspects armed."
                    isManualSeverityOverride = false
                },
                label = { Text("🚓 Armed Crime (S4)", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 1: User Registration Details
        Text(
            text = "1. User Registration Profile",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64B5F6)
        )
        Spacer(modifier = Modifier.height(6.dp))

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceCard,
            unfocusedContainerColor = DarkSurfaceCard,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = EmergencyRedPrimary,
            unfocusedBorderColor = DarkCardBorder,
            focusedLabelColor = EmergencyRedPrimary,
            unfocusedLabelColor = TextSecondary,
            focusedLeadingIconColor = EmergencyRedPrimary,
            unfocusedLeadingIconColor = TextSecondary
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("User / Caller Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                colors = textFieldColors,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("input_caller_name")
            )

            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                colors = textFieldColors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
            label = { Text("10-Digit Mobile Number *") },
            supportingText = { Text("${phone.length}/10 digits", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_phone")
        )

        OutlinedTextField(
            value = userAddress,
            onValueChange = { userAddress = it },
            label = { Text("User Residential / Permanent Address") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Service Request & Location
        Text(
            text = "2. Emergency Incident & Location",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64B5F6)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Location with GPS Pin button
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Incident Location / Landmark *") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                TextButton(onClick = {
                    location = "Ring Road Junction Pillar 104"
                    gpsCoordinates = "17.3916° N, 78.4419° E"
                }) {
                    Text("📍 GPS", fontSize = 11.sp, color = EmergencyRedPrimary)
                }
            },
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_location")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Service Type Dropdown
        ExposedDropdownMenuBox(
            expanded = serviceExpanded,
            onExpandedChange = { serviceExpanded = !serviceExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = serviceType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Emergency Service Category *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                colors = textFieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = serviceExpanded,
                onDismissRequest = { serviceExpanded = false },
                modifier = Modifier.background(DarkSurfaceCard)
            ) {
                serviceOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = TextPrimary) },
                        onClick = {
                            serviceType = option
                            serviceExpanded = false
                            isManualSeverityOverride = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Situation Details / First Responder Instructions") },
            maxLines = 3,
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Section 3: Automatic Priority Detection & Severity Classification
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3. Severity Classification",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64B5F6)
            )

            // Feature 1: Automatic Priority Tag
            Surface(
                color = if (severity == 4) SeverityCriticalBg else Color(0xFF1E2838),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, if (severity == 4) SeverityCriticalBorder else Color(0xFF385070))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = if (severity == 4) SeverityCritical else Color(0xFF64B5F6), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Auto-Priority: Level $autoDetectedSeverity",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (severity == 4) SeverityCritical else Color(0xFF90CAF9)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            4 to Triple("Critical (Level 4)", "Cardiac arrest, active fire, live electric wire, chemical/gas leak", SeverityCritical),
            3 to Triple("High (Level 3)", "Severe injury/fractures, building fire, main pipe flood, armed crime", SeverityHigh),
            2 to Triple("Medium (Level 2)", "Minor injuries, local electricity blackout, pipeline leak, dispute", SeverityModerate),
            1 to Triple("Low (Level 1)", "Routine transfer, water tap overflow, non-hazardous municipal aid", SeverityLow)
        ).forEach { (lvl, info) ->
            val isSelected = severity == lvl
            Surface(
                onClick = {
                    severity = lvl
                    isManualSeverityOverride = true
                },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) info.third.copy(alpha = 0.18f) else DarkSurfaceCard,
                border = BorderStroke(
                    width = if (isSelected) 1.8.dp else 1.dp,
                    color = if (isSelected) info.third else DarkCardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            severity = lvl
                            isManualSeverityOverride = true
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = info.third)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = info.first,
                            fontWeight = FontWeight.Bold,
                            color = info.third,
                            fontSize = 13.sp
                        )
                        Text(
                            text = info.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        fun resetForm() {
            userName = ""
            phone = ""
            userAddress = ""
            location = ""
            description = ""
            userId = "USR-${(1000..9999).random()}"
        }

        // Submit Button with Ambulance Confirmation Trigger
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (userName.isBlank() || location.isBlank() || !phone.matches(Regex("^[0-9]{10}$"))) {
                    // Let viewModel show validation errors
                    viewModel.registerAndSubmitRequest(
                        callerName = userName,
                        userId = userId,
                        phone = phone,
                        userAddress = userAddress,
                        location = location,
                        gpsCoordinates = gpsCoordinates,
                        serviceType = serviceType,
                        severity = severity,
                        description = description
                    )
                    return@Button
                }

                val isAmbulance = serviceType.contains("Ambulance", ignoreCase = true) || serviceType.contains("Medical", ignoreCase = true)
                val descLower = description.lowercase()
                val isAlreadyEvidentCritical = severity == 4 && (descLower.contains("cardiac") || descLower.contains("unconscious") || descLower.contains("bleeding heavily"))

                if (isAmbulance && !isAlreadyEvidentCritical) {
                    showAmbulanceConfirmDialog = true
                } else {
                    val ok = viewModel.registerAndSubmitRequest(
                        callerName = userName,
                        userId = userId,
                        phone = phone,
                        userAddress = userAddress,
                        location = location,
                        gpsCoordinates = gpsCoordinates,
                        serviceType = serviceType,
                        severity = severity,
                        description = description
                    )
                    if (ok) {
                        resetForm()
                        onSuccessNavToQueue()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_request_button")
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register User & Submit Request (O(log n))", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        // Ambulance Clinical Screening Confirmation Dialog
        if (showAmbulanceConfirmDialog) {
            AmbulanceConfirmationDialog(
                callerName = userName.ifBlank { "Registered Caller" },
                phoneNumber = phone.ifBlank { "Unspecified" },
                location = location.ifBlank { "Incident Location" },
                gpsCoordinates = gpsCoordinates,
                onConfirmCritical = { symptoms ->
                    val symptomNote = if (symptoms.isNotEmpty()) " [Screening: ${symptoms.joinToString(", ")}]" else ""
                    val ok = viewModel.registerAndSubmitRequest(
                        callerName = userName,
                        userId = userId,
                        phone = phone,
                        userAddress = userAddress,
                        location = location,
                        gpsCoordinates = gpsCoordinates,
                        serviceType = serviceType,
                        severity = 4, // Upgraded to critical level!
                        description = "$description$symptomNote",
                        isDispatcherReviewRequired = false,
                        screeningSymptoms = symptoms
                    )
                    if (ok) {
                        showAmbulanceConfirmDialog = false
                        resetForm()
                        onSuccessNavToQueue()
                    }
                },
                onContinueNonCritical = { symptoms ->
                    val symptomNote = if (symptoms.isNotEmpty()) " [Screening: ${symptoms.joinToString(", ")}]" else ""
                    val ok = viewModel.registerAndSubmitRequest(
                        callerName = userName,
                        userId = userId,
                        phone = phone,
                        userAddress = userAddress,
                        location = location,
                        gpsCoordinates = gpsCoordinates,
                        serviceType = serviceType,
                        severity = if (severity == 4) 2 else severity, // Non-critical transport level
                        description = "$description$symptomNote",
                        isDispatcherReviewRequired = true, // Flagged for dispatcher review!
                        screeningSymptoms = symptoms
                    )
                    if (ok) {
                        showAmbulanceConfirmDialog = false
                        resetForm()
                        onSuccessNavToQueue()
                    }
                },
                onDismiss = { showAmbulanceConfirmDialog = false }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                viewModel.loadDemoData()
                onSuccessNavToQueue()
            },
            border = BorderStroke(1.dp, DarkCardBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Load Sample Incident Requests (All 4 Severities)")
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
