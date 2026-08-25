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
    val isSimulation: Boolean = false,
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
    val defaultCount: Int = 5,
    val defaultDelaySeconds: Float = 1.5f,
    val isFavorite: Boolean = false,
    val isSystemDefault: Boolean = false,
    val usageCount: Int = 0
)

data class ContactItem(
    val name: String,
    val phoneNumber: String
)

data class BlastConfig(
    val targetNumber: String = "",
    val messageTemplate: String = "Test packet #{index} from SMS Blast Pro [Code: {code}] at {time}",
    val count: Int = 5,
    val delaySeconds: Float = 1.0f,
    val isSimulationMode: Boolean = true,
    val selectedSimSlot: Int = 0,
    val autoIncrementCode: Boolean = true
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
