package com.gcoded.meditrack.domain.model

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

data class UserPreferences(
    val userName: String = "",
    val onboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true
)