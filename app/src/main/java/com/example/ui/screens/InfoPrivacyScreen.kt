package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SaffronSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight

@Composable
fun InfoPrivacyScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "माहिती, गोपनीयता व सुरक्षितता",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "आई AI ॲपची कार्यपद्धती आणि पारदर्शकता",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Section 1: Call Confirmation Guarantee
        InfoSectionCard(
            title = "१. फोन कॉल सुरक्षितता (२-टप्पा पुष्टी)",
            icon = Icons.Default.Phone,
            iconTint = SuccessGreen,
            containerColor = Color.White
        ) {
            Text(
                text = "• सुरक्षा नियम: हे ॲप कोणालाही परस्पर फोन लावत नाही.\n" +
                        "• जेव्हा आई 'राहुलला फोन लावा' म्हणतात, तेव्हा ॲप प्रथम स्पष्टपणे व्यक्तीचे नाव उच्चारते:\n" +
                        "  \"तुम्हाला राहुलला फोन करायचा आहे का? हो किंवा नाही सांगा.\"\n" +
                        "• आईने आवाजाने 'हो' म्हटल्याशिवाय किंवा स्क्रीनवरील मोठ्या हिरव्या बटणावर दाबल्याशिवाय कॉल लागत नाही.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section 2: Speech Recognition & Offline Transparency
        InfoSectionCard(
            title = "२. आवाज ओळख व ऑफलाइन स्थितीची वस्तुस्थिती",
            icon = Icons.Default.Mic,
            iconTint = TealPrimary,
            containerColor = Color.White
        ) {
            Text(
                text = "• आवाज ओळख तंत्रज्ञान: हे ॲप Android च्या अधिकृत SpeechRecognizer आणि Google स्पीच सेवेचा वापर करते.\n" +
                        "• ऑफलाइन स्थिती: सध्या आवाज अचूक ओळखण्यासाठी फोनवर इंटरनेट (वाय-फाय किंवा मोबाइल डेटा) आवश्यक आहे. जोपर्यंत फोनच्या Google ॲपमध्ये मराठी ऑफलाइन व्हॉइस पॅक डाउनलोड केलेला नाही, तोपर्यंत हे ऑफलाइन काम करू शकत नाही.\n" +
                        "• हे ॲप ऑफलाइन असल्याचा कोणताही खोटा दावा करत नाही.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section 3: Speaker Verification Notice
        InfoSectionCard(
            title = "३. व्हॉइस व्हेरिफिकेशन (आईच्या आवाजाची ओळख)",
            icon = Icons.Default.RecordVoiceOver,
            iconTint = SaffronSecondary,
            containerColor = Color.White
        ) {
            Text(
                text = "• पारदर्शक खुलासा: सध्या हे ॲप कोणताही मराठी आवाज ऐकून त्यानुसार कृती करते.\n" +
                        "• आईचा स्वतःचा विशिष्ट आवाज (बायोमेट्रिक व्हॉइस व्हेरिफिकेशन) तपासण्याची प्रणाली अजून पूर्ण चाचणी झालेली नाही, त्यामुळे हे ॲप केवळ आईचाच आवाज ओळखते असा कोणताही दावा करत नाही.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section 4: Privacy & Local Storage
        InfoSectionCard(
            title = "४. १००% स्थानिक व खाजगी डेटा (Local Storage)",
            icon = Icons.Default.Lock,
            iconTint = TealPrimary,
            containerColor = Color.White
        ) {
            Text(
                text = "• संपर्क यादी आणि औषधांच्या नोंदी फक्त तुमच्या फोनवरच (Internal Storage) खाजगीरीत्या साठवल्या जातात.\n" +
                        "• कोणताही वैयक्तिक डेटा, फोन नंबर किंवा संभाषणाचा इतिहास कोणत्याही बाह्य सर्व्हरवर पाठवला किंवा विकला जात नाही.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section 5: Permissions & Background Limits
        InfoSectionCard(
            title = "५. Android परवानग्या व बॅकग्राउंड मर्यादा",
            icon = Icons.Default.Security,
            iconTint = Color(0xFFE11D48),
            containerColor = Color.White
        ) {
            Text(
                text = "• मायक्रोफोन (RECORD_AUDIO): जेव्हा आई 'बोला' वर दाबतात तेव्हाच आवाज ऐकण्यासाठी लागतो.\n" +
                        "• फोन कॉल (CALL_PHONE): आईने पुष्टी दिल्यावर थेट नंबर फिरवण्यासाठी लागतो.\n" +
                        "• फोन संपर्क (READ_CONTACTS): नातेवाईकांची नावे फोनबुकमधून शोधण्यासाठी लागतो.\n" +
                        "• बॅकग्राउंड मर्यादा: Android ची गोपनीयता धोरणे बॅकग्राउंडमध्ये २४ तास मायक्रोफोन चालू ठेवण्यास परवानगी देत नाहीत (यामुळे बॅटरीही संपते). म्हणूनच ॲप उघडून एका बटणावर दाबून बोलणे ही सर्वात विश्वासार्ह आणि सुरक्षित पद्धत आहे.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section 6: Native Android APK & Testing Guide
        InfoSectionCard(
            title = "६. तांत्रिक माहिती व फोनवर चाचणी कशी करावी?",
            icon = Icons.Default.Android,
            iconTint = SuccessGreen,
            containerColor = TealPrimaryLight.copy(alpha = 0.5f)
        ) {
            Text(
                text = "• हे वेब ॲप नसून परिपूर्ण 'Native Android App' (Kotlin + Jetpack Compose) आहे!\n" +
                        "• संगणकावर Android Studio ची गरज नाही: तुम्ही Google AI Studio च्या मेनूमधून थेट 'Download APK' करू शकता.\n" +
                        "• तो APK फाइल आईच्या अँड्रॉइड फोनवर पाठवा आणि त्यावर टॅप करून इन्स्टॉल करा.\n" +
                        "• ॲप उघडून 'बोला' दाबा आणि 'वेळ काय झाली?' किंवा 'राहुलला फोन लावा' असे बोलून चाचणी घ्या!",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun InfoSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
