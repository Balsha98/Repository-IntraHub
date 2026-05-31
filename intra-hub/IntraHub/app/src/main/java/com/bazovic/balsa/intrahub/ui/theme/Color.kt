package com.bazovic.balsa.intrahub.ui.theme

import androidx.compose.ui.graphics.Color
import com.bazovic.balsa.intrahub.data.Sport

// ─── SECTION: IntraHub Palette ─── //
val OrangeRIT = Color(0xFFF76902)
val OrangeDark = Color(0xFFC24E00)
val OrangeTint = Color(0xFFFFEFE3)
val OrangeTint2 = Color(0xFFFFF8F2)

val Ink = Color(0xFF0A0A0A)
val Ink2 = Color(0xFF1A1A1A)
val Ink3 = Color(0xFF404040)
val Ink4 = Color(0xFF6B6B6B)
val Ink5 = Color(0xFF9B9B9B)

val Line = Color(0xFFE8E4DE)
val Line2 = Color(0xFFF0ECE6)
val Canvas = Color(0xFFFAF8F5)
val Surface = Color(0xFFFFFFFF)

val Win = Color(0xFF1F7A4D)
val WinTint = Color(0xFFE5F2EC)
val Loss = Color(0xFFB43A3A)
val LossTint = Color(0xFFF7E6E6)
val Tie = Color(0xFF8A6F2A)
val TieTint = Color(0xFFF5EFE0)

// ─── SECTION: Sport Accent Colors ─── //
val SportBasketballColor = Color(0xFFC95A2C)
val SportSoccerColor = Color(0xFF2C7A4D)
val SportVolleyballColor = Color(0xFFC29A2C)
val SportDodgeballColor = Color(0xFF7A3AAD)

val SportBasketballTint = Color(0xFFF7E8E1)
val SportSoccerTint = Color(0xFFE1ECE6)
val SportVolleyballTint = Color(0xFFF6F1E1)
val SportDodgeballTint = Color(0xFFECE3F4)

// ─── SECTION: Sport Color Extensions ─── //
val Sport.accentColor: Color get() = when (this) {
    Sport.Basketball -> SportBasketballColor
    Sport.Soccer -> SportSoccerColor
    Sport.Volleyball -> SportVolleyballColor
    Sport.Dodgeball -> SportDodgeballColor
}//accentColor

val Sport.tintColor: Color get() = when (this) {
    Sport.Basketball -> SportBasketballTint
    Sport.Soccer -> SportSoccerTint
    Sport.Volleyball -> SportVolleyballTint
    Sport.Dodgeball -> SportDodgeballTint
}//tintColor:

// ─── SECTION: Avatar Palette ─── //
val AvatarColors = listOf(
    Color(0xFFF76902),
    Color(0xFF2C7A4D),
    Color(0xFF3A4A6B),
    Color(0xFF7A3AAD),
    Color(0xFFC29A2C),
    Color(0xFF0A6B7A),
    Color(0xFFB43A3A),
)//AvatarColors

fun avatarColorFor(name: String): Color {
    val index = name.fold(0) {
        acc, c -> acc + c.code
    } % AvatarColors.size

    return AvatarColors[index]
}//avatarColorFor
