package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SmsDeliveryStatus
import com.example.data.model.SmsLog
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
import com.example.ui.viewmodel.SmsBlastViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: SmsBlastViewModel,
    onResendToBlaster: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.filteredLogs.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalLogsCount.collectAsStateWithLifecycle()
    val successCount by viewModel.successfulLogsCount.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedHistoryFilter.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }
    var inspectLog by remember { mutableStateOf<SmsLog?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Overview Summary Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DISPATCH AUDIT LOGS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )
                        Text(
                            text = "$totalCount Total Recorded",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryDark
                        )
                    }

                    if (totalCount > 0) {
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All",
                                tint = TerminalRed
                            )
                        }
                    }
                }
            }
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    HistoryFilterChip(
                        label = "All Logs",
                        isSelected = selectedFilter == null,
                        onClick = { viewModel.setHistoryFilter(null) }
                    )
                }
                item {
                    HistoryFilterChip(
                        label = "✔ Sent / Delivered",
                        isSelected = selectedFilter == SmsDeliveryStatus.SENT,
                        onClick = { viewModel.setHistoryFilter(SmsDeliveryStatus.SENT) }
                    )
                }
                item {
                    HistoryFilterChip(
                        label = "🧪 Simulated",
                        isSelected = selectedFilter == SmsDeliveryStatus.SIMULATED,
                        onClick = { viewModel.setHistoryFilter(SmsDeliveryStatus.SIMULATED) }
                    )
                }
                item {
                    HistoryFilterChip(
                        label = "❌ Failed",
                        isSelected = selectedFilter == SmsDeliveryStatus.FAILED,
                        onClick = { viewModel.setHistoryFilter(SmsDeliveryStatus.FAILED) }
                    )
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No dispatch records found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "Run a blast from the main console to record telemetry logs.",
                            fontSize = 12.sp,
                            color = TextSecondaryDark.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                HistoryLogCard(
                    log = log,
                    onInspect = { inspectLog = log },
                    onResend = {
                        viewModel.resendLog(log)
                        onResendToBlaster()
                    },
                    onDelete = { viewModel.deleteLog(log.id) }
                )
            }
        }
    }

    // Inspect Details Dialog
    inspectLog?.let { log ->
        AlertDialog(
            onDismissRequest = { inspectLog = null },
            title = {
                Text(
                    text = "Packet #${log.indexInBatch}/${log.totalBatch} Details",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow("Recipient", log.phoneNumber)
                    DetailRow("Batch ID", log.batchId)
                    DetailRow("Status", log.status.name)
                    DetailRow("Mode", if (log.isSimulation) "Sandbox Simulated" else "Real SIM Slot ${log.simSlot + 1}")
                    DetailRow("Latency", "${log.latencyMs} ms")
                    DetailRow(
                        "Timestamp",
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                    )

                    if (!log.errorMessage.isNullOrBlank()) {
                        DetailRow("Error", log.errorMessage)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Full Payload:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        color = DarkBg
                    ) {
                        Text(
                            text = log.messageText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimaryDark,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resendLog(log)
                        inspectLog = null
                        onResendToBlaster()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Resend in Blaster", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectLog = null }) {
                    Text("Close", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Audit Logs?", fontWeight = FontWeight.Bold, color = TextPrimaryDark) },
            text = {
                Text(
                    "This action will wipe all sent SMS logs and batch telemetry records permanently.",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllLogs()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalRed)
                ) {
                    Text("Wipe All Logs", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun HistoryLogCard(
    log: SmsLog,
    onInspect: () -> Unit,
    onResend: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when (log.status) {
                            SmsDeliveryStatus.SENT, SmsDeliveryStatus.DELIVERED -> TerminalGreen.copy(alpha = 0.15f)
                            SmsDeliveryStatus.SIMULATED -> ElectricCyan.copy(alpha = 0.15f)
                            SmsDeliveryStatus.FAILED -> TerminalRed.copy(alpha = 0.15f)
                            else -> DarkSurfaceElevated
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (log.status) {
                        SmsDeliveryStatus.SENT, SmsDeliveryStatus.DELIVERED -> Icons.Default.CheckCircle
                        SmsDeliveryStatus.SIMULATED -> Icons.Default.Science
                        SmsDeliveryStatus.FAILED -> Icons.Default.Error
                        else -> Icons.Default.HourglassTop
                    },
                    contentDescription = null,
                    tint = when (log.status) {
                        SmsDeliveryStatus.SENT, SmsDeliveryStatus.DELIVERED -> TerminalGreen
                        SmsDeliveryStatus.SIMULATED -> ElectricCyan
                        SmsDeliveryStatus.FAILED -> TerminalRed
                        else -> TextSecondaryDark
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.phoneNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }

                Text(
                    text = log.messageText,
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[${log.batchId} #${log.indexInBatch}/${log.totalBatch}]",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ElectricCyan
                    )
                    Text(
                        text = "${log.latencyMs}ms",
                        fontSize = 10.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            IconButton(
                onClick = onResend,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Resend",
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                1.dp,
                if (isSelected) ElectricCyan else DarkSurfaceBorder,
                RoundedCornerShape(16.dp)
            ),
        color = if (isSelected) CyberCyan.copy(alpha = 0.3f) else DarkSurfaceElevated
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) ElectricCyan else TextSecondaryDark
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondaryDark)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
    }
}
