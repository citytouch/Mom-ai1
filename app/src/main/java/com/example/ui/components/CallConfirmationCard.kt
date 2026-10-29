package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ContactItem
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.SaffronSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight

@Composable
fun CallConfirmationCard(
    contact: ContactItem,
    promptText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(3.dp, SaffronSecondary, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Confirmation alert banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SuccessGreenLight, RoundedCornerShape(12.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "फोन लावण्यासाठी खात्री करा",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SuccessGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Avatar & Name
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(contact.avatarColorHex))
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = contact.nameMarathi,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (contact.relationMarathi.isNotBlank()) {
                Text(
                    text = contact.relationMarathi,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "फोन नंबर: ${contact.phoneNumber}",
                fontSize = 17.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "तुम्हाला ${contact.nameMarathi} ला फोन करायचा आहे का?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "टीप: तुम्ही आवाजानेही 'हो' किंवा 'नाही' म्हणू शकता.",
                fontSize = 15.sp,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Two Large, High-Contrast Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Confirm YES button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("confirm_call_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "हो, फोन लावा",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cancel NO button
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("cancel_call_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRedLight,
                        contentColor = DangerRed
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "नाही, रद्द करा",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
