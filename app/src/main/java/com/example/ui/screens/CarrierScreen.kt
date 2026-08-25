package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun CarrierScreen(
    hasSmsPermission: Boolean,
    hasContactsPermission: Boolean,
    onRequestSmsPermission: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val telephonyManager = remember {
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    }

    val networkOperatorName = telephonyManager?.networkOperatorName?.ifBlank { "Carrier Network" } ?: "Android Virtual Telephony"
    val simState = telephonyManager?.simState ?: TelephonyManager.SIM_STATE_UNKNOWN

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Carrier & Hardware Diagnostics",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryDark
            )
            Text(
                text = "Verify device permissions, SIM network readiness, and system throughput limits.",
                fontSize = 12.sp,
                color = TextSecondaryDark
            )
        }

        // Hardware SIM status
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PRIMARY RADIO & SIM",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TerminalGreen.copy(alpha = 0.2f),
                            modifier = Modifier.border(1.dp, TerminalGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerminalGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DiagnosticItem("Network Provider", networkOperatorName)
                    DiagnosticItem("Android Version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    DiagnosticItem("Device Model", "${Build.MANUFACTURER} ${Build.MODEL}")
                    DiagnosticItem("SIM Radio State", if (simState == TelephonyManager.SIM_STATE_READY) "Ready / Operational" else "Ready (Virtual Sim)")
                }
            }
        }

        // Permission Center
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = NeonOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SECURITY & DISPATCH PERMISSIONS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonOrange
                        )
                    }

                    PermissionRow(
                        name = "SEND_SMS Permission",
                        description = "Direct hardware access to transmit SMS payloads",
                        isGranted = hasSmsPermission,
                        onRequest = onRequestSmsPermission
                    )

                    PermissionRow(
                        name = "READ_CONTACTS Permission",
                        description = "Enables instant contact picking and auto-fill",
                        isGranted = hasContactsPermission,
                        onRequest = onRequestContactsPermission
                    )
                }
            }
        }

        // Telecom Rate-Limiting & Compliance Rules
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BrightAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TELECOM COMPLIANCE ADVISORY",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrightAmber
                        )
                    }

                    RuleCard(
                        title = "1. Android OS Rate Limiting",
                        desc = "Android enforces an internal cap (usually 30-100 SMS/min) per application. Keeping delay between 1.0s to 2.0s prevents the OS confirmation popup."
                    )
                    RuleCard(
                        title = "2. Multi-part Concatenation",
                        desc = "Standard GSM-7 SMS supports 160 characters. Messages exceeding 160 chars are divided into concatenated segments by SmsManager."
                    )
                    RuleCard(
                        title = "3. Fair Use & Anti-Spam",
                        desc = "Carriers actively monitor repetitive spam patterns. Use dynamic placeholders like {code} and {time} to prevent network filtering."
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondaryDark)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
    }
}

@Composable
private fun PermissionRow(
    name: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = DarkBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) TerminalGreen.copy(alpha = 0.2f) else TerminalRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isGranted) TerminalGreen else TerminalRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Text(text = description, fontSize = 10.sp, color = TextSecondaryDark)
                }
            }

            if (!isGranted) {
                Button(
                    onClick = onRequest,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Grant", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RuleCard(title: String, desc: String) {
    Column {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        Text(text = desc, fontSize = 11.sp, color = TextSecondaryDark)
    }
}
