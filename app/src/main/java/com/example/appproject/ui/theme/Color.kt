package com.example.appproject.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Modern Travel Blue Palette
val PrimaryBlue = Color(0xFF0D5C91)
val PrimaryBlueDark = Color(0xFF063763)
val PrimaryBlueLight = Color(0xFF1E88E5)
val SecondaryCyan = Color(0xFF0284C7)
val AccentGold = Color(0xFFF59E0B)

// Preserved Legacy Names for Backward Compatibility
val GuestNavy = PrimaryBlueDark
val GuestCyan = Color(0xFF00A9C5)
val GuestGold = AccentGold

// Enriched Modern Travel Colors
val OceanBluePrimary = Color(0xFF0D5C91)
val DeepRoyalNavy = Color(0xFF0A4B7C)
val SkyCyanAccent = Color(0xFF0284C7)
val SoftAzure = Color(0xFF38BDF8)
val WarmAmberGold = Color(0xFFF59E0B)

// Status & Indicators
val ConfirmedEmerald = Color(0xFF10B981)
val ConfirmedContainer = Color(0xFFD1FAE5)
val PendingAmber = Color(0xFFF59E0B)
val PendingContainer = Color(0xFFFEF3C7)
val CancelledRose = Color(0xFFE11D48)
val CancelledContainer = Color(0xFFFFE4E6)

// Background & Surface
val TravelBackground = Color(0xFFF6F8FC)
val TravelSurface = Color(0xFFFFFFFF)
val TravelSurfaceVariant = Color(0xFFEBF3FA)
val TravelSurfaceGlass = Color(0xF0FFFFFF)
val TravelOutline = Color(0xFF94A3B8) // Darkened for crisp, visible borders & outlines
val TravelOutlineVariant = Color(0xFFCBD5E1)
val TravelTextPrimary = Color(0xFF0F172A)
val TravelTextSecondary = Color(0xFF475569)
val TravelTextMuted = Color(0xFF64748B)

// Gradients
val NavyStart = Color(0xFF0A4B7C)
val NavyEnd = Color(0xFF041C32)
val BlueGradientStart = Color(0xFF0D5C91)
val BlueGradientEnd = Color(0xFF1E88E5)

val GuestColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2F1FF),
    onPrimaryContainer = PrimaryBlueDark,
    secondary = SecondaryCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = AccentGold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF92400E),
    background = TravelBackground,
    onBackground = TravelTextPrimary,
    surface = TravelSurface,
    onSurface = TravelTextPrimary,
    surfaceVariant = TravelSurfaceVariant,
    onSurfaceVariant = TravelTextSecondary,
    outline = TravelOutline,
    outlineVariant = TravelOutlineVariant,
    error = CancelledRose,
    onError = Color.White
)
