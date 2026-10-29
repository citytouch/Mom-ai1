package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.assistant.CommandResult
import com.example.assistant.MarathiCommandEngine
import com.example.data.ContactItem
import com.example.data.LocalMomRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MarathiCommandEngineTest {

    private lateinit var context: Context
    private lateinit var repository: LocalMomRepository
    private lateinit var engine: MarathiCommandEngine

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = LocalMomRepository(context)
        engine = MarathiCommandEngine(repository)
    }

    @Test
    fun testCallCommand_RequiresExplicitConfirmationWithContactName() {
        // User says: "राहुलला फोन लावा"
        val result = engine.processCommand("राहुलला फोन लावा", null)

        assertTrue("Result must be RequestCallConfirmation", result is CommandResult.RequestCallConfirmation)
        val confirmation = result as CommandResult.RequestCallConfirmation
        assertEquals("राहुल", confirmation.contact.nameMarathi)
        assertTrue(
            "Prompt must explicitly mention contact name",
            confirmation.promptMarathi.contains("राहुल")
        )
        assertTrue(
            "Prompt must ask for confirmation (हो किंवा नाही)",
            confirmation.promptMarathi.contains("हो किंवा नाही")
        )
    }

    @Test
    fun testPendingCall_ConfirmWithPositiveMarathiWord() {
        val contact = ContactItem(
            id = "c1",
            nameMarathi = "राहुल",
            nameEnglish = "Rahul",
            phoneNumber = "9876543210",
            relationMarathi = "मुलगा"
        )

        // When awaiting confirmation, user says "हो"
        val result = engine.processCommand("हो", contact)
        assertTrue("Result must confirm call", result is CommandResult.ConfirmCall)
        assertEquals("राहुल", (result as CommandResult.ConfirmCall).contact.nameMarathi)
    }

    @Test
    fun testPendingCall_CancelWithNegativeMarathiWord() {
        val contact = ContactItem(
            id = "c1",
            nameMarathi = "राहुल",
            nameEnglish = "Rahul",
            phoneNumber = "9876543210",
            relationMarathi = "मुलगा"
        )

        // When awaiting confirmation, user says "नाही" or "नको"
        val result = engine.processCommand("नाही", contact)
        assertTrue("Result must cancel call", result is CommandResult.CancelCall)
    }

    @Test
    fun testTimeQuery_ReturnsMarathiTime() {
        val result = engine.processCommand("वेळ काय झाली?", null)
        assertTrue(result is CommandResult.SpeakResponse)
        val text = (result as CommandResult.SpeakResponse).responseMarathi
        assertTrue(text.contains("वाज") || text.contains("सध्या"))
    }

    @Test
    fun testDateQuery_ReturnsMarathiDate() {
        val result = engine.processCommand("आज कोणता वार आहे?", null)
        assertTrue(result is CommandResult.SpeakResponse)
        val text = (result as CommandResult.SpeakResponse).responseMarathi
        assertTrue(text.contains("आज") && text.contains(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()))
    }

    @Test
    fun testMedicineQuery_ReturnsMedicineSummary() {
        val result = engine.processCommand("माझी औषधे", null)
        assertTrue(result is CommandResult.SpeakResponse)
        val text = (result as CommandResult.SpeakResponse).responseMarathi
        assertTrue(text.contains("औषध") || text.contains("गोळ"))
    }
}
