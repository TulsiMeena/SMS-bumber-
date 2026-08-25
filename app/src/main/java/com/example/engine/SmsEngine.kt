package com.example.engine

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.data.model.BlastConfig
import com.example.data.model.EngineState
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class SmsEngine(
    private val context: Context,
    private val repository: SmsRepository
) {
    private var blastJob: Job? = null
    private val engineScope = CoroutineScope(Dispatchers.IO)

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
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
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

        val batchId = "BATCH-" + System.currentTimeMillis().toString().takeLast(6)

        addLog("🚀 Initiating SMS Blast Engine [Batch: $batchId]", isSuccess = true)
        addLog("🎯 Target: ${config.targetNumber} | Total Packets: ${config.count} | Interval: ${config.delaySeconds}s")
        if (config.isSimulationMode) {
            addLog("🧪 Mode: SIMULATED / SANDBOX (Zero carrier charges, virtual carrier loop)")
        } else {
            addLog("⚡ Mode: REAL HARDWARE SMS (Direct carrier radio dispatch via SIM ${config.selectedSimSlot + 1})")
        }

        blastJob = engineScope.launch {
            try {
                val smsManager = getSmsManager(config.selectedSimSlot)

                for (i in 1..config.count) {
                    while (isPaused) {
                        delay(200)
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

                    if (config.isSimulationMode) {
                        // High fidelity carrier network response simulation
                        delay(120 + Random.nextLong(180))
                        isSuccess = true
                    } else {
                        // Real Carrier Hardware SMS Dispatch
                        if (!isSmsPermissionGranted()) {
                            isSuccess = false
                            errorReason = "PERMISSION_DENIED: SEND_SMS permission missing"
                        } else {
                            try {
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
                            } catch (se: SecurityException) {
                                isSuccess = false
                                errorReason = "SecurityException: ${se.localizedMessage}"
                            } catch (e: Exception) {
                                isSuccess = false
                                errorReason = e.localizedMessage ?: "Unknown hardware error"
                            }
                        }
                    }

                    val latency = System.currentTimeMillis() - startTimeMs

                    if (isSuccess) {
                        _successCount.update { it + 1 }
                        addLog(
                            "✔ [Packet $i/${config.count}] Dispatched -> ${config.targetNumber} (${latency}ms)",
                            isSuccess = true
                        )
                    } else {
                        _failedCount.update { it + 1 }
                        addLog(
                            "❌ [Packet $i/${config.count}] Failed: ${errorReason ?: "Carrier rejection"}",
                            isError = true
                        )
                    }

                    _currentProgress.value = i

                    // Save to Room DB
                    val logEntity = SmsLog(
                        phoneNumber = config.targetNumber,
                        messageText = messageText,
                        batchId = batchId,
                        indexInBatch = i,
                        totalBatch = config.count,
                        timestamp = System.currentTimeMillis(),
                        status = when {
                            config.isSimulationMode -> SmsDeliveryStatus.SIMULATED
                            isSuccess -> SmsDeliveryStatus.SENT
                            else -> SmsDeliveryStatus.FAILED
                        },
                        errorMessage = errorReason,
                        isSimulation = config.isSimulationMode,
                        simSlot = config.selectedSimSlot,
                        latencyMs = latency
                    )
                    repository.saveLog(logEntity)

                    if (i < config.count) {
                        val waitMillis = (config.delaySeconds * 1000).toLong().coerceAtLeast(100L)
                        delay(waitMillis)
                    }
                }

                _engineState.value = EngineState.COMPLETED
                addLog("🏁 Blast Complete! Success: ${_successCount.value}, Failed: ${_failedCount.value}", isSuccess = true)

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
            addLog("⏸ Blast paused. Ready to resume.", isWarning = true)
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

    private fun getSmsManager(simSlot: Int): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
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
