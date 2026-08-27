package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
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
import com.example.data.model.DispatchMode
import com.example.data.model.SmsDeliveryStatus
import com.example.data.model.SmsLog
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

    var selectedLogForDetail by remember { mutableStateOf<SmsLog?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // TOP TELEMETRY SUMMARY
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
                            text = "DISPATCH TELEMETRY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalCount Total Packets",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = TextPrimaryDark
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Delivered", fontSize = 10.sp, color = TextSecondaryDark)
                            Text(
                                text = "$successCount",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerminalGreen
                            )
                        }

                        if (totalCount > 0) {
                            IconButton(
                                onClick = { showClearConfirm = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("clear_history_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear History",
                                    tint = TerminalRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FILTER CHIPS ROW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Filter Logs:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.width(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            label = "All ($totalCount)",
                            isSelected = selectedFilter == null,
                            onClick = { viewModel.setHistoryFilter(null) }
                        )
                    }
                    items(SmsDeliveryStatus.entries) { status ->
                        FilterChip(
                            label = status.name,
                            isSelected = selectedFilter == status,
                            onClick = { viewModel.setHistoryFilter(status) }
                        )
                    }
                }
            }
        }

        // LOG ITEMS LIST
        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No SMS Dispatch Records Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Launch an SMS batch from the Blaster tab to see telemetry logs.",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                HistoryLogCard(
                    log = log,
                    onViewDetails = { selectedLogForDetail = log },
                    onDelete = { viewModel.deleteLog(log.id) },
                    onResend = {
                        viewModel.resendLog(log)
                        onResendToBlaster()
                    }
                )
            }
        }
    }

    // LOG DETAIL DIALOG
    selectedLogForDetail?.let { log ->
        AlertDialog(
            onDismissRequest = { selectedLogForDetail = null },
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
                    DetailRow("Sender ID", log.senderId)
                    DetailRow("Mode", if (log.isNumberMasked) "Masked Gateway (${log.gatewayProvider})" else "Hardware SIM Slot ${log.simSlot + 1}")
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
                        shape = RoundedCornerShape(8.dp),
                        color = DarkBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = log.messageText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimaryDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resendLog(log)
                        selectedLogForDetail = null
                        onResendToBlaster()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Load into Blaster", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLogForDetail = null }) {
                    Text("Close", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface
        )
    }

    // CLEAR HISTORY CONFIRMATION
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Text("Clear All Telemetry Logs?", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            },
            text = {
                Text(
                    "This will delete all saved SMS dispatch records from the local database.",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllLogs()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalRed)
                ) {
                    Text("Delete All Logs", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
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
    onViewDetails: () -> Unit,
    onDelete: () -> Unit,
    onResend: () -> Unit
) {
    val statusColor = when (log.status) {
        SmsDeliveryStatus.DELIVERED, SmsDeliveryStatus.SENT -> TerminalGreen
        SmsDeliveryStatus.PENDING -> WarningYellow
        SmsDeliveryStatus.FAILED -> TerminalRed
        SmsDeliveryStatus.SIMULATED -> ElectricCyan
    }

    val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.phoneNumber,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (log.isNumberMasked) TerminalGreen.copy(alpha = 0.15f) else NeonOrange.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (log.isNumberMasked) "🛡️ ${log.senderId}" else "SIM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (log.isNumberMasked) TerminalGreen else NeonOrange,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.messageText,
                fontSize = 12.sp,
                color = TextSecondaryDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pkt #${log.indexInBatch}/${log.totalBatch} • ${log.status.name} • ${log.latencyMs}ms",
                    fontSize = 10.sp,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )

                TextButton(
                    onClick = onResend,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Resend", fontSize = 11.sp, color = ElectricCyan)
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(
                1.dp,
                if (isSelected) ElectricCyan else DarkSurfaceBorder,
                RoundedCornerShape(8.dp)
            ),
        color = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else DarkSurface
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) ElectricCyan else TextSecondaryDark,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
        )
    }
}
