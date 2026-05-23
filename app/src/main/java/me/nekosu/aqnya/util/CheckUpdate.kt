package me.nekosu.aqnya.util

import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import me.nekosu.aqnya.BuildConfig
import me.nekosu.aqnya.util.UpdateChecker


@Composable
fun CheckUpdate(
    owner: String,
    repo: String,
) {
    val context = LocalContext.current
    var showUpdateDialog by remember { mutableStateOf(false) }
    var latestTag by remember { mutableStateOf<String?>(null) }

    fun stripSuffix(version: String): String = version.trimStart('v', 'V').substringBefore('-')

    fun parseNumbers(version: String): List<Int> =
        stripSuffix(version)
            .split('.')
            .map { it.toIntOrNull() ?: 0 }
            .let {
                when {
                    it.size >= 3 -> it.take(3)
                    it.size == 2 -> it + listOf(0)
                    it.size == 1 -> it + listOf(0, 0)
                    else -> listOf(0, 0, 0)
                }
            }

    fun isRemoteGreater(
        local: String,
        remote: String,
    ): Boolean {
        val localNums = parseNumbers(local)
        val remoteNums = parseNumbers(remote)
        for (i in 0..2) {
            if (remoteNums[i] > localNums[i]) return true
            if (remoteNums[i] < localNums[i]) return false
        }
        return false
    }

    LaunchedEffect(Unit) {
        UpdateChecker.fetchLatestVersion(owner, repo)?.let { remoteVer ->
            latestTag = remoteVer
            if (isRemoteGreater(BuildConfig.VERSION_NAME, remoteVer)) {
                showUpdateDialog = true
            }
        }
    }

    if (showUpdateDialog && latestTag != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("检测到新版本") },
            text = {
                Text(
                    "当前版本：${BuildConfig.VERSION_NAME}\n" +
                        "最新版本：$latestTag\n\n" +
                        "针对你的牛牛进行了一些优化，是否前往 GitHub 下载？",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showUpdateDialog = false
                    val intent =
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/$owner/$repo/releases/latest".toUri(),
                        )
                    context.startActivity(intent)
                }) { Text("去下载") }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("稍后再说")
                }
            },
        )
    }
}
