package com.izimboa.keycloak.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WhatsAppClientTest {

    private final WhatsAppClient client = new WhatsAppClient("http://localhost", "t", "42", "izimboa_code");

    @Test
    void buildsAuthenticationTemplatePayload() {
        String p = client.payload("+237699123456", "123456", "fr");
        assertTrue(p.contains("\"to\":\"237699123456\""));
        assertTrue(p.contains("\"name\":\"izimboa_code\""));
        assertTrue(p.contains("\"language\":{\"code\":\"fr\"}"));
        assertEquals(2, p.split("\"text\":\"123456\"", -1).length - 1, "code dans le texte et dans le bouton");
        assertTrue(p.contains("\"sub_type\":\"url\""));
    }

    @Test
    void fallsBackToFrench() {
        assertTrue(client.payload("+237699123456", "1", "de").contains("\"code\":\"fr\""));
        assertTrue(client.payload("+237699123456", "1", "en").contains("\"code\":\"en\""));
    }

    @Test
    void notConfiguredWithoutToken() {
        assertFalse(new WhatsAppClient("x", "", "1", "t").configured());
        assertFalse(new WhatsAppClient("x", "", "1", "t").sendCode("+237699123456", "123456", "fr"));
    }

    @Test
    void hashDependsOnPhoneAndCode() {
        assertEquals(WhatsAppOtpAuthenticator.hash("+1", "123456"), WhatsAppOtpAuthenticator.hash("+1", "123456"));
        assertFalse(WhatsAppOtpAuthenticator.hash("+1", "123456").equals(WhatsAppOtpAuthenticator.hash("+2", "123456")));
    }
}
