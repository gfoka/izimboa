package com.izimboa.keycloak.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PhoneNumbersTest {

    @Test
    void normalizesCameroonianNumbers() {
        assertEquals("+237699123456", PhoneNumbers.normalize("6 99 12 34 56"));
        assertEquals("+237699123456", PhoneNumbers.normalize("699-12-34-56"));
        assertEquals("+237699123456", PhoneNumbers.normalize("237699123456"));
        assertEquals("+237699123456", PhoneNumbers.normalize("+237 6 99 12 34 56"));
        assertEquals("+237699123456", PhoneNumbers.normalize("00237699123456"));
        assertEquals("+237222123456", PhoneNumbers.normalize("222 12 34 56"));
    }

    @Test
    void acceptsForeignNumbersWithPlus() {
        assertEquals("+33612345678", PhoneNumbers.normalize("+33 6 12 34 56 78"));
        assertEquals("+15550179613", PhoneNumbers.normalize("+1 555 017 9613"));
    }

    @Test
    void rejectsImplausibleNumbers() {
        assertNull(PhoneNumbers.normalize(null));
        assertNull(PhoneNumbers.normalize(""));
        assertNull(PhoneNumbers.normalize("12345"));
        assertNull(PhoneNumbers.normalize("599123456"));
        assertNull(PhoneNumbers.normalize("+23769912345"));
        assertNull(PhoneNumbers.normalize("abc"));
        assertNull(PhoneNumbers.normalize("+0123456789"));
    }

    @Test
    void masksAndDerivesUsername() {
        assertEquals("+237 6•• ••• 456", PhoneNumbers.mask("+237699123456"));
        assertEquals("tel-237699123456", PhoneNumbers.username("+237699123456"));
        assertTrue(PhoneNumbers.mask("+33612345678").endsWith("678"));
    }
}
