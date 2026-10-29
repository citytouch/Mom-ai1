package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.ContactItem
import com.example.ui.MomAiViewModel
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight

@Composable
fun ContactsScreen(
    viewModel: MomAiViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.repository.contacts.collectAsState()
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
                    text = "महत्त्वाचे संपर्क",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "कुटुंब व आपत्कालीन फोन नंबर",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("add_contact_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("नवीन", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Security guarantee notice
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = TealPrimaryLight.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "सुरक्षा नियम: कोणत्याही संपर्काला थेट फोन लागत नाही; नाव पुष्टी केल्याशिवाय कॉल होत नाही.",
                    fontSize = 13.sp,
                    color = TealPrimary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(contacts, key = { it.id }) { contact ->
                ContactCard(
                    contact = contact,
                    onCallClick = {
                        viewModel.requestCallForContact(contact)
                    },
                    onDeleteClick = {
                        viewModel.repository.deleteContact(contact.id)
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { nameMr, nameEn, phone, rel ->
                viewModel.repository.addContact(nameMr, nameEn, phone, rel)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ContactCard(
    contact: ContactItem,
    onCallClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(contact.avatarColorHex))
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = contact.nameMarathi,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (contact.relationMarathi.isNotBlank()) {
                        Text(
                            text = contact.relationMarathi,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = contact.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Call button with phone icon
                Button(
                    onClick = onCallClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.testTag("call_contact_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "फोन लावा",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "फोन",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "हटवा",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (nameMarathi: String, nameEnglish: String, phone: String, relation: String) -> Unit
) {
    var nameMarathi by remember { mutableStateOf("") }
    var nameEnglish by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "नवीन संपर्क जोडा",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameMarathi,
                    onValueChange = { nameMarathi = it },
                    label = { Text("नाव (मराठीत, उदा. ताई / काका)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_name_mr"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nameEnglish,
                    onValueChange = { nameEnglish = it },
                    label = { Text("नाव इंग्रजीत (उदा. Tai / Kaka)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_name_en"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("फोन नंबर") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_phone"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("नाते (उदा. मोठी बहीण)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_relation"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameMarathi.isNotBlank() && phoneNumber.isNotBlank()) {
                        onAdd(nameMarathi, nameEnglish, phoneNumber, relation)
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
