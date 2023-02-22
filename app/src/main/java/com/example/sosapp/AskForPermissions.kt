package com.example.sosapp

import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.sosapp.util.RequestState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AskForPermissions(
    mainViewModel: MainViewModel,
    multiplePermissionsState: MultiplePermissionsState
) {
    val permissionsState by mainViewModel.permissionState.collectAsState()

    if (permissionsState is RequestState.Success<*>) {
        if (multiplePermissionsState.shouldShowRationale.not() && (permissionsState as RequestState.Success<Boolean>).data) {
            AskWithSettings()
        } else {
            AskWithRequest(
                mainViewModel = mainViewModel,
                multiplePermissionsState = multiplePermissionsState
            )
        }
    }
}

@Composable
fun AskWithSettings() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val context = LocalContext.current
        Text(text = "This application cannot work without all the permissions!\nPlease allow all the permissions manually")
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                ContextCompat.startActivity(
                    context,
                    Intent(
                        ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", "com.example.sosapp", null)
                    ), null
                )
            }
        ) {
            Text("Open Settings")
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AskWithRequest(
    mainViewModel: MainViewModel,
    multiplePermissionsState: MultiplePermissionsState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            getTextToShowGivenPermissions(
                mainViewModel = mainViewModel,
                multiplePermissionsState.revokedPermissions,
                multiplePermissionsState.shouldShowRationale
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }) {
            Text("Request permissions")
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun getTextToShowGivenPermissions(
    mainViewModel: MainViewModel,
    permissions: List<PermissionState>,
    shouldShowRationale: Boolean
): String {
    val revokedPermissionsSize = permissions.size
    if (revokedPermissionsSize == 0) return ""

    val textToShow = StringBuilder().apply {
        append("The \n")
    }

    for (i in permissions.indices) {
        textToShow.append(permissions[i].permission)
        when {
            revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
                textToShow.append(", and \n")
            }
            i == revokedPermissionsSize - 1 -> {
                textToShow.append(" ")
            }
            else -> {
                textToShow.append(", \n")
            }
        }
    }
    textToShow.append(
        if (shouldShowRationale) {
            "\n\n(Rationale): "
        } else {
            "\n\n"
        }
    )
    textToShow.append(if (revokedPermissionsSize == 1) "Permission is" else "Permissions are")
    textToShow.append(
        if (shouldShowRationale) {
            mainViewModel.persistPermissionState(true)
            " important. Please grant all of them for the app to function properly."
        } else {
            " not granted.\nThe app cannot function without them."
        }
    )
    return textToShow.toString()
}