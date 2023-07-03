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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import ir.erfansn.artouch.R
import ir.erfansn.artouch.ui.configuration.ConfigurationFragment.Companion.BLUETOOTH_PERMISSIONS
import ir.erfansn.artouch.ui.configuration.ConfigurationFragment.Companion.CAMERA_PERMISSION
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
    var snackbarHeight by remember { mutableStateOf(0.dp) }
    val paddingFromBottom by animateDpAsState(snackbarHeight)
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        snackbarHostState.currentSnackbarData?.dismiss()
    }
    Scaffold(
        snackbarHost = {
            val density = LocalDensity.current
            SnackbarHost(
                modifier = Modifier.onSizeChanged {
                    snackbarHeight = with(density) { it.height.toDp() }
                },
                hostState = snackbarHostState
            )
        },
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
            when (uiState) {
                ConfigurationUiState.DisableBluetooth -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PermissionsRequestButton(
                            permissions = listOf(*BLUETOOTH_PERMISSIONS),
                            onGranted = onPromptToEnableBluetooth,
                            onRationaleShow = {
                                scope.launch {
                                    snackbarHostState.showSnackbarImmediately(context.getString(R.string.bluetooth_permissions_rationale))
                                }
                            },
                            onPermanentlyDenied = {
                                scope.launch {
                                    val result = snackbarHostState.showSnackbarImmediately(
                                        message = context.getString(R.string.permanently_denied_permission_message),
                                        actionLabel = context.getString(R.string.ok),
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) context.openAppSettings()
                                }
                            }
                        ) {
                            Text(text = stringResource(R.string.turn_on_bluetooth))
                        }
                    } else {
                        Button(onClick = onPromptToEnableBluetooth) {
                            Text(text = stringResource(R.string.turn_on_bluetooth))
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
                                    snackbarHostState.showSnackbarImmediately(context.getString(R.string.bluetooth_permissions_rationale))
                                }
                            },
                            onPermanentlyDenied = {
                                scope.launch {
                                    val result = snackbarHostState.showSnackbarImmediately(
                                        message = context.getString(R.string.permanently_denied_permission_message),
                                        actionLabel = context.getString(R.string.ok),
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) context.openAppSettings()
                                }
                            }
                        ) {
                            Text(text = stringResource(R.string.request_bluetooth_permissions))
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
                            text = stringResource(R.string.advertising_mode),
                            style = MaterialTheme.typography.titleLarge
                        )

                        var selectedDeviceItem by rememberSaveable {
                            mutableStateOf<BluetoothDevice?>(null)
                        }
                        LaunchedEffect(selectedDeviceItem, bluetoothBondedDevices) {
                            if (selectedDeviceItem !in bluetoothBondedDevices) {
                                selectedDeviceItem = null
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            if (bluetoothBondedDevices.isEmpty()) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = stringResource(R.string.bonded_devices_list_is_empty)
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

                        var shouldShowDebuggingStuff by rememberSaveable { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = stringResource(R.string.show_debugging_stuff))
                            Switch(
                                checked = shouldShowDebuggingStuff,
                                onCheckedChange = {
                                    shouldShowDebuggingStuff = it
                                }
                            )
                        }

                        PermissionsRequestButton(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .padding(bottom = paddingFromBottom),
                            permissions = listOf(CAMERA_PERMISSION),
                            onGranted = {
                                selectedDeviceItem?.let {
                                    onNavigateToCameraFragment(shouldShowDebuggingStuff, it)
                                } ?: run {
                                    scope.launch {
                                        snackbarHostState.showSnackbarImmediately(message = context.getString(
                                            R.string.must_select_a_device)
                                        )
                                    }
                                }
                            },
                            onRationaleShow = {
                                scope.launch {
                                    snackbarHostState.showSnackbarImmediately(context.getString(R.string.camera_permissions_rationale))
                                }
                            },
                            onPermanentlyDenied = {
                                scope.launch {
                                    val result = snackbarHostState.showSnackbarImmediately(
                                        message = context.getString(R.string.permanently_denied_permission_message),
                                        actionLabel = context.getString(R.string.ok),
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) context.openAppSettings()
                                }
                            }
                        ) {
                            Text(text = stringResource(R.string.start_ar_touch))
                        }
                    }
                }
            }
        }
    }
}

suspend fun SnackbarHostState.showSnackbarImmediately(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = withDismissAction,
        duration = duration,
    )
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
        onRationaleShow = { onRationaleShow() },
        onPermanentlyDenied = { onPermanentlyDenied() }
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
    onRationaleShow: (permissions: List<String>) -> Unit = { },
    onPermanentlyDenied: (permissions: List<String>) -> Unit = { },
): ManagedActivityResultLauncher<Array<String>, *> {
    val activity = LocalContext.current.findActivity()
    val permissionsStatusDetermined = remember {
        activity.getSharedPreferences("permissions_status_determined", Context.MODE_PRIVATE)
    }

    operator fun SharedPreferences.set(keys: List<String>, value: Boolean) = edit(commit = true) {
        keys.forEach { putBoolean(it, value) }
    }

    operator fun SharedPreferences.get(keys: List<String>, default: Boolean = false) =
        keys.isNotEmpty() && keys.all { getBoolean(it, default) }

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionsResult ->
        val result = permissionsResult.mapValues { (_, isGranted) -> PermissionStatus(isGranted) }
        val permissions = result.keys.toList()
        val permissionsStatus = result.values.toList()

        when {
            permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            } -> {
                permissionsStatusDetermined[permissions] = true
                onRationaleShow(permissions.filter { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) })
            }

            permissionsStatusDetermined[permissions] && permissionsStatus.any {
                !it.isGranted
            } -> {
                permissionsStatusDetermined[permissions] = true
                onPermanentlyDenied(result.filterValues { !it.isGranted }.map { it.key })
            }

            // When requests are repeatedly sent the result maybe be empty
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
