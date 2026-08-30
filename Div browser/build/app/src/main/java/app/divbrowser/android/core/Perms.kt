package app.divbrowser.android.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat

/**
 * Phase 14 — soft permission helpers (request only when feature needs it).
 */
object Perms {

    val camera = Manifest.permission.CAMERA
    val mic = Manifest.permission.RECORD_AUDIO
    val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
    val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION

    fun notificationsOrNull(): String? =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null

    fun has(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun hasAll(context: Context, permissions: Array<String>): Boolean =
        permissions.all { has(context, it) }

    /** Permissions WebView media / geo often need. */
    fun webMediaPermissions(): Array<String> = arrayOf(camera, mic)

    fun locationPermissions(): Array<String> = arrayOf(fineLocation, coarseLocation)
}

/**
 * Compose helper — call [launch] when user taps allow (never on cold start spam).
 */
class SoftPermissionRequester(
    private val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
) {
    fun launch(permissions: Array<String>) {
        if (permissions.isEmpty()) return
        launcher.launch(permissions)
    }

    fun launchCameraMic() = launch(Perms.webMediaPermissions())
    fun launchLocation() = launch(Perms.locationPermissions())
}

@Composable
fun rememberSoftPermissionRequester(
    onResult: (Map<String, Boolean>) -> Unit = {},
): SoftPermissionRequester {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = onResult,
    )
    return remember(launcher) { SoftPermissionRequester(launcher) }
}
