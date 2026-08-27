package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DispatchMode
import com.example.data.model.EngineState
import com.example.ui.theme.BrightAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningYellow
import com.example.ui.viewmodel.SmsBlastViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlasterScreen(
    viewModel: SmsBlastViewModel,
    onRequestSmsPermission: () -> Unit,
    hasSmsPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val engineState by viewModel.engineState.collectAsStateWithLifecycle()
    val currentProgress by viewModel.currentProgress.collectAsStateWithLifecycle()
    val totalProgress by viewModel.totalProgress.collectAsStateWithLifecycle()
    val successCount by viewModel.successCount.collectAsStateWithLifecycle()
    val failedCount by viewModel.failedCount.collectAsStateWithLifecycle()
    val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()

    var showContactPicker by remember { mutableStateOf(false) }
    var showSenderIdDialog by remember { mutableStateOf(false) }
    var showMaskInfoDialog by remember { mutableStateOf(false) }

    val terminalListState = rememberLazyListState()

    LaunchedEffect(terminalLogs.size) {
        if (terminalLogs.isNotEmpty()) {
            terminalListState.animateScrollToItem(terminalLogs.size - 1)
        }
    }

    val isRunning = engineState == EngineState.RUNNING
    val isPaused = engineState == EngineState.PAUSED
    val isBusy = isRunning || isPaused

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val currentSpeedMsgPerSec = (1.0f / config.delaySeconds.coerceAtLeast(0.05f))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // TOP TELEMETRY STATUS BANNER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isRunning) NeonOrange else DarkSurfaceBorder,
                        RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (engineState) {
                                            EngineState.RUNNING -> TerminalGreen
                                            EngineState.PAUSED -> WarningYellow
                                            EngineState.COMPLETED -> ElectricCyan
                                            EngineState.ERROR -> TerminalRed
                                            else -> TextSecondaryDark
                                        }
                                    )
                                    .then(if (isRunning) Modifier.scale(pulseScale) else Modifier)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (engineState) {
                                    EngineState.RUNNING -> "TURBO BLAST ACTIVE ⚡"
                                    EngineState.PAUSED -> "DISPATCH PAUSED"
                                    EngineState.COMPLETED -> "BATCH COMPLETED 🏁"
                                    EngineState.STOPPED -> "DISPATCH STOPPED"
                                    EngineState.ERROR -> "DISPATCH ERROR"
                                    EngineState.IDLE -> "TURBO ENGINE READY"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = when (engineState) {
                                    EngineState.RUNNING -> TerminalGreen
                                    EngineState.PAUSED -> WarningYellow
                                    EngineState.COMPLETED -> ElectricCyan
                                    EngineState.ERROR -> TerminalRed
                                    else -> TextSecondaryDark
                                }
                            )
                        }

                        // Speed Indicator Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = NeonOrange.copy(alpha = 0.2f),
                            modifier = Modifier.border(1.dp, NeonOrange.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.0f", currentSpeedMsgPerSec)} msg/sec",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonOrange
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    val progressRatio = if (totalProgress > 0) {
                        (currentProgress.toFloat() / totalProgress.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when (config.dispatchMode) {
                            DispatchMode.CLOUD_ANONYMOUS -> TerminalGreen
                            DispatchMode.REAL_SIM -> NeonOrange
                            DispatchMode.SANDBOX -> ElectricCyan
                        },
                        trackColor = DarkSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Statistics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "Progress",
                            value = "$currentProgress / ${if (totalProgress > 0) totalProgress else config.count}",
                            color = TextPrimaryDark
                        )
                        MetricItem(
                            label = "Success",
                            value = "$successCount",
                            color = TerminalGreen
                        )
                        MetricItem(
                            label = "Sender Mask",
                            value = if (config.dispatchMode == DispatchMode.CLOUD_ANONYMOUS) config.senderId else "SIM No.",
                            color = if (config.dispatchMode == DispatchMode.CLOUD_ANONYMOUS) TerminalGreen else NeonOrange
                        )
                        MetricItem(
                            label = "Mode",
                            value = when (config.dispatchMode) {
                                DispatchMode.CLOUD_ANONYMOUS -> "Masked Gateway"
                                DispatchMode.REAL_SIM -> "Real SIM"
                                DispatchMode.SANDBOX -> "Sandbox"
                            },
                            color = when (config.dispatchMode) {
                                DispatchMode.CLOUD_ANONYMOUS -> TerminalGreen
                                DispatchMode.REAL_SIM -> NeonOrange
                                DispatchMode.SANDBOX -> ElectricCyan
                            }
                        )
                    }
                }
            }
        }

        // DISPATCH CHANNEL SELECTOR (ANONYMOUS MASKED vs REAL SIM vs SANDBOX)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "1. DISPATCH SOURCE & SENDER IDENTITY",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ElectricCyan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DispatchModeButton(
                            title = "🛡️ Masked Sender",
                            subtitle = "Hide phone number",
                            isSelected = config.dispatchMode == DispatchMode.CLOUD_ANONYMOUS,
                            selectedColor = TerminalGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setDispatchMode(DispatchMode.CLOUD_ANONYMOUS) }
                        )

                        DispatchModeButton(
                            title = "📱 Real SIM",
                            subtitle = "Shows your SIM #",
                            isSelected = config.dispatchMode == DispatchMode.REAL_SIM,
                            selectedColor = NeonOrange,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (!hasSmsPermission) {
                                    onRequestSmsPermission()
                                }
                                viewModel.setDispatchMode(DispatchMode.REAL_SIM)
                            }
                        )

                        DispatchModeButton(
                            title = "🧪 Sandbox",
                            subtitle = "Virtual testing",
                            isSelected = config.dispatchMode == DispatchMode.SANDBOX,
                            selectedColor = ElectricCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setDispatchMode(DispatchMode.SANDBOX) }
                        )
                    }

                    if (config.dispatchMode == DispatchMode.CLOUD_ANONYMOUS) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, TerminalGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            color = DarkBg
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = TerminalGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Receiver Sees Sender As:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryDark
                                        )
                                    }

                                    TextButton(
                                        onClick = { showSenderIdDialog = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = TerminalGreen
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Change ID", fontSize = 11.sp, color = TerminalGreen)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TerminalGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = config.senderId,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TerminalGreen
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "• Provider: ${config.gatewayConfig.selectedProvider.displayName}",
                                            fontSize = 10.sp,
                                            color = TextSecondaryDark
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("TX-ALERTS", "VK-SECURE", "SMS-PRO", "OTP-AUTH", "FLASH-INFO").forEach { preset ->
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { viewModel.setSenderId(preset) }
                                                .border(
                                                    1.dp,
                                                    if (config.senderId == preset) TerminalGreen else DarkSurfaceBorder,
                                                    RoundedCornerShape(6.dp)
                                                ),
                                            color = if (config.senderId == preset) TerminalGreen.copy(alpha = 0.2f) else DarkSurfaceElevated
                                        ) {
                                            Text(
                                                text = preset,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (config.senderId == preset) TerminalGreen else TextSecondaryDark,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SPEED & TURBO FREQUENCY BOOST SECTION
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonOrange.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. TURBO SPEED PRESETS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = NeonOrange
                            )
                        }
                        Text(
                            text = "${config.delaySeconds}s (${String.format(Locale.US, "%.0f", currentSpeedMsgPerSec)} msg/sec)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpeedPresetChip(
                            title = "⚡ 0.05s",
                            subtitle = "20/sec",
                            isSelected = config.delaySeconds == 0.05f,
                            accentColor = TerminalRed,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSpeedPreset(0.05f) }
                        )
                        SpeedPresetChip(
                            title = "🚀 0.10s",
                            subtitle = "10/sec",
                            isSelected = config.delaySeconds == 0.10f,
                            accentColor = NeonOrange,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSpeedPreset(0.10f) }
                        )
                        SpeedPresetChip(
                            title = "⚡ 0.25s",
                            subtitle = "4/sec",
                            isSelected = config.delaySeconds == 0.25f,
                            accentColor = BrightAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSpeedPreset(0.25f) }
                        )
                        SpeedPresetChip(
                            title = "⏱ 0.50s",
                            subtitle = "2/sec",
                            isSelected = config.delaySeconds == 0.50f,
                            accentColor = ElectricCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSpeedPreset(0.50f) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Batch Packet Quantity:", fontSize = 13.sp, color = TextPrimaryDark)
                        Text(
                            text = "${config.count} Packets",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElectricCyan
                        )
                    }

                    Slider(
                        value = config.count.toFloat(),
                        onValueChange = { viewModel.updateCount(it.toInt()) },
                        valueRange = 1f..100f,
                        steps = 98,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = DarkSurfaceBorder
                        ),
                        modifier = Modifier.testTag("count_slider")
                    )
                }
            }
        }

        // TARGET RECIPIENT CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. TARGET RECIPIENT NUMBER",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )
                        TextButton(
                            onClick = { showContactPicker = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ElectricCyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Contacts", fontSize = 12.sp, color = ElectricCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = config.targetNumber,
                        onValueChange = { viewModel.updateTargetNumber(it) },
                        label = { Text("Destination Phone Number") },
                        placeholder = { Text("+12025550199 or 9876543210") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_number_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        )
                    )
                }
            }
        }

        // MESSAGE COMPOSER CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "4. MESSAGE PAYLOAD",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )
                        Text(
                            text = "${config.messageTemplate.length} chars (${(config.messageTemplate.length / 160) + 1} SMS)",
                            fontSize = 11.sp,
                            color = if (config.messageTemplate.length > 160) WarningYellow else TextSecondaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = config.messageTemplate,
                        onValueChange = { viewModel.updateMessageTemplate(it) },
                        label = { Text("Message Body") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                            .testTag("message_body_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TagChip(label = "#{index}", onClick = { viewModel.insertTag("{index}") })
                        TagChip(label = "OTP {code}", onClick = { viewModel.insertTag("{code}") })
                        TagChip(label = "🕒 {time}", onClick = { viewModel.insertTag("{time}") })
                        TagChip(label = "🎲 {random}", onClick = { viewModel.insertTag("{random}") })
                    }
                }
            }
        }

        // LAUNCH & CONTROL BUTTONS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isBusy) {
                    Button(
                        onClick = {
                            if (config.dispatchMode == DispatchMode.REAL_SIM && !hasSmsPermission) {
                                onRequestSmsPermission()
                            } else {
                                viewModel.startBlast()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("start_blast_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (config.dispatchMode) {
                                DispatchMode.CLOUD_ANONYMOUS -> TerminalGreen
                                DispatchMode.REAL_SIM -> NeonOrange
                                DispatchMode.SANDBOX -> CyberCyan
                            }
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (config.dispatchMode) {
                                    DispatchMode.CLOUD_ANONYMOUS -> Icons.Default.Bolt
                                    DispatchMode.REAL_SIM -> Icons.Default.Send
                                    DispatchMode.SANDBOX -> Icons.Outlined.Science
                                },
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (config.dispatchMode) {
                                    DispatchMode.CLOUD_ANONYMOUS -> "LAUNCH FAST MASKED BLAST ⚡"
                                    DispatchMode.REAL_SIM -> "LAUNCH REAL SIM BLAST"
                                    DispatchMode.SANDBOX -> "LAUNCH ULTRA SANDBOX TEST"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isRunning) {
                            Button(
                                onClick = { viewModel.pauseBlast() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("pause_blast_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrightAmber)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PAUSE", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.resumeBlast() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("resume_blast_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("RESUME", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Button(
                            onClick = { viewModel.stopBlast() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("stop_blast_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalRed)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ABORT", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                if (engineState == EngineState.COMPLETED || engineState == EngineState.STOPPED) {
                    OutlinedButton(
                        onClick = { viewModel.resetBlast() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder))
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondaryDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Telemetry", color = TextSecondaryDark, fontSize = 13.sp)
                    }
                }
            }
        }

        // LIVE TERMINAL LOGS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE TURBO DISPATCH TERMINAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }
                        Text(
                            text = "${terminalLogs.size} events",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (terminalLogs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Turbo terminal idle. Tap Launch to stream packets...",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyColumn(
                            state = terminalListState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(terminalLogs) { log ->
                                Text(
                                    text = log.text,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = when {
                                        log.isError -> TerminalRed
                                        log.isWarning -> WarningYellow
                                        log.isSuccess -> TerminalGreen
                                        else -> TextPrimaryDark
                                    },
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // SENDER ID CUSTOMIZER DIALOG
    if (showSenderIdDialog) {
        var tempSenderId by remember { mutableStateOf(config.senderId) }
        AlertDialog(
            onDismissRequest = { showSenderIdDialog = false },
            title = {
                Text(
                    text = "Configure Masked Sender ID",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter the name/brand header that receiver will see instead of your phone number (Max 11 alphanumeric chars):",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )

                    OutlinedTextField(
                        value = tempSenderId,
                        onValueChange = { tempSenderId = it.take(11).uppercase() },
                        label = { Text("Sender Header ID") },
                        placeholder = { Text("e.g. TX-ALERTS, VK-SECURE") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerminalGreen,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempSenderId.isNotBlank()) {
                            viewModel.setSenderId(tempSenderId)
                        }
                        showSenderIdDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                ) {
                    Text("Apply Sender Mask", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSenderIdDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface
        )
    }

    // MASKED PRIVACY INFO DIALOG
    if (showMaskInfoDialog) {
        AlertDialog(
            onDismissRequest = { showMaskInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = TerminalGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Anonymous Sender Masking", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. How it works: In 'Masked Sender' mode, your personal SIM phone number is replaced with an Alpha Sender Header (e.g. TX-ALERTS, SMS-PRO).",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = "2. Recipient View: On the recipient's phone, the SMS appears from the chosen Header name rather than any personal 10-digit number.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = "3. Complete Privacy: Your mobile operator SIM identity and caller-ID remain protected.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMaskInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                ) {
                    Text("Got It", color = Color.White)
                }
            },
            containerColor = DarkSurface
        )
    }

    // CONTACT PICKER DIALOG
    if (showContactPicker) {
        AlertDialog(
            onDismissRequest = { showContactPicker = false },
            title = {
                Text(
                    text = "Select Recipient Target",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose from device contacts or presets:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(contacts) { contact ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.updateTargetNumber(contact.phoneNumber)
                                        showContactPicker = false
                                    }
                                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp)),
                                color = DarkSurfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(CyberCyan.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricCyan,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = contact.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = TextPrimaryDark
                                        )
                                        Text(
                                            text = contact.phoneNumber,
                                            fontSize = 11.sp,
                                            color = TextSecondaryDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactPicker = false }) {
                    Text("Close", color = ElectricCyan)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun SpeedPresetChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .border(
                1.5.dp,
                if (isSelected) accentColor else DarkSurfaceBorder,
                RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.2f) else DarkBg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) accentColor else TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) accentColor else TextSecondaryDark
            )
        }
    }
}

@Composable
private fun DispatchModeButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .border(
                1.5.dp,
                if (isSelected) selectedColor else DarkSurfaceBorder,
                RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) selectedColor.copy(alpha = 0.15f) else DarkBg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) selectedColor else TextPrimaryDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = TextSecondaryDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = TextSecondaryDark)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun TagChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp)),
        color = DarkSurfaceElevated
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Tag,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, fontSize = 11.sp, color = ElectricCyan)
        }
    }
}
