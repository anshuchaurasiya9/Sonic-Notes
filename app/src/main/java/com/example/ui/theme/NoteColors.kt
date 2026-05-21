package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class NoteColor(
    val index: Int,
    val name: String,
    val light: Color,
    val dark: Color
)

object NoteColors {
    val list = listOf(
        NoteColor(0, "Default", Color.Transparent, Color.Transparent),
        NoteColor(1, "Rose", Color(0xFFFFD1D1), Color(0xFF5C2B2B)),
        NoteColor(2, "Peach", Color(0xFFFFE3C7), Color(0xFF5C3D1F)),
        NoteColor(3, "Yellow", Color(0xFFFFF9C4), Color(0xFF5C541F)),
        NoteColor(4, "Sage", Color(0xFFD1F2D1), Color(0xFF215C21)),
        NoteColor(5, "Mint", Color(0xFFD1F2EB), Color(0xFF1E5C4E)),
        NoteColor(6, "Sky", Color(0xFFD2E8FD), Color(0xFF1F3D5C)),
        NoteColor(7, "Lavender", Color(0xFFE5DDFD), Color(0xFF32285C)),
        NoteColor(8, "Lilac", Color(0xFFF3D3FC), Color(0xFF45225C)),
        NoteColor(9, "Cherry", Color(0xFFFCD1E6), Color(0xFF5C1E3C))
    )

    fun getColor(index: Int, isDark: Boolean): Color {
        val noteColor = list.getOrNull(index) ?: list[0]
        return if (index == 0) {
            Color.Transparent
        } else {
            if (isDark) noteColor.dark else noteColor.light
        }
    }
}
