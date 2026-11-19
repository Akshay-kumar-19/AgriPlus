package com.example.agriplus.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** Shared colors for the screenshot's navy and teal visual style. */
object AppPalette {
    var dark by mutableStateOf(true)

    val background: Color get() = if (dark) Color(0xFF0F2027) else Color(0xFFEAF4FF)
    val backgroundAlt: Color get() = if (dark) Color(0xFF2C5364) else Color(0xFFDCEBFA)
    val surface: Color get() = if (dark) Color(0xFF1B2B34) else Color(0xFFFFFFFF)
    val surfaceAlt: Color get() = if (dark) Color(0xFF193A46) else Color(0xFFF3F9FF)
    val field: Color get() = if (dark) Color(0xFF142935) else Color(0xFFF7FBFF)
    val text: Color get() = if (dark) Color(0xFFF7FAFC) else Color(0xFF102C46)
    val muted: Color get() = if (dark) Color(0xFFC4CDD7) else Color(0xFF4B647C)
    val blue: Color get() = if (dark) Color(0xFF42A5F5) else Color(0xFF1976D2)
    val teal: Color get() = if (dark) Color(0xFF26C6DA) else Color(0xFF008A9A)
    val accent: Color get() = if (dark) Color(0xFF81C784) else Color(0xFF388E3C)
    val accentText: Color get() = if (dark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
    val line: Color get() = if (dark) Color(0xFF38515D) else Color(0xFFBDD2E4)
    val danger: Color get() = if (dark) Color(0xFFFF6B6B) else Color(0xFFC62828)
    val dangerContainer: Color get() = if (dark) Color(0xFF3B2027) else Color(0xFFFFF0EF)
    val warning: Color get() = if (dark) Color(0xFFFFB74D) else Color(0xFFB85C00)
}

@Composable
fun AppOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppPalette.text,
    unfocusedTextColor = AppPalette.text,
    disabledTextColor = AppPalette.muted,
    cursorColor = AppPalette.blue,
    focusedBorderColor = AppPalette.blue,
    unfocusedBorderColor = AppPalette.line,
    disabledBorderColor = AppPalette.line,
    focusedLabelColor = AppPalette.blue,
    unfocusedLabelColor = AppPalette.muted,
    disabledLabelColor = AppPalette.muted,
    focusedPlaceholderColor = AppPalette.muted,
    unfocusedPlaceholderColor = AppPalette.muted,
    focusedContainerColor = AppPalette.field,
    unfocusedContainerColor = AppPalette.field,
    disabledContainerColor = AppPalette.field
)
