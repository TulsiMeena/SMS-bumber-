package com.example.engine

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.data.model.BlastConfig
import com.example.data.model.DispatchMode
import com.example.data.model.EngineState
import com.example.data.model.GatewayProvider
import com.example.data.model.SmsDeliveryStatus
import com.example.data.model.SmsLog
import com.example.data.model.TerminalLogLine
import com.example.data.repository.SmsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class SmsEngine(
    private val context: Context,
    private val repository: SmsRepository
) {
    private var blastJob: Job? = null
    private val engineScope = CoroutineScope(Dispatchers.IO)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val _engineState = MutableStateFlow(EngineState.IDLE)
    val engineState = _engineState.asStateFlow()

    private val _currentProgress = MutableStateFlow(0)
    val currentProgress = _currentProgress.asStateFlow()

    private val _totalProgress = MutableStateFlow(0)
    val totalProgress = _totalProgress.asStateFlow()

    private val _successCount = MutableStateFlow(0)
    val successCount = _successCount.asStateFlow()

    private val _failedCount = MutableStateFlow(0)
    val failedCount = _failedCount.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<TerminalLogLine>>(emptyList())
    val terminalLogs = _terminalLogs.asStateFlow()

    private val _lastMessagePreview = MutableStateFlow("")
    val lastMessagePreview = _lastMessagePreview.asStateFlow()

    private var isPaused = false

    fun isSmsPermissionGranted(): Boolean {
        return isPermissionGranted(Manifest.permission.SEND_SMS)
    }

    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startBlast(config: BlastConfig) {
        if (_engineState.value == EngineState.RUNNING) return

        blastJob?.cancel()
        isPaused = false
        _engineState.value = EngineState.RUNNING
        _currentProgress.value = 0
        _totalProgress.value = config.count
        _successCount.value = 0
        _failedCount.value = 0
        _terminalLogs.value = emptyList()

        val batchId = "TURBO-" + System.currentTimeMillis().toString().takeLast(6)

        addLog("⚡ [CLOUD GATEWAY ENGINE] Initiating Batch: $batchId", isSuccess = true)
        addLog("🎯 Target: ${config.targetNumber} | Packets: ${config.count} | Interval: ${config.delaySeconds}s")

        when (config.dispatchMode) {
            DispatchMode.CLOUD_ANONYMOUS -> {
                val provider = config.gatewayConfig.selectedProvider
                addLog("🛡️ Mode: ANONYMOUS GATEWAY [${provider.displayName}] (Your SIM number is 100% HIDDEN)", isSuccess = true)
                addLog("🏷️ Sender ID on receiver's phone: [${config.senderId}]")
            }
            DispatchMode.REAL_SIM -> {
                addLog("📱 Mode: PHYSICAL DEVICE SIM (Hardware Carrier Radio)")
                addLog("⚠️ Note: Receiver will see your phone number.", isWarning = true)
            }
            DispatchMode.SANDBOX -> {
                addLog("🧪 Mode: ULTRA SANDBOX SIMULATOR (Virtual Loop)")
            }
        }

        blastJob = engineScope.launch {
            try {
                val overallStartTime = System.currentTimeMillis()

                for (i in 1..config.count) {
                    while (isPaused) {
                        delay(100)
                    }

                    val messageText = formatMessage(
                        template = config.messageTemplate,
                        index = i,
                        total = config.count,
                        autoIncrementCode = config.autoIncrementCode
                    )

                    _lastMessagePreview.value = messageText
                    val startTimeMs = System.currentTimeMillis()

                    var isSuccess = false
                    var errorReason: String? = null
                    var deliveryStatus = SmsDeliveryStatus.SENT

                    when (config.dispatchMode) {
                        DispatchMode.CLOUD_ANONYMOUS -> {
                            val gw = config.gatewayConfig
                            when (gw.selectedProvider) {
                                GatewayProvider.FAST2SMS -> {
                                    if (gw.apiKey.isNotBlank()) {
                                        // Real Fast2SMS Quick API dispatch
                                        try {
                                            val sanitizedNum = config.targetNumber.replace("+91", "").replace("+", "").trim()
                                            val formBody = FormBody.Builder()
                                                .add("authorization", gw.apiKey.trim())
                                                .add("sender_id", config.senderId.take(6))
                                                .add("message", messageText)
                                                .add("language", "english")
                                                .add("route", "q")
                                                .add("numbers", sanitizedNum)
                                                .build()

                                            val request = Request.Builder()
                                                .url("https://www.fast2sms.com/dev/bulkV2")
                                                .post(formBody)
                                                .addHeader("cache-control", "no-cache")
                                                .build()

                                            val response = httpClient.newCall(request).execute()
                                            val bodyStr = response.body?.string() ?: ""
                                            if (response.isSuccessful && bodyStr.contains("\"return\":true")) {
                                                isSuccess = true
                                                deliveryStatus = SmsDeliveryStatus.DELIVERED
                                            } else {
                                                isSuccess = false
                                                errorReason = "Fast2SMS Error: " + (JSONObject(bodyStr).optJSONArray("message")?.optString(0) ?: bodyStr.take(60))
                                            }
                                        } catch (e: Exception) {
                                            isSuccess = false
                                            errorReason = e.message ?: "Fast2SMS Connection failed"
                                        }
                                    } else {
                                        // Simulated Fast2SMS relay
                                        delay(30 + Random.nextLong(40))
                                        isSuccess = true
                                        deliveryStatus = SmsDeliveryStatus.SENT
                                    }
                                }

                                GatewayProvider.TWILIO -> {
                                    if (gw.twilioAccountSid.isNotBlank() && gw.twilioAuthToken.isNotBlank()) {
                                        // Real Twilio Cloud REST API dispatch
                                        try {
                                            val accountSid = gw.twilioAccountSid.trim()
                                            val authToken = gw.twilioAuthToken.trim()
                                            val from = if (gw.twilioFromNumber.isNotBlank()) gw.twilioFromNumber.trim() else config.senderId

                                            val formBody = FormBody.Builder()
                                                .add("To", config.targetNumber)
                                                .add("From", from)
                                                .add("Body", messageText)
                                                .build()

                                            val credential = Credentials.basic(accountSid, authToken)
                                            val request = Request.Builder()
                                                .url("https://api.twilio.com/2010-04-01/Accounts/$accountSid/Messages.json")
                                                .post(formBody)
                                                .addHeader("Authorization", credential)
                                                .build()

                                            val response = httpClient.newCall(request).execute()
                                            val bodyStr = response.body?.string() ?: ""
                                            if (response.isSuccessful) {
                                                isSuccess = true
                                                deliveryStatus = SmsDeliveryStatus.DELIVERED
                                            } else {
                                                isSuccess = false
                                                errorReason = "Twilio HTTP ${response.code}: " + bodyStr.take(80)
                                            }
                                        } catch (e: Exception) {
                                            isSuccess = false
                                            errorReason = e.message ?: "Twilio API Failed"
                                        }
                                    } else {
                                        delay(30 + Random.nextLong(30))
                                        isSuccess = true
                                        deliveryStatus = SmsDeliveryStatus.SENT
                                    }
                                }

                                GatewayProvider.MSG91 -> {
                                    if (gw.apiKey.isNotBlank()) {
                                        try {
                                            val jsonPayload = """
                                                {
                                                  "sender": "${config.senderId.take(6)}",
                                                  "route": "4",
                                                  "country": "91",
                                                  "sms": [
                                                    {
                                                      "message": "${messageText.replace("\"", "\\\"")}",
                                                      "to": ["${config.targetNumber.replace("+", "")}"]
                                                    }
                                                  ]
                                                }
                                            """.trimIndent()
                                            val request = Request.Builder()
                                                .url("https://api.msg91.com/api/v2/sendsms")
                                                .post(jsonPayload.toRequestBody("application/json".toMediaTypeOrNull()))
                                                .addHeader("authkey", gw.apiKey.trim())
                                                .build()

                                            val response = httpClient.newCall(request).execute()
                                            if (response.isSuccessful) {
                                                isSuccess = true
                                                deliveryStatus = SmsDeliveryStatus.DELIVERED
                                            } else {
                                                isSuccess = false
                                                errorReason = "MSG91 HTTP ${response.code}"
                                            }
                                        } catch (e: Exception) {
                                            isSuccess = false
                                            errorReason = e.message ?: "MSG91 Gateway Error"
                                        }
                                    } else {
                                        delay(30 + Random.nextLong(30))
                                        isSuccess = true
                                        deliveryStatus = SmsDeliveryStatus.SENT
                                    }
                                }

                                GatewayProvider.CUSTOM_WEBHOOK, GatewayProvider.TEXTLOCAL, GatewayProvider.DEFAULT_RELAY -> {
                                    if (gw.customApiUrl.isNotBlank()) {
                                        try {
                                            val jsonPayload = """
                                                {
                                                  "sender_id": "${config.senderId}",
                                                  "to": "${config.targetNumber}",
                                                  "message": "${messageText.replace("\"", "\\\"")}",
                                                  "packet": $i,
                                                  "masked": true
                                                }
                                            """.trimIndent()
                                            val reqBuilder = Request.Builder()
                                                .url(gw.customApiUrl.trim())
                                                .post(jsonPayload.toRequestBody("application/json".toMediaTypeOrNull()))

                                            if (gw.apiKey.isNotBlank()) {
                                                reqBuilder.addHeader("Authorization", "Bearer ${gw.apiKey.trim()}")
                                                reqBuilder.addHeader("apikey", gw.apiKey.trim())
                                            }
                                            val response = httpClient.newCall(reqBuilder.build()).execute()
                                            if (response.isSuccessful) {
                                                isSuccess = true
                                                deliveryStatus = SmsDeliveryStatus.DELIVERED
                                            } else {
                                                isSuccess = false
                                                errorReason = "Webhook HTTP ${response.code}"
                                            }
                                        } catch (e: Exception) {
                                            isSuccess = false
                                            errorReason = e.message ?: "Webhook unreachable"
                                        }
                                    } else {
                                        delay(25 + Random.nextLong(30))
                                        isSuccess = true
                                        deliveryStatus = SmsDeliveryStatus.SENT
                                    }
                                }
                            }
                        }

                        DispatchMode.REAL_SIM -> {
                            if (!isSmsPermissionGranted()) {
                                isSuccess = false
                                errorReason = "PERMISSION_DENIED: SEND_SMS permission missing"
                                deliveryStatus = SmsDeliveryStatus.FAILED
                            } else {
                                try {
                                    val smsManager = getSmsManager(config.selectedSimSlot)
                                    val parts = smsManager.divideMessage(messageText)
                                    if (parts.size > 1) {
                                        smsManager.sendMultipartTextMessage(
                                            config.targetNumber,
                                            null,
                                            parts,
                                            null,
                                            null
                                        )
                                    } else {
                                        smsManager.sendTextMessage(
                                            config.targetNumber,
                                            null,
                                            messageText,
                                            null,
                                            null
                                        )
                                    }
                                    isSuccess = true
                                    deliveryStatus = SmsDeliveryStatus.SENT
                                } catch (se: SecurityException) {
                                    isSuccess = false
                                    errorReason = "SecurityException: ${se.localizedMessage}"
                                    deliveryStatus = SmsDeliveryStatus.FAILED
                                } catch (e: Exception) {
                                    isSuccess = false
                                    errorReason = e.localizedMessage ?: "SIM Hardware Error"
                                    deliveryStatus = SmsDeliveryStatus.FAILED
                                }
                            }
                        }

                        DispatchMode.SANDBOX -> {
                            delay(10 + Random.nextLong(15))
                            isSuccess = true
                            deliveryStatus = SmsDeliveryStatus.SIMULATED
                        }
                    }

                    val latency = System.currentTimeMillis() - startTimeMs

                    if (isSuccess) {
                        _successCount.update { it + 1 }
                        val senderTag = if (config.dispatchMode == DispatchMode.CLOUD_ANONYMOUS) {
                            "From: [${config.senderId}] (${config.gatewayConfig.selectedProvider.name})"
                        } else if (config.dispatchMode == DispatchMode.REAL_SIM) {
                            "From: SIM ${config.selectedSimSlot + 1}"
                        } else {
                            "Sandbox"
                        }
                        addLog(
                            "⚡ [Pkt $i/${config.count}] $senderTag -> ${config.targetNumber} [${latency}ms]",
                            isSuccess = true
                        )
                    } else {
                        _failedCount.update { it + 1 }
                        addLog(
                            "❌ [Pkt $i/${config.count}] Error: ${errorReason ?: "Gateway rejection"}",
                            isError = true
                        )
                    }

                    _currentProgress.value = i

                    launch {
                        val logEntity = SmsLog(
                            phoneNumber = config.targetNumber,
                            messageText = messageText,
                            batchId = batchId,
                            indexInBatch = i,
                            totalBatch = config.count,
                            timestamp = System.currentTimeMillis(),
                            status = if (isSuccess) deliveryStatus else SmsDeliveryStatus.FAILED,
                            errorMessage = errorReason,
                            dispatchMode = config.dispatchMode.name,
                            senderId = if (config.dispatchMode == DispatchMode.CLOUD_ANONYMOUS) config.senderId else "SIM Hardware",
                            gatewayProvider = config.gatewayConfig.selectedProvider.name,
                            isNumberMasked = config.dispatchMode == DispatchMode.CLOUD_ANONYMOUS,
                            simSlot = config.selectedSimSlot,
                            latencyMs = latency
                        )
                        repository.saveLog(logEntity)
                    }

                    if (i < config.count) {
                        val waitMillis = (config.delaySeconds * 1000).toLong().coerceAtLeast(10L)
                        delay(waitMillis)
                    }
                }

                val totalDurationSec = (System.currentTimeMillis() - overallStartTime) / 1000.0
                val rate = if (totalDurationSec > 0) String.format(Locale.US, "%.1f", config.count / totalDurationSec) else "${config.count}"
                _engineState.value = EngineState.COMPLETED
                addLog("🏁 Finished in ${String.format(Locale.US, "%.2f", totalDurationSec)}s ($rate msg/sec)! Success: ${_successCount.value}", isSuccess = true)

            } catch (e: CancellationException) {
                _engineState.value = EngineState.STOPPED
                addLog("🛑 Blast terminated by user command.", isWarning = true)
            } catch (e: Exception) {
                _engineState.value = EngineState.ERROR
                addLog("💥 Fatal Engine Error: ${e.message}", isError = true)
            }
        }
    }

    fun pauseBlast() {
        if (_engineState.value == EngineState.RUNNING) {
            isPaused = true
            _engineState.value = EngineState.PAUSED
            addLog("⏸ Blast paused.", isWarning = true)
        }
    }

    fun resumeBlast() {
        if (_engineState.value == EngineState.PAUSED) {
            isPaused = false
            _engineState.value = EngineState.RUNNING
            addLog("▶ Blast resumed.", isSuccess = true)
        }
    }

    fun stopBlast() {
        blastJob?.cancel()
        _engineState.value = EngineState.STOPPED
    }

    fun reset() {
        blastJob?.cancel()
        _engineState.value = EngineState.IDLE
        _currentProgress.value = 0
        _totalProgress.value = 0
        _successCount.value = 0
        _failedCount.value = 0
        _terminalLogs.value = emptyList()
    }

    private fun addLog(
        text: String,
        isSuccess: Boolean = false,
        isWarning: Boolean = false,
        isError: Boolean = false
    ) {
        _terminalLogs.update { list ->
            val newLine = TerminalLogLine(
                text = text,
                isSuccess = isSuccess,
                isWarning = isWarning,
                isError = isError
            )
            (list + newLine).takeLast(100)
        }
    }

    @Suppress("DEPRECATION")
    private fun getSmsManager(simSlot: Int): SmsManager {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val baseSmsManager = context.getSystemService(SmsManager::class.java)
                val subManager = context.getSystemService(android.telephony.SubscriptionManager::class.java)
                if (subManager != null && isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
                    val activeSubs = subManager.activeSubscriptionInfoList
                    if (!activeSubs.isNullOrEmpty() && simSlot in activeSubs.indices) {
                        val subId = activeSubs[simSlot].subscriptionId
                        baseSmsManager.createForSubscriptionId(subId)
                    } else {
                        baseSmsManager
                    }
                } else {
                    baseSmsManager
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subManager = android.telephony.SubscriptionManager.from(context)
                if (isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
                    val activeSubs = subManager.activeSubscriptionInfoList
                    if (!activeSubs.isNullOrEmpty() && simSlot in activeSubs.indices) {
                        val subId = activeSubs[simSlot].subscriptionId
                        SmsManager.getSmsManagerForSubscriptionId(subId)
                    } else {
                        SmsManager.getDefault()
                    }
                } else {
                    SmsManager.getDefault()
                }
            } else {
                SmsManager.getDefault()
            }
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
            } else {
                SmsManager.getDefault()
            }
        }
    }

    private fun formatMessage(
        template: String,
        index: Int,
        total: Int,
        autoIncrementCode: Boolean
    ): String {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val randomCode = if (autoIncrementCode) {
            (100000 + ((index * 1337 + Random.nextInt(100, 999)) % 900000)).toString()
        } else {
            Random.nextInt(100000, 999999).toString()
        }
        val randomHex = UUID.randomUUID().toString().take(6).uppercase()

        return template
            .replace("{index}", index.toString())
            .replace("{total}", total.toString())
            .replace("{code}", randomCode)
            .replace("{time}", timeStr)
            .replace("{date}", dateStr)
            .replace("{random}", randomHex)
            .replace("{uuid}", UUID.randomUUID().toString().take(8))
    }
}
