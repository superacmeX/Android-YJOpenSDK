package com.superacme.biz_bind.ui.components

import android.Manifest
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionWrapper(content: @Composable () -> Unit) {
//    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    val permissionState = rememberPermissionState(permission = cameraPermission)

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    if (permissionState.status.isGranted) {
        content()
    } else {
        Text("Camera permission is required to use this feature.")
    }
}
