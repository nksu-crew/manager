package me.nekosu.aqnya

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.flutter.embedding.engine.FlutterEngineCache
import me.nekosu.aqnya.ui.screens.MainScreen
import me.nekosu.aqnya.ui.theme.NekosuTheme
import me.nekosu.aqnya.util.LocaleHelper

class MainActivity : ComponentActivity() {
    private val engine get() = FlutterEngineCache.getInstance().get("main_flutter_engine")

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase, LocaleHelper.savedLanguageTag(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NekosuTheme {
                MainScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        engine?.lifecycleChannel?.appIsResumed()
    }

    override fun onPause() {
        super.onPause()
        engine?.lifecycleChannel?.appIsInactive()
    }

    override fun onStop() {
        super.onStop()
        engine?.lifecycleChannel?.appIsPaused()
    }

    override fun onDestroy() {
        super.onDestroy()
        engine?.lifecycleChannel?.appIsDetached()
    }
}
