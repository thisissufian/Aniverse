package com.aniverse.app.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aniverse.app.data.local.AppPreferences
import com.aniverse.app.data.remote.GoogleDriveSyncManager
import com.aniverse.app.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appPrefs = remember { AppPreferences.getInstance(context) }
    val syncManager = remember { GoogleDriveSyncManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    var autoSkipIntro by remember { mutableStateOf(appPrefs.autoSkipIntro) }
    var autoPlayNext by remember { mutableStateOf(appPrefs.autoPlayNext) }
    var preferDub by remember { mutableStateOf(appPrefs.preferDub) }

    var googleAccount by remember { mutableStateOf(syncManager.getSignedInAccount()) }
    var isSyncing by remember { mutableStateOf(false) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            googleAccount = account
            scope.launch {
                isSyncing = true
                syncManager.syncNow()
                isSyncing = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Header
        Text(
            text = "Settings & Preferences",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Playback Card
        Text(
            text = "PLAYBACK CONTROLS",
            color = PrimaryViolet,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingToggleItem(
                    icon = Icons.Filled.FastForward,
                    title = "Auto-Skip Intro & Outro",
                    subtitle = "Instantly skips OP & ED when timestamps are detected",
                    checked = autoSkipIntro,
                    onCheckedChange = {
                        autoSkipIntro = it
                        appPrefs.autoSkipIntro = it
                    }
                )

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 12.dp))

                SettingToggleItem(
                    icon = Icons.Filled.SkipNext,
                    title = "Auto-Play Next Episode",
                    subtitle = "Seamless binge-watching without touching the screen",
                    checked = autoPlayNext,
                    onCheckedChange = {
                        autoPlayNext = it
                        appPrefs.autoPlayNext = it
                    }
                )

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 12.dp))

                SettingToggleItem(
                    icon = Icons.Filled.Language,
                    title = "Prefer English Dub",
                    subtitle = "Default to English audio when available",
                    checked = preferDub,
                    onCheckedChange = {
                        preferDub = it
                        appPrefs.preferDub = it
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cloud Sync Card
        Text(
            text = "CLOUD SYNC (CROSS-DEVICE)",
            color = PrimaryViolet,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudSync,
                            contentDescription = null,
                            tint = AccentCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google Drive Sync",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (googleAccount != null) "Connected as ${googleAccount?.email}" else "Sync exact seconds, watch history & watchlists with Desktop",
                            color = if (googleAccount != null) AccentCyan else TextMuted,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    if (googleAccount != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "CONNECTED",
                                color = AccentCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (googleAccount != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isSyncing = true
                                    syncManager.syncNow()
                                    isSyncing = false
                                }
                            },
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSyncing) "Syncing..." else "Sync Now",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                syncManager.getSignInClient().signOut().addOnCompleteListener {
                                    googleAccount = null
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                        ) {
                            Text("Disconnect", fontSize = 12.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            signInLauncher.launch(syncManager.getSignInClient().signInIntent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect Google Drive", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Card
        Text(
            text = "ABOUT ANIVERSE",
            color = PrimaryViolet,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Aniverse Native Android",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Version 1.0.0 (Jetpack Compose + ExoPlayer)",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Built for 100% ad-free, 60fps buttery-smooth playback with hardware-accelerated video decoding.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryViolet,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryViolet,
                uncheckedTrackColor = DarkCard,
                uncheckedThumbColor = TextMuted
            )
        )
    }
}
