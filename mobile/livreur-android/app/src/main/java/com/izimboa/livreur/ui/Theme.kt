package com.izimboa.livreur.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Couleurs Izimboa, contrastes forts pour la lecture en plein soleil. */
object Brand {
    val Navy = Color(0xFF0B2545)
    val Navy2 = Color(0xFF13345E)
    val Sun = Color(0xFFF28C28)
    val SunSoft = Color(0xFFFFF3E6)
    val Cloud = Color(0xFFF4F6F9)
    val Line = Color(0xFFE3E8EF)
    val Slate = Color(0xFF4A5A70)
    val Ink = Color(0xFF14243A)
    val Go = Color(0xFF14894E)       // « Accepter », « En ligne »
    val GoSoft = Color(0xFFE6F5EC)
    val Stop = Color(0xFFB42318)
    val StopSoft = Color(0xFFFDECEA)
    val WhatsApp = Color(0xFF128C4A)
    val Offline = Color(0xFF8A96A8)
}

private val colors = lightColorScheme(
    primary = Brand.Navy,
    onPrimary = Color.White,
    secondary = Brand.Sun,
    onSecondary = Brand.Navy,
    background = Brand.Cloud,
    onBackground = Brand.Ink,
    surface = Color.White,
    onSurface = Brand.Ink,
    error = Brand.Stop,
)

private val type = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 52.sp, lineHeight = 56.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Black, fontSize = 40.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 23.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, letterSpacing = 0.5.sp),
)

@Composable
fun IzimboaTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = colors, typography = type, content = content)
