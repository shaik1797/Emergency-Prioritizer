package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.NotificationType
import com.example.ui.EmergencyViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppScreen(val label: String, val icon: ImageVector) {
    QUEUE("Queue", Icons.Default.FormatListNumbered),
    INTAKE("Registration", Icons.Default.PersonAdd),
    VISUALIZER("Visualizer", Icons.Default.AccountTree),
    HISTORY("History", Icons.Default.History),
    ANALYSIS("Analysis", Icons.Default.BarChart)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                EmergencyApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyApp(
    viewModel: EmergencyViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.QUEUE) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = tween(durationMillis = 500),
        finishedListener = { isRefreshing = false },
        label = "refresh_rotation"
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissMessage()
        }
    }

    // Android back navigation: return to Queue if on another screen
    BackHandler(enabled = currentScreen != AppScreen.QUEUE) {
        currentScreen = AppScreen.QUEUE
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.pro_dispatch_logo_1793723196427),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, EmergencyRedPrimary.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Emergency Prioritizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SeverityLow)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE DISPATCH • MAX-HEAP CAD",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SeverityLow
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Feature 9: Notification Bell with Badge
                    IconButton(
                        onClick = { showNotificationsDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("notifications_action")
                    ) {
                        BadgedBox(
                            badge = {
                                if (notifications.isNotEmpty()) {
                                    Badge(containerColor = EmergencyRedPrimary) {
                                        Text("${notifications.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = TextSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Genuine Refresh Action (re-syncs queue, waiting time, and checks heap property)
                    IconButton(
                        onClick = {
                            isRefreshing = true
                            viewModel.refreshQueue()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refresh_queue_action")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh Queue",
                            tint = if (isRefreshing) EmergencyRedPrimary else TextSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(rotationAngle)
                        )
                    }

                    // Overflow Menu for Competition Features, Invariant Checking & Demo Scenarios
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("more_menu_action")
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            modifier = Modifier.background(DarkSurfaceCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Load Competition Scenarios (10 Calls)", color = TextPrimary, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = SeverityHigh, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.loadDemoData()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Verify Max-Heap Invariant (O(n))", color = TextPrimary, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeverityLow, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.verifyHeap()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Run Micro-Benchmark (200 Ops)", color = TextPrimary, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.runMicroBenchmark()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("CSV Persistence & Backup", color = TextPrimary, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMoreMenu = false
                                    showCsvDialog = true
                                }
                            )
                            Divider(color = DarkCardBorder)
                            DropdownMenuItem(
                                text = { Text("Clear All Pending Calls", color = SeverityCritical, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = SeverityCritical, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.cancelAllPending()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About & DSA Architecture", color = TextSecondary, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMoreMenu = false
                                    showAboutDialog = true
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                AppScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                screen.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmergencyRedPrimary,
                            selectedTextColor = EmergencyRedPrimary,
                            indicatorColor = Color(0xFF321A1D),
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        ),
                        modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(140)) togetherWith fadeOut(animationSpec = tween(90))
                },
                label = "screen_transition"
            ) { target ->
                when (target) {
                    AppScreen.QUEUE -> QueueScreen(
                        viewModel = viewModel,
                        onNavigateToRegistration = { currentScreen = AppScreen.INTAKE }
                    )
                    AppScreen.INTAKE -> IntakeScreen(
                        viewModel = viewModel,
                        onSuccessNavToQueue = { currentScreen = AppScreen.QUEUE }
                    )
                    AppScreen.VISUALIZER -> VisualizerScreen(
                        viewModel = viewModel
                    )
                    AppScreen.HISTORY -> HistoryScreen(
                        viewModel = viewModel
                    )
                    AppScreen.ANALYSIS -> AnalysisScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Feature 9: Notifications Feed Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Emergency Notifications Feed")
                }
            },
            text = {
                if (notifications.isEmpty()) {
                    Text("No dispatch notifications yet.", color = TextTertiary)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            val iconTint = when (notif.type) {
                                NotificationType.CRITICAL_ALERT -> SeverityCritical
                                NotificationType.ASSIGNMENT -> Color(0xFF64B5F6)
                                NotificationType.STATUS_UPDATE -> SeverityLow
                                NotificationType.CANCELLATION -> Color(0xFFFFA726)
                            }
                            Surface(
                                color = DarkSurfaceCard,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, iconTint.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (notif.type) {
                                            NotificationType.CRITICAL_ALERT -> Icons.Default.Warning
                                            NotificationType.ASSIGNMENT -> Icons.Default.PersonPin
                                            NotificationType.STATUS_UPDATE -> Icons.Default.CheckCircle
                                            NotificationType.CANCELLATION -> Icons.Default.Cancel
                                        },
                                        contentDescription = null,
                                        tint = iconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(notif.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                                            Text(notif.timestamp, color = TextTertiary, fontSize = 10.sp)
                                        }
                                        Text(notif.message, color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // About Project Modal Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = DarkSurfaceCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Emergency Service Prioritizer") },
            text = {
                Column {
                    Text(
                        text = "Real-Time Emergency Dispatch & Triage Engine",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Divider(color = DarkCardBorder, modifier = Modifier.padding(vertical = 6.dp))
                    Text(
                        text = "• Core Structure: Hand-crafted Binary Max-Heap with 0-based vector indexing.\n" +
                               "• Triage Rule: Severity 4 (Critical) down to 1 (Low) with FIFO tie-breaking on arrival sequence numbers.\n" +
                               "• Direct Hash Lookup: O(1) ID index map allows dynamic severity update & cancellation in O(log n).\n" +
                               "• Features: User Registration, Location Coordinates, Waiting Time Tracker, Duplicate Detection, Personnel Assignment & Notifications.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // CSV Persistence & File Export/Import Dialog
    if (showCsvDialog) {
        val context = LocalContext.current
        var importInputText by remember { mutableStateOf("") }
        var activeTab by remember { mutableIntStateOf(0) } // 0: Export, 1: Import
        val csvData = remember(showCsvDialog) { viewModel.exportToCsv() }

        AlertDialog(
            onDismissRequest = { showCsvDialog = false },
            containerColor = DarkSurfaceCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CSV Data Persistence", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = DarkSurface,
                        contentColor = EmergencyRedPrimary
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("Export CSV", fontSize = 12.sp) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("Import CSV", fontSize = 12.sp) }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (activeTab == 0) {
                        Text("Active & resolved requests formatted as standard CSV:", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFF141418),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            LazyColumn(modifier = Modifier.padding(8.dp)) {
                                item {
                                    Text(
                                        text = csvData,
                                        color = Color(0xFFB0BEC5),
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Emergency_Data.csv", csvData)
                                    clipboard.setPrimaryClip(clip)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C38)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy CSV", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_SUBJECT, "Emergency_Requests.csv")
                                        putExtra(Intent.EXTRA_TEXT, csvData)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share CSV Data"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 11.sp)
                            }
                        }
                    } else {
                        Text("Paste CSV data lines (with standard emergency headers):", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = importInputText,
                            onValueChange = { importInputText = it },
                            placeholder = { Text("ID,UserID,Name,Phone,Address,Location,GPS,Service,Severity...", fontSize = 10.sp, color = TextTertiary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val (count, _) = viewModel.importFromCsv(importInputText)
                                if (count > 0) {
                                    showCsvDialog = false
                                }
                            },
                            enabled = importInputText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SeverityLow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Load CSV into Heap", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCsvDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }
}
