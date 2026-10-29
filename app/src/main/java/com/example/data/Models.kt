package com.example.data

data class ContactItem(
    val id: String,
    val nameMarathi: String,
    val nameEnglish: String,
    val phoneNumber: String,
    val relationMarathi: String,
    val isFavorite: Boolean = true,
    val avatarColorHex: Long = 0xFF0F5257
)

data class MedicineReminder(
    val id: String,
    val nameMarathi: String,
    val dosageMarathi: String,
    val timingLabelMarathi: String,
    val isTaken: Boolean = false
)

enum class Sender {
    USER,
    MOM_AI
}

data class AssistantMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class VoiceState {
    object Idle : VoiceState()
    data class Listening(val volumeRms: Float = 0f) : VoiceState()
    data class Processing(val recognizedText: String) : VoiceState()
    data class Speaking(val responseText: String) : VoiceState()
    data class Error(val errorMessageMarathi: String) : VoiceState()
}

sealed class CallConfirmationState {
    object None : CallConfirmationState()
    data class Pending(
        val contact: ContactItem,
        val promptTextMarathi: String
    ) : CallConfirmationState()
}
