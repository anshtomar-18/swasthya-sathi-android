package com.swasthyasathi.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Emerald, Green & Pure White Color Scheme
// ==========================================

// Primary Emerald Palette
val EmeraldPrimary = Color(0xFF047857)        // Deep rich clinical emerald (700)
val EmeraldDark = Color(0xFF064E3B)           // Darkest forest emerald (900)
val EmeraldMedium = Color(0xFF059669)         // Vibrant emerald (600)
val EmeraldLight = Color(0xFF10B981)          // Fresh emerald mint (500)
val EmeraldMint = Color(0xFF34D399)           // Soft bright mint (400)
val EmeraldContainer = Color(0xFFE6F7ED)      // Gentle emerald tint container (50/100)
val EmeraldContainerDark = Color(0xFF065F46)  // 800
val EmeraldFixed = Color(0xFFA7F3D0)          // Mint 200
val EmeraldDim = Color(0xFF6EE7B7)            // Mint 300

// Aliases for compatibility
val PrimaryTeal = EmeraldPrimary
val PrimaryTealContainer = EmeraldMedium
val PrimaryTealFixed = EmeraldFixed
val PrimaryTealDim = EmeraldDim

// Clean Pure Whites & Soft Emerald Surfaces
val AppSurface = Color(0xFFFFFFFF)                    // Pure clean white
val SurfaceContainerLowest = Color(0xFFFFFFFF)        // Pure white card background
val SurfaceContainerLow = Color(0xFFF3FCF7)           // Very faint emerald-white
val SurfaceContainer = Color(0xFFE8F8F0)              // Soft emerald surface
val SurfaceContainerHigh = Color(0xFFD8F3E5)          // Mild emerald divider/border
val SurfaceContainerHighest = Color(0xFFBFF0D7)       // Faint mint tint
val OnSurface = Color(0xFF064E3B)                     // Deepest emerald text
val OnSurfaceVariant = Color(0xFF0F5B46)              // Muted deep emerald text
val AppOutline = Color(0xFF4B7A6A)                    // Gentle emerald-slate outline

// Secondary / Accent Colors (Emerald & Warm Alert Accent)
val SecondaryCoral = Color(0xFFE11D48)                // Rose / Crimson for SOS Emergency
val SecondaryCoralContainer = Color(0xFFBE123C)
val SecondaryCoralFixed = Color(0xFFFFE4E6)

// Tertiary Navy / Teal (Kept as lush blue-green)
val TertiaryNavy = Color(0xFF0D9488)                  // Teal-emerald
val TertiaryNavyContainer = Color(0xFF115E59)
val TertiaryBlue = TertiaryNavy
val TertiaryBlueContainer = TertiaryNavyContainer

// Semantic Risk Level Colors (Health Guidelines)
val RiskLow = Color(0xFF059669)                       // Clean Emerald
val RiskLowBg = Color(0xFFECFDF5)                     // Soft Emerald Mint
val RiskModerate = Color(0xFFD97706)                  // Amber Caution
val RiskModerateBg = Color(0xFFFEF3C7)                // Soft Amber
val RiskHigh = Color(0xFFEA580C)                      // Orange Warning
val RiskHighBg = Color(0xFFFFEDD5)                    // Soft Orange
val RiskCritical = Color(0xFFDC2626)                  // Crimson Emergency
val RiskCriticalBg = Color(0xFFFEE2E2)                // Soft Crimson
