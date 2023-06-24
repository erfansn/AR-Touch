package ir.erfansn.artouch.ui.configuration

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import ir.erfansn.artouch.ui.configuration.ConfigurationFragment.Companion.BLUETOOTH_PERMISSIONS
import ir.erfansn.artouch.ui.configuration.ConfigurationFragment.Companion.CAMERA_PERMISSION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun ConfigurationScreen(
    uiState: ConfigurationUiState,
    onNavigateToCameraFragment: (Boolean, BluetoothDevice) -> Unit = { _, _ -> },
    onStartArTouchAdvertiser: () -> Unit = { },
    onPromptToEnableBluetooth: () -> Unit = { },
    bluetoothBondedDevices: List<BluetoothDevice> = emptyList(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeContent,
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val noQueueSnackbarShower = remember { NoQueueSnackbarShower(scope, snackbarHostState) }
            LaunchedEffect(uiState) {
                noQueueSnackbarShower.cancelImmediately()
            }
            when (uiState) {
                ConfigurationUiState.DisableBluetooth -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PermissionsRequestButton(
                            permissions = listOf(*BLUETOOTH_PERMISSIONS),
                            onGranted = onPromptToEnableBluetooth,
                            onRationaleShow = {
                                scope.launch {
                                    noQueueSnackbarShower.showSnackbar("This permission is necessary")
                                }
                            },
                            onPermanentlyDenied = {
                                scope.launch {
                                    val result = noQueueSnackbarShower.showSnackbar(
                                        message = "Go to app setting to enable it",
                                        actionLabel = "OK",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) context.openAppSettings()
                                }
                            }
                        ) {
                            Text(text = "Turn on Bluetooth")
                        }
                    } else {
                        Button(onClick = onPromptToEnableBluetooth) {
                            Text(text = "Turn on Bluetooth")
                        }
                    }
                }

                ConfigurationUiState.EnableBluetooth -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PermissionsRequestButton(
                            permissions = listOf(*BLUETOOTH_PERMISSIONS),
                            onGranted = onStartArTouchAdvertiser,
                            onRationaleShow = {
                                scope.launch {
                                    noQueueSnackbarShower.showSnackbar("This permission is necessary")
                                }
                            },
                            onPermanentlyDenied = {
                                scope.launch {
                                    val result = noQueueSnackbarShower.showSnackbar(
                                        message = "Go to app setting to enable it",
                                        actionLabel = "OK",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) context.openAppSettings()
                                }
                            }
                        ) {
                            Text(text = "Request Bluetooth permissions")
                        }
                    } else {
                        LaunchedEffect(Unit) {
                            onStartArTouchAdvertiser()
                        }
                    }
                }

                ConfigurationUiState.AdvertisingMode -> {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 16.dp),
                            text = "Advertising Mode",
                            style = MaterialTheme.typography.titleLarge
                        )

                        var selectedDeviceItem by rememberSaveable {
                            mutableStateOf<BluetoothDevice?>(null)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            if (bluetoothBondedDevices.isEmpty()) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = "Bonded devices list is empty!"
                                )
                            } else {
                                LazyColumn {
                                    items(bluetoothBondedDevices) {
                                        ListItem(
                                            modifier = Modifier
                                                .clickable { selectedDeviceItem = it }
                                                .fillParentMaxWidth()
                                                .background(color = Color.Red),
                                            leadingContent = {
                                                RadioButton(
                                                    selected = selectedDeviceItem == it,
                                                    onClick = null
                                                )
                                            },
                                            headlineContent = {
                                                Text(text = it.name)
                                            },
                                            overlineContent = {
                                                Text(text = it.address)
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        var shouldShowDebuggingStuff by rememberSaveable { mutableStateOf(true) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Show debugging stuff")
                            Switch(
                                checked = shouldShowDebuggingStuff,
                                onCheckedChange = {
                                    shouldShowDebuggingStuff = it
                                }
                            )
                        }

                        var snackbarShowed by remember { mutableStateOf(false) }
                        val paddingFromBottom by animateDpAsState(targetValue = if (snackbarShowed) 52.dp else 0.dp)
                        PermissionsRequestButton(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .padding(bottom = paddingFromBottom),
                            permissions = listOf(CAMERA_PERMISSION),
                            onGranted = {
                                if (selectedDeviceItem !in bluetoothBondedDevices) {
                                    scope.launch {
                                        snackbarShowed = true
                                        noQueueSnackbarShower.showSnackbar(message = "You want to connect to anything?")
                                        snackbarShowed = false
                                    }
                                    return@PermissionsRequestButton
                                }
                                selectedDeviceItem?.let {
                                    onNavigateToCameraFragment(shouldShowDebuggingStuff, it)
                                } ?: run {
                                    scope.launch {
                                        snackbarShowed = true
                                        noQueueSnackbarShower.showSnackbar(message = "Must select a device")
                                        snackbarShowed = false
                                    }
                                }
                            },
                            onRationaleShow = {
                                scope.launch {
                                    snackbarShowed = true
                                    noQueueSnackbarShower.showSnackbar("This permission is necessary")
                                    snackbarShowed = false
                                }
                            },
                            onPermanentlyDenied = {
                                scope.launch {
                                    snackbarShowed = true
                                    val result = noQueueSnackbarShower.showSnackbar(
                                        message = "Go to app setting to enable it",
                                        actionLabel = "OK",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) context.openAppSettings()
                                    snackbarShowed = false
                                }
                            }
                        ) {
                            Text(text = "Start AR Touch")
                        }
                    }
                }
            }
        }
    }
}

class NoQueueSnackbarShower(
    private val scope: CoroutineScope,
    private val snackbarHostState: SnackbarHostState,
) {
    private var deferredSnackbarResult: Deferred<SnackbarResult>? = null

    suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration =
            if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
    ): SnackbarResult {
        cancelImmediately()
        deferredSnackbarResult = scope.async {
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = withDismissAction,
                duration = duration,
            )
        }
        return deferredSnackbarResult?.await() ?: throw IllegalStateException()
    }

    fun cancelImmediately() {
        deferredSnackbarResult?.cancel()
        deferredSnackbarResult = null
    }
}

fun Context.openAppSettings() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts("package", packageName, null))
        .also(::startActivity)
}

@Composable
fun PermissionsRequestButton(
    permissions: List<String>,
    onGranted: () -> Unit,
    modifier: Modifier = Modifier,
    onRationaleShow: () -> Unit = { },
    onPermanentlyDenied: () -> Unit = { },
    content: @Composable RowScope.() -> Unit,
) {
    val permissionsRequest = rememberPermissionsRequestLauncher(
        onGranted = onGranted,
        onRationaleShow = onRationaleShow,
        onPermanentlyDenied = onPermanentlyDenied
    )
    Button(
        modifier = modifier,
        onClick = { permissionsRequest.launch(permissions.toTypedArray()) },
        content = content,
    )
}

@Composable
fun rememberPermissionsRequestLauncher(
    onGranted: () -> Unit,
    onRationaleShow: () -> Unit = { },
    onPermanentlyDenied: () -> Unit = { },
): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> {
    val activity = LocalContext.current.findActivity()
    val permissionsStatusDetermined = remember {
        activity.getSharedPreferences("permissions_status_determined", Context.MODE_PRIVATE)
    }

    operator fun SharedPreferences.set(keys: List<String>, value: Boolean) = edit(commit = true) {
        keys.forEach { putBoolean(it, value) }
    }

    operator fun SharedPreferences.get(keys: List<String>, default: Boolean = false) =
        keys.isNotEmpty() && keys.all { getBoolean(it, default) }

    var shownRationale by remember { mutableStateOf(false) }
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        val result = it.mapValues { (_, isGranted) -> PermissionStatus(isGranted) }
        val permissions = result.keys.toList()
        val permissionsStatus = result.values.toList()

        when {
            permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            } -> {
                onRationaleShow()
                shownRationale = true
            }

            (shownRationale || permissionsStatusDetermined[permissions]) && permissionsStatus.any {
                !it.isGranted
            } -> {
                permissionsStatusDetermined[permissions] = true
                onPermanentlyDenied()
            }

            // When requests are repeatedly sent the result list maybe sent empty
            permissionsStatus.isNotEmpty() && permissionsStatus.all(PermissionStatus::isGranted) -> {
                permissionsStatusDetermined[permissions] = true
                onGranted()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfigurationScreenPreview() {
    MaterialTheme {
        ConfigurationScreen(
            uiState = ConfigurationUiState.AdvertisingMode
        )
    }
}

@JvmInline
value class PermissionStatus(
    val isGranted: Boolean,
)

private tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> throw IllegalStateException()
}
