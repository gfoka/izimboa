package com.izimboa.livreur

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.izimboa.livreur.data.Delivery
import com.izimboa.livreur.data.Offer
import com.izimboa.livreur.data.OfferDelivery
import com.izimboa.livreur.data.Pricing
import com.izimboa.livreur.data.Stop
import com.izimboa.livreur.domain.Status
import com.izimboa.livreur.ui.DeliveryScreen
import com.izimboa.livreur.ui.NotCourierScreen
import com.izimboa.livreur.ui.HomeScreen
import com.izimboa.livreur.ui.IzimboaTheme
import com.izimboa.livreur.ui.LoginScreen
import com.izimboa.livreur.ui.OfferScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Rendu des écrans principaux (téléphone 6,5"), pour relire l'interface sans appareil. Images dans build/screens/. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-xxhdpi", application = android.app.Application::class)
class ScreensScreenshotTest {

    @get:Rule val compose = createComposeRule()

    private fun shot(name: String, content: @Composable () -> Unit) {
        compose.setContent { IzimboaTheme(content) }
        compose.onRoot().captureRoboImage("build/screens/$name.png")
    }

    private val delivery = Delivery(
        id = "d1", reference = "IZ-7K3P9Q", status = Status.ASSIGNED,
        pickup = Stop(4.0511, 9.7679, "Rue Joss, Akwa", "Face à la pharmacie du Port"), pickupPhone = "+237699000001",
        dropoff = Stop(4.088, 9.75, "Bonamoussadi, carrefour Kotto", "Derrière la boulangerie, portail bleu"),
        recipientName = "Awa Ngo", recipientPhone = "+237677123456", packageDescription = "Une paire de chaussures",
        express = true, distanceKm = 6.2, pricing = Pricing(15000, 2225, "DEDUCTED_FROM_COD"),
    )

    @Test fun connexion() = shot("1-connexion") { LoginScreen(onLogin = {}, message = null) }
    @Test fun nonActive() = shot("1b-non-active") { NotCourierScreen(name = null, onLogout = {}, onRetry = {}, phone = "+237699123456") }
    @Test fun horsLigne() = shot("2-hors-ligne") { HomeScreen("Jean", false, false, null, {}, {}, {}) }
    @Test fun enLigne() = shot("3-en-ligne") { HomeScreen("Jean", true, false, null, {}, {}, {}) }

    @Test fun course() = shot("4-course-proposee") {
        OfferScreen(
            Offer("o1", 24, 450, 975, OfferDelivery("d1", "IZ-7K3P9Q", true, 4.05, 9.76, "Rue Joss, Akwa",
                "Face à la pharmacie du Port", "Bonamoussadi, carrefour Kotto", "Derrière la boulangerie", 6200, 1500, 15000)),
            receivedAt = System.currentTimeMillis(), busy = false, onAccept = {}, onDecline = {}, onExpired = {},
        )
    }

    @Test fun ramassage() = shot("5-ramassage") { DeliveryScreen(delivery, false, null, {}, {}, {}, {}, {}, {}, {}, {}) }
    @Test fun enRoute() = shot("6-en-route") { DeliveryScreen(delivery.copy(status = Status.IN_TRANSIT), false, null, {}, {}, {}, {}, {}, {}, {}, {}) }
    @Test fun livree() = shot("7-livree") { DeliveryScreen(delivery.copy(status = Status.DELIVERED), false, null, {}, {}, {}, {}, {}, {}, {}, {}) }
}
