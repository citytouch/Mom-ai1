package com.example.assistant

import com.example.data.ContactItem
import com.example.data.LocalMomRepository
import com.example.data.MedicineReminder
import java.util.Calendar

sealed class CommandResult {
    data class SpeakResponse(val responseMarathi: String) : CommandResult()
    data class RequestCallConfirmation(
        val contact: ContactItem,
        val promptMarathi: String
    ) : CommandResult()
    data class ConfirmCall(val contact: ContactItem) : CommandResult()
    object CancelCall : CommandResult()
}

class MarathiCommandEngine(private val repository: LocalMomRepository) {

    fun processCommand(
        userSpeech: String,
        pendingCallContact: ContactItem?
    ): CommandResult {
        val trimmed = userSpeech.trim()
        val lower = trimmed.lowercase()

        // 1. If we are currently awaiting confirmation to place a phone call
        if (pendingCallContact != null) {
            if (isConfirmationPositive(lower)) {
                return CommandResult.ConfirmCall(pendingCallContact)
            }
            if (isConfirmationNegative(lower)) {
                return CommandResult.CancelCall
            }
            // If unclear while awaiting confirmation, ask clearly again
            return CommandResult.SpeakResponse(
                "तुम्हाला ${pendingCallContact.nameMarathi} ला फोन करायचा आहे का? कृपया स्पष्टपणे 'हो' किंवा 'नाही' सांगा."
            )
        }

        // 2. Call Intent Detection
        if (isCallIntent(lower)) {
            val contact = repository.findContactForQuery(lower)
            return if (contact != null) {
                // Strict requirement: Never place a phone call without first stating contact name and asking for confirmation
                CommandResult.RequestCallConfirmation(
                    contact = contact,
                    promptMarathi = "तुम्हाला ${contact.nameMarathi} ला फोन करायचा आहे का? हो किंवा नाही सांगा."
                )
            } else {
                val extractedName = extractTargetName(lower)
                CommandResult.SpeakResponse(
                    if (extractedName.isNotBlank()) {
                        "मला '$extractedName' चा संपर्क सापडला नाही. कृपया संपर्क यादीत नाव तपासा किंवा नवीन संपर्क जोडा."
                    } else {
                        "तुम्हाला कोणाला फोन करायचा आहे? उदाहरणार्थ: 'राहुलला फोन लावा' असे बोला."
                    }
                )
            }
        }

        // 3. Time Intent
        if (isTimeIntent(lower)) {
            return CommandResult.SpeakResponse(getCurrentTimeInMarathi())
        }

        // 4. Date and Day Intent
        if (isDateIntent(lower)) {
            return CommandResult.SpeakResponse(getCurrentDateInMarathi())
        }

        // 5. Medicine / Health Intent
        if (isMedicineIntent(lower)) {
            return CommandResult.SpeakResponse(getMedicinesSummaryInMarathi())
        }

        // 6. Emergency / Help Intent
        if (isEmergencyIntent(lower)) {
            val ambulance = repository.contacts.value.firstOrNull {
                it.phoneNumber == "108" || it.nameMarathi.contains("रुग्णवाहिका")
            } ?: repository.contacts.value.firstOrNull()

            return if (ambulance != null) {
                CommandResult.RequestCallConfirmation(
                    contact = ambulance,
                    promptMarathi = "आपत्कालीन मदत: तुम्हाला ${ambulance.nameMarathi} ला फोन करायचा आहे का? हो किंवा नाही सांगा."
                )
            } else {
                CommandResult.SpeakResponse("आपत्कालीन परिस्थिती असल्यास कृपया त्वरित १०८ किंवा ११२ वर कॉल करा.")
            }
        }

        // 7. Greeting & Wellbeing Intent
        if (isGreetingIntent(lower)) {
            return CommandResult.SpeakResponse(
                "नमस्कार आई! मी अगदी छान आहे. तुम्ही कसे आहात? मी तुम्हाला फोन लावण्यासाठी, वेळ किंवा औषधांची आठवण करून देण्यासाठी मदत करू शकते."
            )
        }

        // 8. Help on capabilities
        if (lower.contains("काय करू शकतेस") || lower.contains("मदत") || lower.contains("help") || lower.contains("काय विचारू")) {
            return CommandResult.SpeakResponse(
                "आई, तुम्ही मला 'वेळ काय झाली?', 'आज कोणता वार आहे?', 'माझी औषधे', किंवा 'राहुलला फोन लावा' असे विचारू शकता."
            )
        }

        // Fallback friendly response
        return CommandResult.SpeakResponse(
            "मी ऐकले: '$trimmed'. तुम्ही मला वेळ, आजचा वार, औषधे किंवा फोन लावण्यासाठी विचारू शकता."
        )
    }

    private fun isConfirmationPositive(text: String): Boolean {
        val positiveWords = listOf(
            "हो", "होय", "कॉल करा", "फोन लावा", "लावा", "करा", "हो लावा",
            "yes", "yeah", "sure", "ok", "ओके", "हो फोन करा"
        )
        return positiveWords.any { text == it || text.contains(it) }
    }

    private fun isConfirmationNegative(text: String): Boolean {
        val negativeWords = listOf(
            "नाही", "नको", "रद्द", "रद्द करा", "थांबा", "नको लावा", "बंद करा",
            "no", "cancel", "stop", "don't", "dont"
        )
        return negativeWords.any { text == it || text.contains(it) }
    }

    private fun isCallIntent(text: String): Boolean {
        val keywords = listOf(
            "फोन लावा", "कॉल करा", "फोन करा", "कॉल लावा", "फोन", "कॉल", "call", "phone",
            "बोलणे करून द्या", "लावा"
        )
        return keywords.any { text.contains(it) }
    }

    private fun extractTargetName(text: String): String {
        return text
            .replace("फोन लावा", "")
            .replace("कॉल करा", "")
            .replace("फोन करा", "")
            .replace("कॉल लावा", "")
            .replace("कॉल", "")
            .replace("फोन", "")
            .replace("ला", "")
            .replace("ना", "")
            .trim()
    }

    private fun isTimeIntent(text: String): Boolean {
        val keywords = listOf("वेळ काय झाली", "किती वाजले", "वेळ सांगा", "वेळ", "time", "घड्याळ")
        return keywords.any { text.contains(it) }
    }

    private fun isDateIntent(text: String): Boolean {
        val keywords = listOf("वार कोणता", "वार", "तारीख", "दिनांक", "आजचा दिवस", "आज काय वार", "date", "day")
        return keywords.any { text.contains(it) }
    }

    private fun isMedicineIntent(text: String): Boolean {
        val keywords = listOf("गोळी", "गोळ्या", "औषध", "औषधे", "दवा", "tablet", "medicine")
        return keywords.any { text.contains(it) }
    }

    private fun isEmergencyIntent(text: String): Boolean {
        val keywords = listOf("आपत्कालीन", "रुग्णवाहिका", "अॅम्ब्युलन्स", "संकट", "वाचवा", "emergency")
        return keywords.any { text.contains(it) }
    }

    private fun isGreetingIntent(text: String): Boolean {
        val keywords = listOf("नमस्कार", "कसे आहात", "कशी आहेस", "सुप्रभात", "शुभ संध्याकाळ", "शुभ रात्री", "हॅलो", "hello", "hi")
        return keywords.any { text.contains(it) }
    }

    private fun getCurrentTimeInMarathi(): String {
        val cal = Calendar.getInstance()
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val period = when (hour24) {
            in 4..11 -> "सकाळचे"
            in 12..15 -> "दुपारचे"
            in 16..19 -> "संध्याकाळचे"
            else -> "रात्रीचे"
        }

        val hour12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minuteText = if (minute == 0) "" else " वाजून $minute मिनिटे"

        return "सध्या $period $hour12$minuteText झाली आहेत."
    }

    private fun getCurrentDateInMarathi(): String {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)

        val weekDaysMarathi = arrayOf(
            "", "रविवार", "सोमवार", "मंगळवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार"
        )

        val monthsMarathi = arrayOf(
            "जानेवारी", "फेब्रुवारी", "मार्च", "एप्रिल", "मे", "जून",
            "जुलै", "ऑगस्ट", "सप्टेंबर", "ऑक्टोबर", "नोव्हेंबर", "डिसेंबर"
        )

        val dayName = weekDaysMarathi.getOrElse(dayOfWeek) { "" }
        val monthName = monthsMarathi.getOrElse(month) { "" }

        return "आज $dayName, $dayOfMonth $monthName $year आहे."
    }

    private fun getMedicinesSummaryInMarathi(): String {
        val meds = repository.medicines.value
        if (meds.isEmpty()) {
            return "तुमच्या आजच्या यादीत कोणतीही औषधे नोंदवलेली नाहीत."
        }

        val pending = meds.filterNot { it.isTaken }
        return if (pending.isEmpty()) {
            "छान बातमी! तुम्ही आजची सर्व औषधे घेतली आहेत."
        } else {
            val listText = pending.joinToString(separator = ", ") {
                "${it.nameMarathi} (${it.timingLabelMarathi})"
            }
            "आज घ्यायची शिल्लक औषधे आहेत: $listText."
        }
    }
}
