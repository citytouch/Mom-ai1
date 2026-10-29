package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.MomAiViewModel
import com.example.ui.PhoneCallAction
import com.example.ui.UiTab
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.InfoPrivacyScreen
import com.example.ui.screens.MedicinesScreen
import com.example.ui.screens.VoiceMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight

class MainActivity : ComponentActivity() {

    private val viewModel: MomAiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    viewModel = viewModel,
                    onPlaceCall = { phoneNumber, contactName ->
                        makePhoneCall(phoneNumber, contactName)
                    }
                )
            }
        }
    }

    private fun makePhoneCall(phoneNumber: String, contactName: String) {
        val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
        val uri = Uri.parse("tel:$cleanNumber")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                val callIntent = Intent(Intent.ACTION_CALL, uri)
                startActivity(callIntent)
            } catch (e: Exception) {
                // Fallback to dialer if direct call fails
                val dialIntent = Intent(Intent.ACTION_DIAL, uri)
                startActivity(dialIntent)
            }
        } else {
            // Open dialer safely with contact number pre-filled
            val dialIntent = Intent(Intent.ACTION_DIAL, uri)
            startActivity(dialIntent)
        }
        Toast.makeText(this, "$contactName ला फोन लावला जात आहे", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun MainAppContent(
    viewModel: MomAiViewModel,
    onPlaceCall: (phoneNumber: String, contactName: String) -> Unit
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()

    // Permission launcher for Microphone, Phone Calls, and Contacts
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (!recordAudioGranted) {
            Toast.makeText(context, "आवाज ऐकण्यासाठी मायक्रोफोन परवानगी आवश्यक आहे", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS
            )
        )
    }

    // Listen for call trigger from ViewModel after explicit confirmation
    LaunchedEffect(viewModel) {
        viewModel.phoneCallEvent.collect { action ->
            when (action) {
                is PhoneCallAction.InitiateCall -> {
                    onPlaceCall(action.phoneNumber, action.contactName)
                }
            }
        }
    }

    // Back handler: Return to Main Voice tab if currently on secondary tab
    BackHandler(enabled = currentTab !is UiTab.MainVoice) {
        viewModel.switchTab(UiTab.MainVoice)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab is UiTab.MainVoice,
                    onClick = { viewModel.switchTab(UiTab.MainVoice) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "मुख्य स्क्रीन (बोला)",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "बोला",
                            fontSize = 15.sp,
                            fontWeight = if (currentTab is UiTab.MainVoice) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = TealPrimaryLight,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_main")
                )

                NavigationBarItem(
                    selected = currentTab is UiTab.Contacts,
                    onClick = { viewModel.switchTab(UiTab.Contacts) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "महत्त्वाचे संपर्क",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "संपर्क",
                            fontSize = 15.sp,
                            fontWeight = if (currentTab is UiTab.Contacts) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = TealPrimaryLight,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_contacts")
                )

                NavigationBarItem(
                    selected = currentTab is UiTab.Medicines,
                    onClick = { viewModel.switchTab(UiTab.Medicines) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = "माझी औषधे",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "औषधे",
                            fontSize = 15.sp,
                            fontWeight = if (currentTab is UiTab.Medicines) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = TealPrimaryLight,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_medicines")
                )

                NavigationBarItem(
                    selected = currentTab is UiTab.PrivacyInfo,
                    onClick = { viewModel.switchTab(UiTab.PrivacyInfo) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "माहिती व सुरक्षा",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "माहिती",
                            fontSize = 15.sp,
                            fontWeight = if (currentTab is UiTab.PrivacyInfo) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = TealPrimaryLight,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_info")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                is UiTab.MainVoice -> {
                    VoiceMainScreen(
                        viewModel = viewModel,
                        onOpenPrivacyInfo = { viewModel.switchTab(UiTab.PrivacyInfo) }
                    )
                }
                is UiTab.Contacts -> {
                    ContactsScreen(viewModel = viewModel)
                }
                is UiTab.Medicines -> {
                    MedicinesScreen(viewModel = viewModel)
                }
                is UiTab.PrivacyInfo -> {
                    InfoPrivacyScreen()
                }
            }
        }
    }
}
