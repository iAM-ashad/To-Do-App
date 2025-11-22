package com.iamashad.foxtodo.ui.theme

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ThemeState(
    val isDarkMode: Boolean = false,
    val useDynamicColor: Boolean = false
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    // later you can inject DataStore / repository here
) : ViewModel() {

    private val _themeState = MutableStateFlow(ThemeState())
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        _themeState.update { it.copy(isDarkMode = enabled) }
        // TODO: persist to DataStore
    }

    fun setDynamicColor(enabled: Boolean) {
        _themeState.update { it.copy(useDynamicColor = enabled) }
        // TODO: persist to DataStore
    }
}