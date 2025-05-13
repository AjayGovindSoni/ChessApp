package com.example

import com.example.routing.request.verifyEmail
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test


class UnitTest {
    val validUsername = ""
    @Test
    fun `valid email should return true`() {
        assertTrue(verifyEmail("test@example.com"))
    }

    @Test
    fun `invalid email format should return false`() {
        assertFalse(verifyEmail("invalid-email"))
        assertFalse(verifyEmail("invalid@.com"))
        assertFalse(verifyEmail("@example.com"))
    }

    @Test
    fun `non-existent domain should return false`() {
        assertFalse(verifyEmail("test@nonexistentdomain123.com"))
    }
}