package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EmergencyViewModel
import com.example.ui.theme.*

@Composable
fun AnalysisScreen(
    viewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.metrics.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "DSA Complexity Analysis",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Real-Time Max-Heap Performance & Triage Analytics",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Live Telemetry Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = BorderStroke(1.dp, Color(0xFF2E7D32))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Live Heap Telemetry",
                    fontWeight = FontWeight.Bold,
                    color = SeverityLow,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Last Operation", fontSize = 11.sp, color = TextTertiary)
                        Text(metrics.lastOperation, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                    }
                    Column {
                        Text("Comparisons", fontSize = 11.sp, color = TextTertiary)
                        Text("${metrics.comparisons}", fontWeight = FontWeight.Bold, color = SeverityHigh, fontSize = 13.sp)
                    }
                    Column {
                        Text("Swaps", fontSize = 11.sp, color = TextTertiary)
                        Text("${metrics.swaps}", fontWeight = FontWeight.Bold, color = SeverityCritical, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = DarkCardBorder)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Pending Calls (n): ${metrics.heapSize}", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextPrimary)
                    Text("Tree Height (h): ${metrics.treeHeight}", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time and Space Complexity Table Card
        Text(
            text = "Asymptotic Complexity Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = BorderStroke(1.dp, DarkCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Operation", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1.8f))
                    Text("Best", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                    Text("Avg / Worst", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1.2f))
                    Text("Space", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                }
                Divider(color = DarkCardBorder)

                val complexities = listOf(
                    Quad("Insert (sift-up)", "O(1)", "O(log n)", "O(1)"),
                    Quad("Extract-Max", "O(1)", "O(log n)", "O(1)"),
                    Quad("Peek Root", "O(1)", "O(1)", "O(1)"),
                    Quad("Change Severity", "O(1)", "O(log n)", "O(1)"),
                    Quad("Cancel by ID", "O(1)", "O(log n)", "O(1)"),
                    Quad("Build-Heap O(n)", "O(n)", "O(n)", "O(1)"),
                    Quad("Heap Sort View", "O(n log n)", "O(n log n)", "O(n)"),
                    Quad("Search (Scan)", "O(1)", "O(n)", "O(1)")
                )

                complexities.forEach { (op, best, avg, space) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(op, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1.8f))
                        Text(best, fontSize = 11.sp, color = SeverityLow, modifier = Modifier.weight(1f))
                        Text(avg, fontSize = 11.sp, color = EmergencyRedPrimary, modifier = Modifier.weight(1.2f))
                        Text(space, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                    }
                    Divider(color = DarkCardBorder.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theory Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10192A)),
            border = BorderStroke(1.dp, Color(0xFF1E3A5F))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Why Binary Max-Heap for Emergency Prioritization?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64B5F6),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Array/List requires linear O(n) scan to extract the top-urgency call.\n" +
                           "• Sorted List takes linear O(n) element shifts upon each new call arrival.\n" +
                           "• Binary Max-Heap balances both operations in O(log n) time, storing nodes in contiguous memory with zero pointer fragmentation.\n" +
                           "• An auxiliary Hash Map enables O(log n) dynamic severity adjustments and cancellations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF90CAF9)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Competition Live Benchmark & Heap Verification Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = BorderStroke(1.dp, Color(0xFF64B5F6))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-Time DSA Benchmark Engine",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64B5F6),
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Runs 200 concurrent insertions followed by 200 extract-max operations on synthetic emergency requests, profiling execution latency down to nanoseconds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.runMicroBenchmark() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Run 200-Op Benchmark", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { viewModel.verifyHeap() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Validate Invariant", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

private data class Quad(val first: String, val second: String, val third: String, val fourth: String)
