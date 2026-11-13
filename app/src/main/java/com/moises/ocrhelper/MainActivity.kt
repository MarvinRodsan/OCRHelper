package com.moises.ocrhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moises.ocrhelper.ui.OCRHelperNavHost
import com.moises.ocrhelper.ui.theme.OCRHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OCRHelperTheme {
                OCRHelperNavHost()
            }
        }
    }
}
