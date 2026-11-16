package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmergencyRequest
import com.example.model.RequestStatus
import com.example.ui.EmergencyViewModel
import com.example.ui.Personnel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

data class QuadColor(val bg: Color, val text: String, val color: Color, val border: Color)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    viewModel: EmergencyViewModel,
    onNavigateToRegistration: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val sortedRequests by viewModel.sortedRequests.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val criticalAlert by viewModel.criticalAlert.collectAsState()
    val duplicateWarning by viewModel.duplicateWarning.collectAsState()

    var filterCategory by remember { mutableStateOf("ALL") } // ALL, CRITICAL, HIGH, MEDIUM, LOW, ASSIGNED, DELAYED
    var selectedForSeverityChange by remember { mutableStateOf<EmergencyRequest?>(null) }
    var selectedForPersonnelAssign by remember { mutableStateOf<EmergencyRequest?>(null) }
    var selectedForReview by remember { mutableStateOf<EmergencyRequest?>(null) }
    var selectedForCancel by remember { mutableStateOf<EmergencyRequest?>(null) }
    var cancelReason by remember { mutableStateOf("Resolved locally by bystanders") }
    var showCancelAllDialog by remember { mutableStateOf(false) }

    // Live clock ticker for Feature 3: Waiting Time Calculation
    var currentClockTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentClockTime = System.currentTimeMillis()
        }
    }

    val filteredList = remember(sortedRequests, searchQuery, filterCategory, currentClockTime) {
        val base = if (searchQuery.isBlank()) sortedRequests
        else {
            val q = searchQuery.trim().lowercase()
            sortedRequests.filter {
                it.id.lowercase().contains(q) ||
                it.callerName.lowercase().contains(q) ||
                it.location.lowercase().contains(q) ||
                it.serviceType.lowercase().contains(q) ||
                it.phoneNumber.contains(q) ||
                it.assignedPersonnel.lowercase().contains(q)
            }
        }
        when (filterCategory) {
            "CRITICAL" -> base.filter { it.severity == 4 }
            "HIGH" -> base.filter { it.severity == 3 }
            "MEDIUM" -> base.filter { it.severity == 2 }
            "LOW" -> base.filter { it.severity == 1 }
            "REVIEW" -> base.filter { it.isDispatcherReviewRequired }
            "ASSIGNED" -> base.filter { it.status == RequestStatus.ASSIGNED }
            "DELAYED" -> base.filter { (currentClockTime - it.creationEpoch) > 180000 }
            else -> base
        }
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Feature 6: Emergency KPI Dashboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, DarkCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EMERGENCY OPERATIONS DASHBOARD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64B5F6)
                    )

                    Row {
                        if (sortedRequests.isNotEmpty()) {
                            IconButton(
                                onClick = { showCancelAllDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.DeleteSweep,
                                    contentDescription = "Cancel All",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    KpiStatCard("Total Calls", "${dashboardStats.totalRequests}", TextPrimary, Modifier.weight(1f))
                    KpiStatCard("Pending", "${dashboardStats.pendingRequests}", Color(0xFFFFA726), Modifier.weight(1f))
                    KpiStatCard("Critical", "${dashboardStats.criticalCount}", if (dashboardStats.criticalCount > 0) SeverityCritical else TextSecondary, Modifier.weight(1f))
                    KpiStatCard("Assigned", "${dashboardStats.assignedCount}", Color(0xFF42A5F5), Modifier.weight(1f))
                    KpiStatCard("Resolved", "${dashboardStats.completedCount}", SeverityLow, Modifier.weight(1f))
                }
            }
        }

        // Primary Dispatch Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = EmergencyRedContainer),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmergencyRedDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val top = sortedRequests.firstOrNull()
                    if (top != null) {
                        Text(
                            text = "Next in Queue: ${top.id} (${top.serviceType})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Priority Level: Severity ${top.severity} • Caller: ${top.callerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        Text(
                            text = "Queue Idle • All emergency calls processed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.dispatchNext()
                    },
                    enabled = sortedRequests.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmergencyRedPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF2C2C34),
                        disabledContentColor = Color(0xFF6E6E7A)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("dispatch_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Dispatch Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar with High Contrast
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search by ID, caller, location, service, unit...", color = TextTertiary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceCard,
                unfocusedContainerColor = DarkSurfaceCard,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = EmergencyRedPrimary,
                unfocusedBorderColor = DarkCardBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .testTag("search_field"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        // Filter Chips Row for fast mobile triage
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = filterCategory == "ALL",
                onClick = { filterCategory = "ALL" },
                label = { Text("All (${sortedRequests.size})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterCategory == "CRITICAL",
                onClick = { filterCategory = "CRITICAL" },
                label = { Text("🚨 Critical (${sortedRequests.count { it.severity == 4 }})", fontSize = 11.sp, color = SeverityCritical) }
            )
            FilterChip(
                selected = filterCategory == "HIGH",
                onClick = { filterCategory = "HIGH" },
                label = { Text("High (${sortedRequests.count { it.severity == 3 }})", fontSize = 11.sp, color = SeverityHigh) }
            )
            FilterChip(
                selected = filterCategory == "MEDIUM",
                onClick = { filterCategory = "MEDIUM" },
                label = { Text("Medium (${sortedRequests.count { it.severity == 2 }})", fontSize = 11.sp, color = SeverityModerate) }
            )
            FilterChip(
                selected = filterCategory == "LOW",
                onClick = { filterCategory = "LOW" },
                label = { Text("Low (${sortedRequests.count { it.severity == 1 }})", fontSize = 11.sp, color = SeverityLow) }
            )
            val reviewCount = sortedRequests.count { it.isDispatcherReviewRequired }
            if (reviewCount > 0) {
                FilterChip(
                    selected = filterCategory == "REVIEW",
                    onClick = { filterCategory = "REVIEW" },
                    label = { Text("🩺 Review ($reviewCount)", fontSize = 11.sp, color = Color(0xFFFFA726)) }
                )
            }
            FilterChip(
                selected = filterCategory == "ASSIGNED",
                onClick = { filterCategory = "ASSIGNED" },
                label = { Text("Assigned (${sortedRequests.count { it.status == RequestStatus.ASSIGNED }})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterCategory == "DELAYED",
                onClick = { filterCategory = "DELAYED" },
                label = { Text("⏱️ Delayed", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = "Empty Queue",
                            modifier = Modifier.size(38.dp),
                            tint = Color(0xFF64B5F6)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Priority Queue Is Standby" else "No matching requests found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) 
                                "All emergency requests have been dispatched, or queue has been cleared." 
                            else 
                                "Try clearing filters or search query.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (searchQuery.isEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.loadDemoData() },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Load 10 Scenarios", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onNavigateToRegistration,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Intake Call", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                itemsIndexed(filteredList, key = { _, item -> item.id }) { index, request ->
                    AdvancedRequestCard(
                        rank = index + 1,
                        request = request,
                        currentClockTime = currentClockTime,
                        onChangeSeverity = { selectedForSeverityChange = request },
                        onAssignPersonnel = { selectedForPersonnelAssign = request },
                        onReviewRequest = { selectedForReview = request },
                        onUpdateStatus = { newStatus: RequestStatus -> viewModel.updateRequestStatus(request.id, newStatus) },
                        onCancel = { selectedForCancel = request },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }

    // Quick Action Extended FAB for mobile thumb reachability
    ExtendedFloatingActionButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateToRegistration()
        },
        icon = { Icon(Icons.Default.AddAlert, contentDescription = null, tint = Color.White) },
        text = { Text("Register Emergency", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) },
        containerColor = EmergencyRedPrimary,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 16.dp, end = 16.dp)
            .testTag("fab_register_emergency")
    )
}

    // Feature 2: Emergency Alert Dialog on Critical Request
    criticalAlert?.let { alertReq ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissCriticalAlert() },
            containerColor = Color(0xFF260D10),
            titleContentColor = SeverityCritical,
            textContentColor = TextPrimary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = SeverityCritical, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CRITICAL EMERGENCY ALERT!", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "A Level 4 CRITICAL incident has just been registered!",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• ID: ${alertReq.id} • ${alertReq.serviceType}", color = TextSecondary)
                    Text("• Location: ${alertReq.location}", color = TextSecondary)
                    Text("• Caller: ${alertReq.callerName} (${alertReq.phoneNumber})", color = TextSecondary)
                    Text("• Situation: ${alertReq.description}", color = Color(0xFFFFCDD2), fontWeight = FontWeight.Medium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dispatchNext("Rapid ICU Team")
                        viewModel.dismissCriticalAlert()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical)
                ) {
                    Text("Dispatch Immediately")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCriticalAlert() }) {
                    Text("Acknowledge", color = TextSecondary)
                }
            }
        )
    }

    // Feature 5: Duplicate Request Warning Dialog
    duplicateWarning?.let { (existing, incoming) ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDuplicateWarning() },
            containerColor = DarkSurface,
            titleContentColor = Color(0xFFFFA726),
            textContentColor = TextPrimary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFFFFA726), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Duplicate Request Detected")
                }
            },
            text = {
                Column {
                    Text(
                        text = "A pending request with phone ${incoming.phoneNumber} for '${incoming.serviceType}' is already queued as ${existing.id}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Existing Request: ${existing.id} (${existing.status.displayName})", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Location: ${existing.location}", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Would you like to escalate existing priority to Critical or force-add duplicate?", color = TextTertiary, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changeSeverity(existing.id, 4)
                        viewModel.dismissDuplicateWarning()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary)
                ) {
                    Text("Escalate to Critical")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.confirmAddDuplicate(incoming) }) {
                        Text("Force Add Duplicate", color = Color(0xFF64B5F6))
                    }
                    TextButton(onClick = { viewModel.dismissDuplicateWarning() }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        )
    }

    // Feature 8: Service Personnel Assignment Dialog
    selectedForPersonnelAssign?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedForPersonnelAssign = null },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Assign Responder to ${req.id}") },
            text = {
                Column {
                    Text("Select available service unit for ${req.serviceType}:", color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    viewModel.availablePersonnel.forEach { responder ->
                        Surface(
                            onClick = {
                                viewModel.assignPersonnel(req.id, responder.name)
                                selectedForPersonnelAssign = null
                            },
                            color = DarkSurfaceCard,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(responder.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    Text(responder.role, color = TextSecondary, fontSize = 11.sp)
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedForPersonnelAssign = null }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }

    // Change Severity Dialog
    selectedForSeverityChange?.let { req ->
        var tempSev by remember { mutableStateOf(req.severity) }
        AlertDialog(
            onDismissRequest = { selectedForSeverityChange = null },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Change Severity for ${req.id}") },
            text = {
                Column {
                    Text("Caller: ${req.callerName} • ${req.serviceType}", color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(
                        4 to "4 - Critical (Immediate Hazard)",
                        3 to "3 - High (Urgent Medical/Fire)",
                        2 to "2 - Medium (Property/Dispute)",
                        1 to "1 - Low (Routine Aid)"
                    ).forEach { (level, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (tempSev == level),
                                onClick = { tempSev = level },
                                colors = RadioButtonDefaults.colors(selectedColor = EmergencyRedPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changeSeverity(req.id, tempSev)
                        selectedForSeverityChange = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary)
                ) {
                    Text("Update Priority")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedForSeverityChange = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Feature 7: Cancel Request Confirmation with Reason
    selectedForCancel?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedForCancel = null },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Cancel Emergency Call?") },
            text = {
                Column {
                    Text("Select cancellation reason for ${req.id} (${req.callerName}):", color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "Resolved locally by bystanders",
                        "False alarm / accidental call",
                        "Patient reached hospital independently",
                        "Duplicate report"
                    ).forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            RadioButton(
                                selected = cancelReason == reason,
                                onClick = { cancelReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = EmergencyRedPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(reason, color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelRequest(req.id, cancelReason)
                        selectedForCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedForCancel = null }) {
                    Text("Keep Active", color = TextSecondary)
                }
            }
        )
    }

    // Cancel All Dialog
    if (showCancelAllDialog) {
        AlertDialog(
            onDismissRequest = { showCancelAllDialog = false },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Cancel All Pending Emergencies?") },
            text = { Text("This will cancel and clear all ${sortedRequests.size} active emergency requests from the priority queue and log them in History.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelAllPending()
                        showCancelAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancel All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelAllDialog = false }) {
                    Text("Back", color = TextSecondary)
                }
            }
        )
    }

    // Dispatcher Review Dialog for Flagged Ambulance Requests
    selectedForReview?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedForReview = null },
            containerColor = DarkSurfaceCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dispatcher Clinical Review", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Call ID: ${req.id} • ${req.serviceType}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Caller: ${req.callerName} (📞 ${req.phoneNumber})", color = TextSecondary, fontSize = 12.sp)
                    Text("Location: ${req.location}", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFF1B1B22),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("PATIENT CLINICAL SCREENING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                            Spacer(modifier = Modifier.height(4.dp))
                            if (req.screeningSymptoms.isEmpty()) {
                                Text("No symptoms recorded in checklist.", fontSize = 11.sp, color = TextTertiary)
                            } else {
                                req.screeningSymptoms.forEach { sym ->
                                    Text("• $sym", fontSize = 12.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Verify urgency and confirm priority level:",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.confirmDispatcherReview(req.id, 4)
                                selectedForReview = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Escalate S4", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.confirmDispatcherReview(req.id, 2)
                                selectedForReview = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C38)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm S2", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmDispatcherReview(req.id)
                        selectedForReview = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary)
                ) {
                    Text("Verify & Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedForReview = null }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun KpiStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF1B1B22),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF2C2C36)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 9.sp, color = TextSecondary)
        }
    }
}

@Composable
fun AdvancedRequestCard(
    rank: Int,
    request: EmergencyRequest,
    currentClockTime: Long,
    onChangeSeverity: () -> Unit,
    onAssignPersonnel: () -> Unit,
    onReviewRequest: () -> Unit = {},
    onUpdateStatus: (RequestStatus) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val (badgeBg, badgeText, badgeColor, borderColor) = when (request.severity) {
        4 -> QuadColor(SeverityCriticalBg, "CRITICAL", SeverityCritical, SeverityCriticalBorder)
        3 -> QuadColor(SeverityHighBg, "HIGH", SeverityHigh, SeverityHighBorder)
        2 -> QuadColor(SeverityModerateBg, "MEDIUM", SeverityModerate, SeverityModerateBorder)
        else -> QuadColor(SeverityLowBg, "LOW", SeverityLow, SeverityLowBorder)
    }

    // Critical Incident Pulsing Glow Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_card")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val cardBorder = if (request.severity == 4) {
        BorderStroke(1.8.dp, borderColor.copy(alpha = pulseAlpha))
    } else {
        BorderStroke(1.2.dp, borderColor)
    }

    // Feature 3: Live Waiting Time Calculation
    val elapsedSeconds = maxOf(0L, (currentClockTime - request.creationEpoch) / 1000)
    val waitTimeString = when {
        elapsedSeconds < 60 -> "${elapsedSeconds}s ago"
        elapsedSeconds < 3600 -> "${elapsedSeconds / 60}m ${elapsedSeconds % 60}s"
        else -> "${elapsedSeconds / 3600}h ${(elapsedSeconds % 3600) / 60}m"
    }
    val isDelayed = (request.severity == 4 && elapsedSeconds > 180) || elapsedSeconds > 600

    fun callPhone(num: String) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openNavigation(loc: String) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(loc)))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun shareTicket(req: EmergencyRequest) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val shareText = """
            🚨 EMERGENCY DISPATCH TICKET
            ID: ${req.id}
            Service: ${req.serviceType} (Severity ${req.severity})
            Caller: ${req.callerName} (${req.phoneNumber})
            Location: ${req.location}
            GPS: ${req.gpsCoordinates}
            Assigned Unit: ${req.assignedPersonnel}
            Status: ${req.status.displayName}
            Situation: ${req.description}
            Reported: ${req.timestamp}
            """.trimIndent()
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "EMERGENCY: ${req.id} - ${req.serviceType}")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Dispatch Ticket"))
        } catch (_: Exception) {}
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("request_item_${request.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = cardBorder
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            // Header Row: Rank, ID, Service, Severity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF32323D), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = request.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFF2E2E38),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = request.serviceType,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "$badgeText (S${request.severity})",
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User Info & Direct Click-to-Dial Mobile Number
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = "Phone",
                    modifier = Modifier
                        .size(15.dp)
                        .clickable { callPhone(request.phoneNumber) },
                    tint = Color(0xFF64B5F6)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${request.callerName} [${request.userId}] • ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = request.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64B5F6),
                    modifier = Modifier.clickable { callPhone(request.phoneNumber) }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Location & GPS with Click-to-Map
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openNavigation(request.location) }
                    .padding(vertical = 2.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Map", modifier = Modifier.size(15.dp), tint = EmergencyRedPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${request.location} (${request.gpsCoordinates})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = Color(0xFF321215),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "MAP",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmergencyRedPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dispatcher Review Flag for Ambulance Screening
            if (request.isDispatcherReviewRequired) {
                Surface(
                    color = Color(0xFF2E1908),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF9800)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("FLAG: DISPATCHER REVIEW REQUIRED", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFFF9800))
                                if (request.screeningSymptoms.isNotEmpty()) {
                                    Text("Screening: ${request.screeningSymptoms.joinToString(", ")}", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }

                        Button(
                            onClick = onReviewRequest,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Review", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Feature 7 & 8: Status & Assigned Personnel Bar
            Surface(
                color = Color(0xFF1B1B22),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF2C2C36)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF64B5F6))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Unit: ${request.assignedPersonnel}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (request.assignedPersonnel == "Unassigned") Color(0xFFFFA726) else Color(0xFF64B5F6)
                        )
                    }

                    // Feature 3: Waiting Time Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (isDelayed) SeverityCritical else TextTertiary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Wait: $waitTimeString",
                            fontSize = 11.sp,
                            fontWeight = if (isDelayed) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDelayed) SeverityCritical else TextSecondary
                        )
                    }

                    // Status Pill
                    Surface(
                        color = when (request.status) {
                            RequestStatus.PENDING -> Color(0xFF382400)
                            RequestStatus.REVIEWING -> Color(0xFF3E200C)
                            RequestStatus.ASSIGNED -> Color(0xFF0D253A)
                            RequestStatus.IN_PROGRESS -> Color(0xFF2E1A47)
                            RequestStatus.COMPLETED -> Color(0xFF0B2D19)
                            RequestStatus.CANCELLED -> Color(0xFF381214)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = request.status.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (request.status) {
                                RequestStatus.PENDING -> Color(0xFFFFA726)
                                RequestStatus.REVIEWING -> Color(0xFFFF9800)
                                RequestStatus.ASSIGNED -> Color(0xFF64B5F6)
                                RequestStatus.IN_PROGRESS -> Color(0xFFCE93D8)
                                RequestStatus.COMPLETED -> SeverityLow
                                RequestStatus.CANCELLED -> SeverityCritical
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Assign, Status Toggle, Severity, Cancel, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Assign Unit Button
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAssignPersonnel()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        border = BorderStroke(1.dp, Color(0xFF385070)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF90CAF9))
                    ) {
                        Icon(Icons.Default.PersonPin, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Assign", fontSize = 11.sp)
                    }

                    // Status Progression Button (4-stage medical pipeline: Request Received -> Dispatcher Reviewing -> Ambulance Assigned -> En Route)
                    when (request.status) {
                        RequestStatus.PENDING -> {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onUpdateStatus(RequestStatus.REVIEWING)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp),
                                border = BorderStroke(1.dp, Color(0xFFE65100)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF9800))
                            ) {
                                Text("Review", fontSize = 11.sp)
                            }
                        }
                        RequestStatus.REVIEWING -> {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onAssignPersonnel()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp),
                                border = BorderStroke(1.dp, Color(0xFF1976D2)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64B5F6))
                            ) {
                                Text("Assign Unit", fontSize = 11.sp)
                            }
                        }
                        RequestStatus.ASSIGNED -> {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onUpdateStatus(RequestStatus.IN_PROGRESS)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp),
                                border = BorderStroke(1.dp, Color(0xFF7B1FA2)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCE93D8))
                            ) {
                                Text("En Route", fontSize = 11.sp)
                            }
                        }
                        RequestStatus.IN_PROGRESS -> {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onUpdateStatus(RequestStatus.COMPLETED)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp),
                                border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SeverityLow)
                            ) {
                                Text("Complete", fontSize = 11.sp)
                            }
                        }
                        else -> {}
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { shareTicket(request) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Ticket",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onChangeSeverity()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        border = BorderStroke(1.dp, DarkCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Severity", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCancel()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        border = BorderStroke(1.dp, Color(0xFF6A1B1B)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80))
                    ) {
                        Text("Cancel", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
