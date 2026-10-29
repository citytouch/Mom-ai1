package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.provider.ContactsContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LocalMomRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mom_ai_local_storage", Context.MODE_PRIVATE)

    private val _contacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val contacts: StateFlow<List<ContactItem>> = _contacts.asStateFlow()

    private val _medicines = MutableStateFlow<List<MedicineReminder>>(emptyList())
    val medicines: StateFlow<List<MedicineReminder>> = _medicines.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val savedContactsJson = prefs.getString(KEY_CONTACTS, null)
        if (savedContactsJson.isNullOrBlank()) {
            val defaults = listOf(
                ContactItem(
                    id = "c1",
                    nameMarathi = "राहुल",
                    nameEnglish = "Rahul",
                    phoneNumber = "9876543210",
                    relationMarathi = "मुलगा (Son)",
                    isFavorite = true,
                    avatarColorHex = 0xFF0F5257
                ),
                ContactItem(
                    id = "c2",
                    nameMarathi = "स्नेहल",
                    nameEnglish = "Snehal",
                    phoneNumber = "9876543211",
                    relationMarathi = "मुलगी (Daughter)",
                    isFavorite = true,
                    avatarColorHex = 0xFFB45309
                ),
                ContactItem(
                    id = "c3",
                    nameMarathi = "डॉ. जोशी",
                    nameEnglish = "Dr. Joshi",
                    phoneNumber = "9876543212",
                    relationMarathi = "फॅमिली डॉक्टर (Doctor)",
                    isFavorite = true,
                    avatarColorHex = 0xFF15803D
                ),
                ContactItem(
                    id = "c4",
                    nameMarathi = "आपत्कालीन रुग्णवाहिका",
                    nameEnglish = "Ambulance",
                    phoneNumber = "108",
                    relationMarathi = "रुग्णवाहिका मदत (Ambulance)",
                    isFavorite = false,
                    avatarColorHex = 0xFFB91C1C
                )
            )
            saveContactsList(defaults)
            _contacts.value = defaults
        } else {
            _contacts.value = parseContacts(savedContactsJson)
        }

        val savedMedsJson = prefs.getString(KEY_MEDICINES, null)
        if (savedMedsJson.isNullOrBlank()) {
            val defaultMeds = listOf(
                MedicineReminder(
                    id = "m1",
                    nameMarathi = "बीपी ची गोळी",
                    dosageMarathi = "१ गोळी",
                    timingLabelMarathi = "सकाळी नाश्त्यानंतर",
                    isTaken = false
                ),
                MedicineReminder(
                    id = "m2",
                    nameMarathi = "कॅल्शियम गोळी",
                    dosageMarathi = "१ गोळी",
                    timingLabelMarathi = "दुपारी जेवणानंतर",
                    isTaken = false
                ),
                MedicineReminder(
                    id = "m3",
                    nameMarathi = "साखरेची गोळी",
                    dosageMarathi = "अर्धी गोळी",
                    timingLabelMarathi = "रात्री जेवणापूर्वी",
                    isTaken = false
                )
            )
            saveMedicinesList(defaultMeds)
            _medicines.value = defaultMeds
        } else {
            _medicines.value = parseMedicines(savedMedsJson)
        }
    }

    fun addContact(
        nameMarathi: String,
        nameEnglish: String,
        phoneNumber: String,
        relationMarathi: String
    ) {
        val newContact = ContactItem(
            id = UUID.randomUUID().toString(),
            nameMarathi = nameMarathi.trim(),
            nameEnglish = nameEnglish.trim(),
            phoneNumber = phoneNumber.trim(),
            relationMarathi = relationMarathi.trim(),
            isFavorite = true,
            avatarColorHex = 0xFF0F5257
        )
        val updated = _contacts.value + newContact
        saveContactsList(updated)
        _contacts.value = updated
    }

    fun deleteContact(id: String) {
        val updated = _contacts.value.filterNot { it.id == id }
        saveContactsList(updated)
        _contacts.value = updated
    }

    fun toggleMedicineTaken(id: String) {
        val updated = _medicines.value.map {
            if (it.id == id) it.copy(isTaken = !it.isTaken) else it
        }
        saveMedicinesList(updated)
        _medicines.value = updated
    }

    fun addMedicine(nameMarathi: String, dosage: String, timing: String) {
        val newMed = MedicineReminder(
            id = UUID.randomUUID().toString(),
            nameMarathi = nameMarathi.trim(),
            dosageMarathi = dosage.trim(),
            timingLabelMarathi = timing.trim(),
            isTaken = false
        )
        val updated = _medicines.value + newMed
        saveMedicinesList(updated)
        _medicines.value = updated
    }

    fun deleteMedicine(id: String) {
        val updated = _medicines.value.filterNot { it.id == id }
        saveMedicinesList(updated)
        _medicines.value = updated
    }

    /**
     * Looks up contact matching spoken query in Marathi or English names/relations.
     */
    fun findContactForQuery(query: String): ContactItem? {
        val cleanQuery = query.lowercase().trim()
        val allContacts = _contacts.value

        // Direct matching against Marathi name, English name, or relation
        for (c in allContacts) {
            val mr = c.nameMarathi.lowercase()
            val en = c.nameEnglish.lowercase()
            val rel = c.relationMarathi.lowercase()

            // Handle Marathi grammatical suffixes like "राहुलला", "राहुलला फोन", "डॉक्टरांना"
            val mrStem = mr.removeSuffix("ला").removeSuffix("ना").removeSuffix("ंना")
            if (cleanQuery.contains(mr) || cleanQuery.contains(mrStem) ||
                (en.isNotBlank() && cleanQuery.contains(en)) ||
                cleanQuery.contains(rel)
            ) {
                return c
            }
        }

        // Search device contacts if phone permission granted
        return searchDeviceContacts(cleanQuery)
    }

    private fun searchDeviceContacts(query: String): ContactItem? {
        try {
            val contentResolver = context.contentResolver
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = it.getString(nameIdx) ?: continue
                    val number = it.getString(numberIdx) ?: continue
                    val cleanName = name.lowercase()
                    if (query.contains(cleanName) || cleanName.contains(query)) {
                        return ContactItem(
                            id = "phone_${System.currentTimeMillis()}",
                            nameMarathi = name,
                            nameEnglish = name,
                            phoneNumber = number,
                            relationMarathi = "फोन संपर्क",
                            isFavorite = false
                        )
                    }
                }
            }
        } catch (_: SecurityException) {
            // Permission not granted, fall back gracefully to local contacts
        } catch (_: Exception) {
            // Ignore device contacts lookup error
        }
        return null
    }

    private fun saveContactsList(list: List<ContactItem>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("nameMarathi", item.nameMarathi)
                put("nameEnglish", item.nameEnglish)
                put("phoneNumber", item.phoneNumber)
                put("relationMarathi", item.relationMarathi)
                put("isFavorite", item.isFavorite)
                put("avatarColorHex", item.avatarColorHex)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CONTACTS, jsonArray.toString()).apply()
    }

    private fun parseContacts(jsonStr: String): List<ContactItem> {
        val list = mutableListOf<ContactItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ContactItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        nameMarathi = obj.optString("nameMarathi", ""),
                        nameEnglish = obj.optString("nameEnglish", ""),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        relationMarathi = obj.optString("relationMarathi", ""),
                        isFavorite = obj.optBoolean("isFavorite", true),
                        avatarColorHex = obj.optLong("avatarColorHex", 0xFF0F5257)
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return list
    }

    private fun saveMedicinesList(list: List<MedicineReminder>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("nameMarathi", item.nameMarathi)
                put("dosageMarathi", item.dosageMarathi)
                put("timingLabelMarathi", item.timingLabelMarathi)
                put("isTaken", item.isTaken)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_MEDICINES, jsonArray.toString()).apply()
    }

    private fun parseMedicines(jsonStr: String): List<MedicineReminder> {
        val list = mutableListOf<MedicineReminder>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MedicineReminder(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        nameMarathi = obj.optString("nameMarathi", ""),
                        dosageMarathi = obj.optString("dosageMarathi", ""),
                        timingLabelMarathi = obj.optString("timingLabelMarathi", ""),
                        isTaken = obj.optBoolean("isTaken", false)
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return list
    }

    companion object {
        private const val KEY_CONTACTS = "contacts_list"
        private const val KEY_MEDICINES = "medicines_list"
    }
}
