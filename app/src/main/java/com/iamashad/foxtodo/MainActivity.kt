package com.iamashad.foxtodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.iamashad.foxtodo.ui.nav.AppNavGraph
import com.iamashad.foxtodo.ui.theme.FoxToDoTheme
import com.iamashad.foxtodo.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeState by themeViewModel.themeState.collectAsState()

            FoxToDoTheme(
                darkTheme = themeState.isDarkMode,
                dynamicColor = themeState.useDynamicColor
            ) {
                AppNavGraph(themeViewModel = themeViewModel)
            }
        }
    }
}
