package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicineReminder
import com.example.ui.MomAiViewModel
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight

@Composable
fun MedicinesScreen(
    viewModel: MomAiViewModel,
    modifier: Modifier = Modifier
) {
    val medicines by viewModel.repository.medicines.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "माझी रोजची औषधे",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "वेळेवर गोळ्या घेण्याची आठवण",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("add_medicine_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("नवीन", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spoken reminder button
        Button(
            onClick = {
                viewModel.onTextCommandSubmitted("माझी औषधे")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("speak_medicines_summary_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimaryLight, contentColor = TealPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = TealPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "आजच्या गोळ्या आवाजात ऐका",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(medicines, key = { it.id }) { med ->
                MedicineCard(
                    medicine = med,
                    onToggleTaken = { viewModel.repository.toggleMedicineTaken(med.id) },
                    onDelete = { viewModel.repository.deleteMedicine(med.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showAddDialog) {
        AddMedicineDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, dose, timing ->
                viewModel.repository.addMedicine(name, dose, timing)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MedicineCard(
    medicine: MedicineReminder,
    onToggleTaken: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleTaken() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (medicine.isTaken) SuccessGreenLight.copy(alpha = 0.5f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Large Check Circle for easy toggle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            if (medicine.isTaken) SuccessGreen else Color(0xFFE2E8F0)
                        )
                ) {
                    if (medicine.isTaken) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "घेतली",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = "शिल्लक",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = medicine.nameMarathi,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${medicine.timingLabelMarathi} • ${medicine.dosageMarathi}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (medicine.isTaken) "✓ गोळी घेतली आहे" else "○ गोळी घेणे बाकी आहे (टॅप करा)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (medicine.isTaken) SuccessGreen else Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "हटवा",
                    tint = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, dose: String, timing: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("१ गोळी") }
    var timing by remember { mutableStateOf("सकाळी नाश्त्यानंतर") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "नवीन गोळी जोडा",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("गोळीचे नाव (उदा. बीपी / डोकेदुखी)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = { Text("प्रमाण (उदा. १ गोळी / अर्धी गोळी)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = timing,
                    onValueChange = { timing = it },
                    label = { Text("वेळ (उदा. सकाळी नाश्त्यानंतर / रात्री)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, dose, timing)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("साठवा")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द")
            }
        }
    )
}
