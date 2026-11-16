package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun AmbulanceConfirmationDialog(
    callerName: String,
    phoneNumber: String,
    location: String,
    gpsCoordinates: String,
    onConfirmCritical: (selectedSymptoms: List<String>) -> Unit,
    onContinueNonCritical: (selectedSymptoms: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val symptomsList = listOf(
        "Difficulty breathing",
        "Chest pain",
        "Unconsciousness or confusion",
        "Severe bleeding",
        "Sudden weakness or stroke symptoms",
        "Severe pain or rapidly worsening condition",
        "None of these / unsure"
    )

    val selectedSymptoms = remember { mutableStateListOf<String>() }

    fun toggleSymptom(symptom: String) {
        if (symptom == "None of these / unsure") {
            selectedSymptoms.clear()
            selectedSymptoms.add(symptom)
        } else {
            selectedSymptoms.remove("None of these / unsure")
            if (selectedSymptoms.contains(symptom)) {
                selectedSymptoms.remove(symptom)
            } else {
                selectedSymptoms.add(symptom)
            }
        }
    }

    val hasCriticalSymptoms = selectedSymptoms.any { it != "None of these / unsure" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("ambulance_confirmation_dialog"),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F1015),
            border = BorderStroke(1.2.dp, Color(0xFF2C2D3A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Category and Orange Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EMERGENCY SERVICES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        color = Color(0xFF3E200C),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE65100))
                    ) {
                        Text(
                            text = "Confirmation required",
                            color = Color(0xFFFF9800),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Headline with Red Ambulance Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF2E1014), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Emergency,
                            contentDescription = "Ambulance",
                            tint = EmergencyRedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Confirm ambulance request",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle explanation
                Text(
                    text = "Your symptoms were initially assessed as potentially non-critical. However, you may still request an ambulance if you believe medical transport is necessary.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Question Prompt
                Text(
                    text = "Is the patient experiencing any of these symptoms?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Select any that apply.",
                    fontSize = 12.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                // Symptom Checklist
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    symptomsList.forEach { symptom ->
                        val isChecked = selectedSymptoms.contains(symptom)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toggleSymptom(symptom) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { toggleSymptom(symptom) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = EmergencyRedPrimary,
                                    uncheckedColor = TextTertiary,
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = symptom,
                                fontSize = 13.sp,
                                color = if (isChecked) TextPrimary else TextSecondary,
                                fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Patient Location Card
                Surface(
                    color = Color(0xFF191B24),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E3244)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF42A5F5),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Patient location",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Confirm the patient's current location and contact details before submitting.",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Location: $location ($gpsCoordinates)",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "• Contact: $callerName (📞 $phoneNumber)",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Red Button: Confirm Emergency Ambulance Request
                Button(
                    onClick = {
                        if (hasCriticalSymptoms) {
                            onConfirmCritical(selectedSymptoms.toList())
                        } else {
                            onContinueNonCritical(if (selectedSymptoms.isEmpty()) listOf("None of these / unsure") else selectedSymptoms.toList())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedPrimary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("confirm_emergency_ambulance_btn")
                ) {
                    Text(
                        text = if (hasCriticalSymptoms) "Confirm Critical Ambulance Request (High Priority)" else "Confirm Emergency Ambulance Request",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Outlined Button: Continue with Ambulance Request
                OutlinedButton(
                    onClick = {
                        onContinueNonCritical(if (selectedSymptoms.isEmpty()) listOf("None of these / unsure") else selectedSymptoms.toList())
                    },
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF333544)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("continue_ambulance_request_btn")
                ) {
                    Text(
                        text = "Continue with Ambulance Request",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Legal / Clinical Disclaimer
                Text(
                    text = "This screening is not a diagnosis. If the patient is in immediate danger or you are unsure, seek emergency assistance. Requests are subject to dispatcher assessment and service availability.",
                    fontSize = 11.sp,
                    color = TextTertiary,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Dismiss / Back text button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Review Information / Edit", color = TextTertiary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
