package com.kali

import com.kali.network.RegisterRequest
import com.kali.network.LoginRequest
import com.kali.network.GoogleLoginRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    // Simple robust JVM-safe email validation regex matching Patterns.EMAIL_ADDRESS
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }

    // JVM-safe password strength validation utility
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    // JVM-safe role validation utility
    private fun isValidRole(role: String): Boolean {
        val validRoles = listOf("User", "Guardian", "Police")
        return validRoles.contains(role)
    }

    @Test
    fun testEmailValidationUtility() {
        assertTrue(isValidEmail("prite.shinde@gmail.com"))
        assertTrue(isValidEmail("demo.kali@outlook.co.in"))
        assertFalse(isValidEmail("invalid-email-address"))
        assertFalse(isValidEmail("test@"))
        assertFalse(isValidEmail("@example.com"))
    }

    @Test
    fun testPasswordStrengthUtility() {
        assertTrue(isValidPassword("password123"))
        assertTrue(isValidPassword("123456"))
        assertFalse(isValidPassword("123"))
        assertFalse(isValidPassword(""))
    }

    @Test
    fun testRoleValidationUtility() {
        assertTrue(isValidRole("User"))
        assertTrue(isValidRole("Guardian"))
        assertTrue(isValidRole("Police"))
        assertFalse(isValidRole("Admin")) // Admin role restricted on client registration
        assertFalse(isValidRole("Stranger"))
    }

    @Test
    fun testDataModelPayloadAssembly() {
        val registerReq = RegisterRequest(
            name = "Prite Shinde",
            phone = "9876543210",
            email = "prite.shinde@gmail.com",
            password = "SecretPassword",
            role = "Guardian"
        )
        assertEquals("Prite Shinde", registerReq.name)
        assertEquals("9876543210", registerReq.phone)
        assertEquals("prite.shinde@gmail.com", registerReq.email)
        assertEquals("SecretPassword", registerReq.password)
        assertEquals("Guardian", registerReq.role)
    }

    @Test
    fun testGooglePayloadAssembly() {
        val googleReq = GoogleLoginRequest(
            email = "prite.shinde@gmail.com",
            name = "Prite Shinde",
            googleId = "g_real_112233"
        )
        assertEquals("prite.shinde@gmail.com", googleReq.email)
        assertEquals("Prite Shinde", googleReq.name)
        assertEquals("g_real_112233", googleReq.googleId)
    }
}
