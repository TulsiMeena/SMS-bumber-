package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GatewayProvider
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

@Composable
fun CarrierScreen(
    viewModel: SmsBlastViewModel,
    hasSmsPermission: Boolean,
    hasContactsPermission: Boolean,
    onRequestSmsPermission: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsStateWithLifecycle()
    val gw = config.gatewayConfig
    val telephonyManager = remember {
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    }

    var selectedProvider by remember { mutableStateOf(gw.selectedProvider) }
    var fast2SmsKey by remember { mutableStateOf(gw.apiKey) }
    var twilioSid by remember { mutableStateOf(gw.twilioAccountSid) }
    var twilioAuth by remember { mutableStateOf(gw.twilioAuthToken) }
    var twilioFrom by remember { mutableStateOf(gw.twilioFromNumber) }
    var msg91Key by remember { mutableStateOf(gw.apiKey) }
    var customWebhookUrl by remember { mutableStateOf(gw.customApiUrl) }
    var customWebhookKey by remember { mutableStateOf(gw.apiKey) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

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
                text = "Real SMS Gateway & Cloud APIs",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryDark
            )
            Text(
                text = "Connect real cloud SMS providers to send actual SMS without showing your SIM number.",
                fontSize = 12.sp,
                color = TextSecondaryDark
            )
        }

        // Provider Selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = TerminalGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SELECT REAL CLOUD SMS PROVIDER",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TerminalGreen
                        )
                    }

                    // Provider Tabs
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            GatewayProvider.FAST2SMS to "Fast2SMS (Direct India / Quick SMS)",
                            GatewayProvider.TWILIO to "Twilio (Global International SMS)",
                            GatewayProvider.MSG91 to "MSG91 (Enterprise Sender ID)",
                            GatewayProvider.CUSTOM_WEBHOOK to "Custom REST Webhook / API URL",
                            GatewayProvider.DEFAULT_RELAY to "Built-in Simulator Relay"
                        ).forEach { (provider, label) ->
                            val isSelected = selectedProvider == provider
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedProvider = provider
                                        viewModel.setGatewayProvider(provider)
                                    }
                                    .border(
                                        1.dp,
                                        if (isSelected) TerminalGreen else DarkSurfaceBorder,
                                        RoundedCornerShape(10.dp)
                                    ),
                                color = if (isSelected) TerminalGreen.copy(alpha = 0.15f) else DarkBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) TerminalGreen else DarkSurfaceBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) TerminalGreen else TextPrimaryDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dynamic Fields based on Selected Provider
                    when (selectedProvider) {
                        GatewayProvider.FAST2SMS -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Fast2SMS API Credentials:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                                OutlinedTextField(
                                    value = fast2SmsKey,
                                    onValueChange = { fast2SmsKey = it },
                                    label = { Text("Fast2SMS Authorization API Key") },
                                    placeholder = { Text("e.g. kA1bC2dE3fG4...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TerminalGreen,
                                        unfocusedBorderColor = DarkSurfaceBorder,
                                        focusedTextColor = TextPrimaryDark,
                                        unfocusedTextColor = TextPrimaryDark
                                    )
                                )
                                Text(
                                    text = "Tip: Get free API Key from fast2sms.com > Dev API.",
                                    fontSize = 10.sp,
                                    color = TextSecondaryDark
                                )

                                Button(
                                    onClick = {
                                        viewModel.updateFast2SmsApiKey(fast2SmsKey)
                                        saveSuccessMessage = "Fast2SMS configuration saved successfully!"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Fast2SMS Config", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        GatewayProvider.TWILIO -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Twilio API Credentials:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                                OutlinedTextField(
                                    value = twilioSid,
                                    onValueChange = { twilioSid = it },
                                    label = { Text("Twilio Account SID") },
                                    placeholder = { Text("ACxxxxxxxxxxxxxxxxxxxxxxxx") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TerminalGreen,
                                        unfocusedBorderColor = DarkSurfaceBorder,
                                        focusedTextColor = TextPrimaryDark,
                                        unfocusedTextColor = TextPrimaryDark
                                    )
                                )
                                OutlinedTextField(
                                    value = twilioAuth,
                                    onValueChange = { twilioAuth = it },
                                    label = { Text("Twilio Auth Token") },
                                    placeholder = { Text("auth_token_here") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TerminalGreen,
                                        unfocusedBorderColor = DarkSurfaceBorder,
                                        focusedTextColor = TextPrimaryDark,
                                        unfocusedTextColor = TextPrimaryDark
                                    )
                                )
                                OutlinedTextField(
                                    value = twilioFrom,
                                    onValueChange = { twilioFrom = it },
                                    label = { Text("Twilio Sender Number / Alpha Header") },
                                    placeholder = { Text("+18005550199 or TX-ALERTS") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TerminalGreen,
                                        unfocusedBorderColor = DarkSurfaceBorder,
                                        focusedTextColor = TextPrimaryDark,
                                        unfocusedTextColor = TextPrimaryDark
                                    )
                                )

                                Button(
                                    onClick = {
                                        viewModel.updateTwilioConfig(twilioSid, twilioAuth, twilioFrom)
                                        saveSuccessMessage = "Twilio configuration saved successfully!"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Twilio Config", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        GatewayProvider.MSG91 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "MSG91 API Credentials:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                                OutlinedTextField(
                                    value = msg91Key,
                                    onValueChange = { msg91Key = it },
                                    label = { Text("MSG91 AuthKey") },
                                    placeholder = { Text("3829482xxxxxxxx") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TerminalGreen,
                                        unfocusedBorderColor = DarkSurfaceBorder,
                                        focusedTextColor = TextPrimaryDark,
                                        unfocusedTextColor = TextPrimaryDark
                                    )
                                )
                                Button(
                                    onClick = {
                                        viewModel.updateMsg91Config(msg91Key)
                                        saveSuccessMessage = "MSG91 configuration saved successfully!"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save MSG91 Config", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        GatewayProvider.CUSTOM_WEBHOOK -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Custom Webhook Endpoint:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                                OutlinedTextField(
                                    value = customWebhookUrl,
                                    onValueChange = { customWebhookUrl = it },
                                    label = { Text("POST URL Endpoint") },
                                    placeholder = { Text("https://my-sms-api.com/send") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TerminalGreen,
                                        unfocusedBorderColor = DarkSurfaceBorder,
                                        focusedTextColor = TextPrimaryDark,
                                        unfocusedTextColor = TextPrimaryDark
                                    )
                                )
                                OutlinedTextField(
                                    value = customWebhookKey,
                                    onValueChange = { customWebhookKey = it },
                                    label = { Text("Bearer Token / Secret (Optional)") },
                                    placeholder = { Text("secret_token") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TerminalGreen,
                                        unfocusedBorderColor = DarkSurfaceBorder,
                                        focusedTextColor = TextPrimaryDark,
                                        unfocusedTextColor = TextPrimaryDark
                                    )
                                )
                                Button(
                                    onClick = {
                                        viewModel.updateCustomWebhook(customWebhookUrl, customWebhookKey)
                                        saveSuccessMessage = "Custom Webhook saved successfully!"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen)
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Webhook Config", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        GatewayProvider.DEFAULT_RELAY, GatewayProvider.TEXTLOCAL -> {
                            Text(
                                text = "Built-in Relay mode active. Tests anonymous payload formatting and telemetry logs without live API costs.",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    saveSuccessMessage?.let {
                        Text(
                            text = "✔ $it",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalGreen
                        )
                    }
                }
            }
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
                                text = "DEVICE HARDWARE SIM",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    DiagnosticItem("Network Provider", networkOperatorName)
                    DiagnosticItem("Android Version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    DiagnosticItem("Device Model", "${Build.MANUFACTURER} ${Build.MODEL}")
                    DiagnosticItem("SIM Radio State", if (simState == TelephonyManager.SIM_STATE_READY) "Ready / Operational" else "Ready (Virtual Radio)")
                }
            }
        }

        // Permissions Center
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
                            text = "ANDROID SYSTEM PERMISSIONS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonOrange
                        )
                    }

                    PermissionRow(
                        name = "SEND_SMS Permission",
                        description = "Required when using Physical SIM mode on phone",
                        isGranted = hasSmsPermission,
                        onRequest = onRequestSmsPermission
                    )

                    PermissionRow(
                        name = "READ_CONTACTS Permission",
                        description = "Enables instant contact picking",
                        isGranted = hasContactsPermission,
                        onRequest = onRequestContactsPermission
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
