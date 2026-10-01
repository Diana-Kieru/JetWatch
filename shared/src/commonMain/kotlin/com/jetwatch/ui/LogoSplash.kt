package com.jetwatch.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.jetwatch.resources.Res
import com.jetwatch.resources.jetwatch_logo
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private val SplashBackground = Color(0xFFF7F9FB)

@Composable
fun LogoSplash(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1400)
        onFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.jetwatch_logo),
            contentDescription = "JetWatch",
            modifier = Modifier.fillMaxWidth(0.86f),
            contentScale = ContentScale.Fit,
        )
    }
}
