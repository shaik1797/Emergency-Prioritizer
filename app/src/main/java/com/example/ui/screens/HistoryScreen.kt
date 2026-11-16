package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RequestStatus
import com.example.ui.EmergencyViewModel
import com.example.ui.theme.*

@Composable
fun HistoryScreen(
    viewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.historyList.collectAsState()
    var filterMode by remember { mutableStateOf("ALL") } // ALL, COMPLETED, CANCELLED

    val filtered = remember(history, filterMode) {
        when (filterMode) {
            "COMPLETED" -> history.filter { it.status == RequestStatus.COMPLETED }
            "CANCELLED" -> history.filter { it.status == RequestStatus.CANCELLED }
            else -> history
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp)
    ) {
        Text(
            text = "Request Resolution History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Comprehensive audit log of all completed dispatches and cancelled requests.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterMode == "ALL",
                onClick = { filterMode = "ALL" },
                label = { Text("All (${history.size})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterMode == "COMPLETED",
                onClick = { filterMode = "COMPLETED" },
                label = { Text("Completed (${history.count { it.status == RequestStatus.COMPLETED }})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterMode == "CANCELLED",
                onClick = { filterMode = "CANCELLED" },
                label = { Text("Cancelled (${history.count { it.status == RequestStatus.CANCELLED }})", fontSize = 11.sp) }
            )
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history records matching '$filterMode'.",
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 70.dp)
            ) {
                items(filtered, key = { it.id + it.resolutionTime }) { req ->
                    val isCompleted = req.status == RequestStatus.COMPLETED
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isCompleted) SeverityLowBorder.copy(alpha = 0.4f) else SeverityCriticalBorder.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isCompleted) SeverityLow else SeverityCritical,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${req.id} • ${req.serviceType}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (isCompleted) "COMPLETED" else "CANCELLED",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isCompleted) SeverityLow else SeverityCritical
                                    )
                                }
                                Text(
                                    text = "User: ${req.callerName} [${req.userId}] • Location: ${req.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                if (req.assignedPersonnel != "Unassigned") {
                                    Text(
                                        text = "Responder: ${req.assignedPersonnel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64B5F6),
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "Logged: ${req.timestamp} • Closed: ${req.resolutionTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
