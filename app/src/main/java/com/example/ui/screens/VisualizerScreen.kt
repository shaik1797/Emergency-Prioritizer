package com.example.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmergencyRequest
import com.example.ui.EmergencyViewModel
import com.example.ui.theme.*
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max

@Composable
fun VisualizerScreen(
    viewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    val rawNodes by viewModel.rawHeapNodes.collectAsState()
    val metrics by viewModel.metrics.collectAsState()

    var selectedNodeIndex by remember { mutableStateOf<Int?>(null) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    val selectedNode = remember(rawNodes, selectedNodeIndex) {
        selectedNodeIndex?.let { idx ->
            if (idx in rawNodes.indices) rawNodes[idx] else null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with live stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Heap Tree Architecture",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Interactive Binary Max-Heap visualizer. Tap any node to inspect.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Zoom controls HUD
            Surface(
                color = DarkSurfaceCard,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, DarkCardBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(2.dp)
                ) {
                    IconButton(
                        onClick = { if (zoomScale > 0.7f) zoomScale -= 0.15f },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${(zoomScale * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { if (zoomScale < 1.4f) zoomScale += 0.15f },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Luxury Status Metric Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricChip("Nodes", "${rawNodes.size}", EmergencyRedPrimary, Modifier.weight(0.9f))
            MetricChip("Depth", "${metrics.treeHeight}", Color(0xFF64B5F6), Modifier.weight(0.8f))
            val topId = rawNodes.firstOrNull()?.id ?: "None"
            MetricChip("Root (Next)", topId, SeverityHigh, Modifier.weight(1.1f))
            MetricChip("Invariant", if (rawNodes.isNotEmpty()) "VALID ✓" else "STANDBY", SeverityLow, Modifier.weight(1.2f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (rawNodes.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DarkCardBorder)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF2C2C36), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountTree, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Binary Heap is Empty", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Add calls from the Intake tab to view the live tree layout.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            // Lavish Canvas Card with Dark Blueprint Grid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0D11)),
                border = BorderStroke(1.2.dp, Color(0xFF2C2D3A))
            ) {
                val horizontalScroll = rememberScrollState()
                val verticalInnerScroll = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScroll)
                        .verticalScroll(verticalInnerScroll)
                ) {
                    val n = rawNodes.size
                    val totalLevels = (floor(ln(n.toDouble()) / ln(2.0)) + 1).toInt()
                    val baseWidth = max(720f, (1 shl (totalLevels - 1)) * 100f) * zoomScale
                    val baseHeight = (totalLevels * 90f + 60f) * zoomScale

                    // Precompute node center positions
                    val nodePositions = remember(n, baseWidth, baseHeight, zoomScale) {
                        val list = ArrayList<Offset>(n)
                        val levelHeight = 84f * zoomScale
                        for (i in 0 until n) {
                            val level = (floor(ln((i + 1).toDouble()) / ln(2.0))).toInt()
                            val indexInLevel = i - ((1 shl level) - 1)
                            val nodesInLevel = 1 shl level
                            val segmentWidth = baseWidth / nodesInLevel
                            val x = (indexInLevel * segmentWidth) + (segmentWidth / 2f)
                            val y = 48f * zoomScale + (level * levelHeight)
                            list.add(Offset(x, y))
                        }
                        list
                    }

                    Canvas(
                        modifier = Modifier
                            .size(baseWidth.dp, baseHeight.dp)
                            .pointerInput(nodePositions, n) {
                                detectTapGestures { tapOffset ->
                                    val radiusPx = 24.dp.toPx() * zoomScale
                                    var clicked: Int? = null
                                    for (i in 0 until n) {
                                        val dist = (tapOffset - nodePositions[i]).getDistance()
                                        if (dist <= radiusPx) {
                                            clicked = i
                                            break
                                        }
                                    }
                                    selectedNodeIndex = clicked
                                }
                            }
                    ) {
                        // 1. Draw High-Tech Blueprint Matrix Dot Grid
                        val gridStep = 24f * zoomScale
                        var gx = 0f
                        while (gx < size.width) {
                            var gy = 0f
                            while (gy < size.height) {
                                drawCircle(
                                    color = Color(0xFF1E212D).copy(alpha = 0.5f),
                                    radius = 1f,
                                    center = Offset(gx, gy)
                                )
                                gy += gridStep
                            }
                            gx += gridStep
                        }

                        // 2. Draw Luxurious Smooth Curved Edges (Bezier paths)
                        for (i in 0 until n) {
                            val left = 2 * i + 1
                            val right = 2 * i + 2
                            val start = nodePositions[i]

                            if (left < n) {
                                val end = nodePositions[left]
                                val path = Path().apply {
                                    moveTo(start.x, start.y)
                                    cubicTo(
                                        start.x, start.y + (end.y - start.y) * 0.55f,
                                        end.x, start.y + (end.y - start.y) * 0.45f,
                                        end.x, end.y
                                    )
                                }
                                drawPath(
                                    path = path,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF81D4FA).copy(alpha = 0.8f), Color(0xFF29B6F6).copy(alpha = 0.3f)),
                                        startY = start.y,
                                        endY = end.y
                                    ),
                                    style = Stroke(width = 2.5f * zoomScale, cap = StrokeCap.Round)
                                )
                            }

                            if (right < n) {
                                val end = nodePositions[right]
                                val path = Path().apply {
                                    moveTo(start.x, start.y)
                                    cubicTo(
                                        start.x, start.y + (end.y - start.y) * 0.55f,
                                        end.x, start.y + (end.y - start.y) * 0.45f,
                                        end.x, end.y
                                    )
                                }
                                drawPath(
                                    path = path,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF81D4FA).copy(alpha = 0.8f), Color(0xFF29B6F6).copy(alpha = 0.3f)),
                                        startY = start.y,
                                        endY = end.y
                                    ),
                                    style = Stroke(width = 2.5f * zoomScale, cap = StrokeCap.Round)
                                )
                            }
                        }

                        // 3. Draw Nodes with Lavish Multi-Layer Glow and Crisp Typography
                        val nodeRadius = 22.dp.toPx() * zoomScale

                        val textPaint = Paint().apply {
                            textAlign = Paint.Align.CENTER
                            textSize = 10.dp.toPx() * zoomScale
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            color = android.graphics.Color.WHITE
                            isAntiAlias = true
                        }

                        val subPaint = Paint().apply {
                            textAlign = Paint.Align.CENTER
                            textSize = 8.dp.toPx() * zoomScale
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                            color = android.graphics.Color.WHITE
                            isAntiAlias = true
                        }

                        val indexPaint = Paint().apply {
                            textAlign = Paint.Align.CENTER
                            textSize = 8.5.dp.toPx() * zoomScale
                            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                            color = android.graphics.Color.rgb(180, 185, 205)
                            isAntiAlias = true
                        }

                        for (i in 0 until n) {
                            val center = nodePositions[i]
                            val node = rawNodes[i]
                            val isSelected = (selectedNodeIndex == i)
                            val isRoot = (i == 0)

                            val (primaryCol, bgGlowCol) = when (node.severity) {
                                4 -> Pair(SeverityCritical, Color(0xFFFF1744))
                                3 -> Pair(SeverityHigh, Color(0xFFFF9100))
                                2 -> Pair(SeverityModerate, Color(0xFFFFD600))
                                else -> Pair(SeverityLow, Color(0xFF00E676))
                            }

                            // Layer 1: Ambient Outer Glow Aura
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(bgGlowCol.copy(alpha = if (isSelected) 0.55f else 0.28f), Color.Transparent),
                                    center = center,
                                    radius = nodeRadius * 2.2f
                                ),
                                radius = nodeRadius * 2.2f,
                                center = center
                            )

                            // Layer 2: Special Halo for Root Node (Next in line)
                            if (isRoot) {
                                drawCircle(
                                    color = Color(0xFFFFD700).copy(alpha = 0.85f),
                                    radius = nodeRadius + 5f * zoomScale,
                                    center = center,
                                    style = Stroke(width = 1.8f * zoomScale, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
                                )
                            }

                            // Layer 3: Solid Inner Body with Radial Glassmorphism Tint
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(primaryCol, Color(0xFF161720)),
                                    center = Offset(center.x - nodeRadius * 0.3f, center.y - nodeRadius * 0.3f),
                                    radius = nodeRadius * 1.4f
                                ),
                                radius = nodeRadius,
                                center = center
                            )

                            // Layer 4: Crisp Metallic / Neon Ring
                            drawCircle(
                                color = if (isSelected) Color.White else primaryCol,
                                radius = nodeRadius,
                                center = center,
                                style = Stroke(width = if (isSelected) 3f * zoomScale else 1.8f * zoomScale)
                            )

                            // Node Text Content
                            drawContext.canvas.nativeCanvas.drawText(
                                node.id,
                                center.x,
                                center.y - 1.5f * zoomScale,
                                textPaint
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "S${node.severity} • #${node.sequenceNumber}",
                                center.x,
                                center.y + 11.dp.toPx() * zoomScale,
                                subPaint
                            )

                            // Index Tag Badge
                            drawContext.canvas.nativeCanvas.drawText(
                                "[$i]",
                                center.x,
                                center.y + nodeRadius + 14.dp.toPx() * zoomScale,
                                indexPaint
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Node Inspection Card (Lavish Details Drawer)
        AnimatedVisibility(
            visible = selectedNode != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedNode?.let { req ->
                val (badgeCol, borderCol) = when (req.severity) {
                    4 -> Pair(SeverityCritical, SeverityCriticalBorder)
                    3 -> Pair(SeverityHigh, SeverityHighBorder)
                    2 -> Pair(SeverityModerate, SeverityModerateBorder)
                    else -> Pair(SeverityLow, SeverityLowBorder)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = BorderStroke(1.2.dp, borderCol)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(badgeCol, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Node [${selectedNodeIndex}]: ${req.id}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = badgeCol.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = req.serviceType,
                                        color = badgeCol,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedNodeIndex = null },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Caller: ${req.callerName} (${req.phoneNumber}) | Location: ${req.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "Description: ${req.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        val parentIdx = if (selectedNodeIndex != null && selectedNodeIndex!! > 0) (selectedNodeIndex!! - 1) / 2 else 0
                        val leftChild = if (selectedNodeIndex != null) 2 * selectedNodeIndex!! + 1 else 0
                        val rightChild = if (selectedNodeIndex != null) 2 * selectedNodeIndex!! + 2 else 0

                        Text(
                            text = "Parent Node: [$parentIdx] | Left Child: [${if (leftChild < rawNodes.size) leftChild else "-"}] | Right Child: [${if (rightChild < rawNodes.size) rightChild else "-"}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64B5F6),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Contiguous Array Inspector
        Text(
            text = "Linear Vector Memory Layout",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "0-Based In-Memory Vector. Array index matches the node position in tree above.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = BorderStroke(1.dp, DarkCardBorder)
        ) {
            val arrayScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(arrayScroll)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rawNodes.isEmpty()) {
                    Text("No array memory entries.", color = TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                } else {
                    rawNodes.forEachIndexed { index, req ->
                        val isSelected = (selectedNodeIndex == index)
                        val (chipBg, chipBorder) = when (req.severity) {
                            4 -> Pair(SeverityCriticalBg, SeverityCriticalBorder)
                            3 -> Pair(SeverityHighBg, SeverityHighBorder)
                            2 -> Pair(SeverityModerateBg, SeverityModerateBorder)
                            else -> Pair(SeverityLowBg, SeverityLowBorder)
                        }

                        Surface(
                            color = if (isSelected) chipBorder.copy(alpha = 0.25f) else chipBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, chipBorder),
                            modifier = Modifier.clickable { selectedNodeIndex = index }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("[$index]", fontSize = 10.sp, color = if (index == 0) Color(0xFFFFD700) else TextSecondary, fontWeight = FontWeight.Bold)
                                Text(req.id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("S${req.severity} • #${req.sequenceNumber}", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun MetricChip(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = DarkSurfaceCard,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
            Text(title, fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
