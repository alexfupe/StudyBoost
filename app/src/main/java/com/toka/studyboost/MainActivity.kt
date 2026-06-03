package com.toka.studyboost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.toka.studyboost.interfaz.NavegacionPrincipal
import com.toka.studyboost.ui.theme.StudyBoostTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyBoostTheme {
                NavegacionPrincipal()
            }
        }
    }
}
