package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.TerminalGreenBg
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalRedBg
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningYellow
import com.example.ui.viewmodel.SmsBlastViewModel

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
    var showSafetyDisclaimer by remember { mutableStateOf(false) }

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
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // TOP TELEMETRY STATUS BANNER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isRunning) ElectricCyan else DarkSurfaceBorder,
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
                                    EngineState.RUNNING -> "BLASTING DISPATCH..."
                                    EngineState.PAUSED -> "DISPATCH PAUSED"
                                    EngineState.COMPLETED -> "BATCH COMPLETED"
                                    EngineState.STOPPED -> "DISPATCH TERMINATED"
                                    EngineState.ERROR -> "DISPATCH ERROR"
                                    EngineState.IDLE -> "READY FOR LAUNCH"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
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

                        IconButton(
                            onClick = { showSafetyDisclaimer = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HelpOutline,
                                contentDescription = "Disclaimer",
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                        color = if (config.isSimulationMode) ElectricCyan else NeonOrange,
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
                            label = "Failed",
                            value = "$failedCount",
                            color = if (failedCount > 0) TerminalRed else TextSecondaryDark
                        )
                        MetricItem(
                            label = "Mode",
                            value = if (config.isSimulationMode) "Sandbox" else "Real SIM",
                            color = if (config.isSimulationMode) ElectricCyan else NeonOrange
                        )
                    }
                }
            }
        }

        // TARGET PHONE NUMBER CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
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
                        Text(
                            text = "1. TARGET RECIPIENT",
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = config.targetNumber,
                        onValueChange = { viewModel.updateTargetNumber(it) },
                        label = { Text("Phone Number (with country code)") },
                        placeholder = { Text("+12025550199 or 03001234567") },
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick number chips
                    Text(
                        text = "Quick Presets:",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(contacts.take(4)) { contact ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updateTargetNumber(contact.phoneNumber) }
                                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp)),
                                color = DarkSurfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = contact.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimaryDark
                                    )
                                }
                            }
                        }
                    }
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
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. MESSAGE TEMPLATE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )
                        Text(
                            text = "${config.messageTemplate.length} chars (${(config.messageTemplate.length / 160) + 1} SMS)",
                            fontSize = 11.sp,
                            color = if (config.messageTemplate.length > 160) WarningYellow else TextSecondaryDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = config.messageTemplate,
                        onValueChange = { viewModel.updateMessageTemplate(it) },
                        label = { Text("Message Body") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic Tag Chips
                    Text(
                        text = "Insert Dynamic Placeholders:",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
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
                        TagChip(label = "Total {total}", onClick = { viewModel.insertTag("{total}") })
                    }
                }
            }
        }

        // ENGINE PARAMETERS (COUNT & DELAY)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "3. BLAST DISPATCH PARAMETERS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ElectricCyan
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Count Stepper & Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Packet Count:", fontSize = 13.sp, color = TextPrimaryDark)
                        }
                        Text(
                            text = "${config.count} Messages",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeonOrange
                        )
                    }

                    Slider(
                        value = config.count.toFloat(),
                        onValueChange = { viewModel.updateCount(it.toInt()) },
                        valueRange = 1f..100f,
                        steps = 98,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonOrange,
                            activeTrackColor = NeonOrange,
                            inactiveTrackColor = DarkSurfaceBorder
                        ),
                        modifier = Modifier.testTag("count_slider")
                    )

                    // Quick Count presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 5, 10, 20, 50).forEach { preset ->
                            OutlinedButton(
                                onClick = { viewModel.updateCount(preset) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (config.count == preset) NeonOrange.copy(alpha = 0.2f) else DarkBg
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            if (config.count == preset) NeonOrange else DarkSurfaceBorder,
                                            if (config.count == preset) BrightAmber else DarkSurfaceBorder
                                        )
                                    )
                                )
                            ) {
                                Text(
                                    text = "$preset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (config.count == preset) NeonOrange else TextSecondaryDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Delay Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Interval Delay:", fontSize = 13.sp, color = TextPrimaryDark)
                        }
                        Text(
                            text = "${config.delaySeconds} sec",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElectricCyan
                        )
                    }

                    Slider(
                        value = config.delaySeconds,
                        onValueChange = { viewModel.updateDelay(it) },
                        valueRange = 0.2f..5.0f,
                        steps = 47,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = DarkSurfaceBorder
                        ),
                        modifier = Modifier.testTag("delay_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulation vs Real Hardware Switch
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (config.isSimulationMode) Icons.Outlined.Science else Icons.Default.SimCard,
                                    contentDescription = null,
                                    tint = if (config.isSimulationMode) ElectricCyan else NeonOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (config.isSimulationMode) "Sandbox Mode (Zero Cost)" else "Real Hardware SIM Blast",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimaryDark
                                    )
                                    Text(
                                        text = if (config.isSimulationMode) "Simulates telecom loop & delivery" else "Dispatches real SMS using device carrier",
                                        fontSize = 10.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                            }

                            Switch(
                                checked = !config.isSimulationMode,
                                onCheckedChange = { isReal ->
                                    if (isReal && !hasSmsPermission) {
                                        onRequestSmsPermission()
                                    }
                                    viewModel.toggleSimulationMode(!isReal)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonOrange,
                                    checkedTrackColor = NeonOrange.copy(alpha = 0.5f),
                                    uncheckedThumbColor = ElectricCyan,
                                    uncheckedTrackColor = DarkSurfaceElevated
                                ),
                                modifier = Modifier.testTag("simulation_mode_switch")
                            )
                        }
                    }
                }
            }
        }

        // ACTION BUTTONS (START / PAUSE / STOP)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isBusy) {
                    Button(
                        onClick = {
                            if (!config.isSimulationMode && !hasSmsPermission) {
                                onRequestSmsPermission()
                            } else {
                                viewModel.startBlast()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_blast_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (config.isSimulationMode) CyberCyan else NeonOrange
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (config.isSimulationMode) "LAUNCH SANDBOX TEST" else "START REAL SMS BLAST",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                                letterSpacing = 0.5.sp
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
                                text = "LIVE DISPATCH TERMINAL",
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
                                text = "Terminal idle. Press Launch to start telemetry feed...",
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

    // SAFETY & DISCLAIMER DIALOG
    if (showSafetyDisclaimer) {
        AlertDialog(
            onDismissRequest = { showSafetyDisclaimer = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = BrightAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Telecom Compliance & Safety", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. Carrier Billing: Real SMS mode uses your active SIM plan and may incur standard carrier messaging charges.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = "2. Android System Limits: Android OS imposes internal rate-limiting (approx. 30-100 SMS/min) to prevent network congestion. Adjust the Interval Delay to 1.0s or more.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = "3. Fair Use Policy: This tool is designed for authorized batch notification testing, server alert simulation, OTP stress tests, and event broadcasts. Do not harass or spam unconsenting recipients.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSafetyDisclaimer = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Understood", color = Color.White)
                }
            },
            containerColor = DarkSurface
        )
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
