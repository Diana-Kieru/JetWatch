package com.jetwatch

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import java.util.Properties

fun main() = application {
    val graph = JetWatchGraph(
        config = desktopConfig(),
        storeFile = File(System.getProperty("user.home"), ".jetwatch/store.json").absolutePath,
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "JetWatch",
    ) {
        App(graph)
    }
}

private fun desktopConfig(): JetWatchConfig {
    val properties = Properties()
    val file = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
        .map { File(it, "local.properties") }
        .firstOrNull { it.isFile }
    file?.inputStream()?.use(properties::load)
    fun value(name: String): String = System.getenv(name) ?: properties.getProperty(name).orEmpty()
    return JetWatchConfig(
        aeroDataBoxKey = value("AERODATABOX_API_KEY").trim(),
        openSkyClientId = value("OPENSKY_CLIENT_ID").trim(),
        openSkyClientSecret = value("OPENSKY_CLIENT_SECRET").trim(),
    )
}
