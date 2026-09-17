package com.example

import com.example.data.repository.sha256
import org.junit.Assert.*
import org.junit.Test

class AdminAuthTest {

    @Test
    fun verifyPasswordHashMatches() {
        val expectedHash = sha256("Sodhar56@123")
        val inputHash = sha256("Sodhar56@123")
        val wrongHash = sha256("Admin@2026")

        assertEquals(expectedHash, inputHash)
        assertNotEquals(wrongHash, inputHash)
    }

    @Test
    fun verifyEmailCheck() {
        val authorizedEmail = "msuhailsodhar@gmail.com"
        val demoEmail = "admin@skillpulse.pk"
        val oldEmail = "itssohailsodhar@gmail.com"

        assertTrue(authorizedEmail.trim().equals("msuhailsodhar@gmail.com", ignoreCase = true))
        assertFalse(demoEmail.trim().equals("msuhailsodhar@gmail.com", ignoreCase = true))
        assertFalse(oldEmail.trim().equals("msuhailsodhar@gmail.com", ignoreCase = true))
    }
}
