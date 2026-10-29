package com.izimboa.livreur

import com.izimboa.livreur.domain.NextAction
import com.izimboa.livreur.domain.Status
import com.izimboa.livreur.domain.digits
import com.izimboa.livreur.domain.distance
import com.izimboa.livreur.domain.fcfa
import com.izimboa.livreur.domain.headingToPickup
import com.izimboa.livreur.domain.isActive
import com.izimboa.livreur.domain.nextAction
import com.izimboa.livreur.domain.realmRoles
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainTest {

    @Test fun uneSeuleActionParEtape() {
        assertEquals(NextAction.PICK_UP, nextAction(Status.ASSIGNED))
        assertEquals(NextAction.DEPART, nextAction(Status.PICKED_UP))
        assertEquals(NextAction.ENTER_CODE, nextAction(Status.IN_TRANSIT))
        assertEquals(NextAction.AFTER_FAILURE, nextAction(Status.FAILED))
        assertEquals(NextAction.NONE, nextAction(Status.DELIVERED))
    }

    @Test fun direction() {
        assertTrue(headingToPickup(Status.ASSIGNED))
        assertFalse(headingToPickup(Status.IN_TRANSIT))
        assertTrue(isActive(Status.FAILED))
        assertFalse(isActive(Status.DELIVERED))
    }

    @Test fun formats() {
        assertEquals("12 775 FCFA", fcfa(12775))
        assertEquals("650 FCFA", fcfa(650))
        assertEquals("600 m", distance(600))
        assertEquals("1,5 km", distance(1450))
        assertEquals("237699123456", digits("+237 6 99 12 34 56"))
        assertEquals("+237 6 99 12 34 56", com.izimboa.livreur.ui.formatPhone("+237699123456"))
        assertEquals("+33612345678", com.izimboa.livreur.ui.formatPhone("+33612345678"))
    }

    @Test fun rolesDuJetonKeycloak() {
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"sub":"x","realm_access":{"roles":["client","livreur"]}}""".toByteArray())
        assertEquals(setOf("client", "livreur"), realmRoles("h.$payload.s"))
        assertEquals(emptySet<String>(), realmRoles("pas-un-jeton"))
        assertEquals(emptySet<String>(), realmRoles(null))
    }
}
