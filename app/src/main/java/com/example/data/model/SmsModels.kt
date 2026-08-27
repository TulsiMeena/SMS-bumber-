package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SmsDeliveryStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
    SIMULATED
}

enum class DispatchMode {
    CLOUD_ANONYMOUS, // Uses Cloud Gateway APIs (Fast2SMS, Twilio, Msg91, Textlocal, Webhook) - Personal SIM number is 100% hidden
    REAL_SIM,        // Hardware SIM card
    SANDBOX          // Offline virtual simulator
}

enum class GatewayProvider(val displayName: String, val defaultEndpoint: String, val supportsMaskedSender: Boolean) {
    FAST2SMS("Fast2SMS (Quick OTP/Direct)", "https://www.fast2sms.com/dev/bulkV2", true),
    TWILIO("Twilio Cloud SMS", "https://api.twilio.com/2010-04-01/Accounts", true),
    MSG91("MSG91 Alpha SMS", "https://control.msg91.com/api/v5/flow", true),
    TEXTLOCAL("Textlocal Gateway", "https://api.textlocal.in/send", true),
    CUSTOM_WEBHOOK("Custom REST Webhook", "", true),
    DEFAULT_RELAY("Built-in Instant Relay", "", true)
}

@Entity(tableName = "sms_logs")
data class SmsLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val messageText: String,
    val batchId: String,
    val indexInBatch: Int,
    val totalBatch: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: SmsDeliveryStatus,
    val errorMessage: String? = null,
    val dispatchMode: String = DispatchMode.CLOUD_ANONYMOUS.name,
    val senderId: String = "TX-ALERTS",
    val gatewayProvider: String = GatewayProvider.FAST2SMS.name,
    val isNumberMasked: Boolean = true,
    val simSlot: Int = 0,
    val latencyMs: Long = 0
)

@Entity(tableName = "sms_templates")
data class SmsTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val templateBody: String,
    val defaultCount: Int = 10,
    val defaultDelaySeconds: Float = 0.1f,
    val isFavorite: Boolean = false,
    val isSystemDefault: Boolean = false,
    val usageCount: Int = 0
)

data class ContactItem(
    val name: String,
    val phoneNumber: String
)

data class CloudGatewayConfig(
    val selectedProvider: GatewayProvider = GatewayProvider.FAST2SMS,
    val senderId: String = "TX-ALERTS",
    val apiKey: String = "",
    val twilioAccountSid: String = "",
    val twilioAuthToken: String = "",
    val twilioFromNumber: String = "",
    val customApiUrl: String = "",
    val maskSenderNumber: Boolean = true
)

data class BlastConfig(
    val targetNumber: String = "",
    val messageTemplate: String = "Passcode #{index}: {code} for your security login at {time}",
    val count: Int = 5,
    val delaySeconds: Float = 0.1f,
    val dispatchMode: DispatchMode = DispatchMode.CLOUD_ANONYMOUS,
    val senderId: String = "TX-ALERTS",
    val selectedSimSlot: Int = 0,
    val autoIncrementCode: Boolean = true,
    val isTurboBurstEnabled: Boolean = true,
    val gatewayConfig: CloudGatewayConfig = CloudGatewayConfig()
)

enum class EngineState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    STOPPED,
    ERROR
}

data class TerminalLogLine(
    val id: Long = System.nanoTime(),
    val timestamp: Long = System.currentTimeMillis(),
    val text: String,
    val isSuccess: Boolean = true,
    val isWarning: Boolean = false,
    val isError: Boolean = false
)

data class BlastProgressState(
    val state: EngineState = EngineState.IDLE,
    val currentSent: Int = 0,
    val totalTarget: Int = 0,
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val currentBatchId: String = "",
    val currentPhone: String = "",
    val activeMessagePreview: String = "",
    val elapsedSeconds: Int = 0,
    val logs: List<TerminalLogLine> = emptyList()
)
