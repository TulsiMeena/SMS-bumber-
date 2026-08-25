package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BlastConfig
import com.example.data.model.ContactItem
import com.example.data.model.EngineState
import com.example.data.model.SmsDeliveryStatus
import com.example.data.model.SmsLog
import com.example.data.model.SmsTemplate
import com.example.data.model.TerminalLogLine
import com.example.data.repository.SmsRepository
import com.example.engine.SmsEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SmsBlastViewModel(
    application: Application,
    private val repository: SmsRepository,
    private val smsEngine: SmsEngine
) : AndroidViewModel(application) {

    private val _config = MutableStateFlow(BlastConfig(targetNumber = "+12025550199"))
    val config: StateFlow<BlastConfig> = _config.asStateFlow()

    private val _selectedHistoryFilter = MutableStateFlow<SmsDeliveryStatus?>(null)
    val selectedHistoryFilter: StateFlow<SmsDeliveryStatus?> = _selectedHistoryFilter.asStateFlow()

    val engineState: StateFlow<EngineState> = smsEngine.engineState
    val currentProgress: StateFlow<Int> = smsEngine.currentProgress
    val totalProgress: StateFlow<Int> = smsEngine.totalProgress
    val successCount: StateFlow<Int> = smsEngine.successCount
    val failedCount: StateFlow<Int> = smsEngine.failedCount
    val terminalLogs: StateFlow<List<TerminalLogLine>> = smsEngine.terminalLogs
    val lastMessagePreview: StateFlow<String> = smsEngine.lastMessagePreview

    val templates: StateFlow<List<SmsTemplate>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLogsCount: StateFlow<Int> = repository.totalLogsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val successfulLogsCount: StateFlow<Int> = repository.successfulLogsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _contacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val contacts: StateFlow<List<ContactItem>> = _contacts.asStateFlow()

    val filteredLogs: StateFlow<List<SmsLog>> = combine(
        repository.allLogs,
        _selectedHistoryFilter
    ) { logs, filter ->
        if (filter == null) logs else logs.filter { it.status == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDeviceContacts()
    }

    fun updateTargetNumber(number: String) {
        _config.update { it.copy(targetNumber = number) }
    }

    fun updateMessageTemplate(body: String) {
        _config.update { it.copy(messageTemplate = body) }
    }

    fun updateCount(count: Int) {
        _config.update { it.copy(count = count.coerceIn(1, 100)) }
    }

    fun updateDelay(delaySeconds: Float) {
        _config.update { it.copy(delaySeconds = (Math.round(delaySeconds * 10) / 10f).coerceIn(0.2f, 10.0f)) }
    }

    fun toggleSimulationMode(isSim: Boolean) {
        _config.update { it.copy(isSimulationMode = isSim) }
    }

    fun selectSimSlot(slot: Int) {
        _config.update { it.copy(selectedSimSlot = slot) }
    }

    fun insertTag(tag: String) {
        _config.update {
            it.copy(messageTemplate = it.messageTemplate + " " + tag)
        }
    }

    fun startBlast() {
        if (_config.value.targetNumber.isBlank()) return
        smsEngine.startBlast(_config.value)
    }

    fun pauseBlast() {
        smsEngine.pauseBlast()
    }

    fun resumeBlast() {
        smsEngine.resumeBlast()
    }

    fun stopBlast() {
        smsEngine.stopBlast()
    }

    fun resetBlast() {
        smsEngine.reset()
    }

    fun loadTemplate(template: SmsTemplate) {
        _config.update {
            it.copy(
                messageTemplate = template.templateBody,
                count = template.defaultCount,
                delaySeconds = template.defaultDelaySeconds
            )
        }
        viewModelScope.launch {
            repository.incrementTemplateUsage(template.id)
        }
    }

    fun addCustomTemplate(
        title: String,
        category: String,
        body: String,
        count: Int,
        delay: Float
    ) {
        viewModelScope.launch {
            val template = SmsTemplate(
                title = title.ifBlank { "Custom Blast" },
                category = category.ifBlank { "Custom" },
                templateBody = body,
                defaultCount = count,
                defaultDelaySeconds = delay,
                isFavorite = false,
                isSystemDefault = false
            )
            repository.saveTemplate(template)
        }
    }

    fun deleteTemplate(template: SmsTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }

    fun toggleFavorite(template: SmsTemplate) {
        viewModelScope.launch {
            repository.updateTemplate(template.copy(isFavorite = !template.isFavorite))
        }
    }

    fun setHistoryFilter(filter: SmsDeliveryStatus?) {
        _selectedHistoryFilter.value = filter
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLog(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun resendLog(log: SmsLog) {
        _config.update {
            it.copy(
                targetNumber = log.phoneNumber,
                messageTemplate = log.messageText,
                count = 1
            )
        }
    }

    fun loadDeviceContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contactList = mutableListOf<ContactItem>()
            try {
                val cursor = getApplication<Application>().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC LIMIT 50"
                )
                cursor?.use {
                    val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext()) {
                        val name = if (nameIdx != -1) it.getString(nameIdx) ?: "Contact" else "Contact"
                        val num = if (numIdx != -1) it.getString(numIdx) ?: "" else ""
                        if (num.isNotBlank()) {
                            contactList.add(ContactItem(name, num.replace(" ", "").replace("-", "")))
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback default quick targets if contacts permission is not granted yet
            }

            if (contactList.isEmpty()) {
                contactList.addAll(
                    listOf(
                        ContactItem("Self / Test Sim", "+12025550199"),
                        ContactItem("Echo Test Line", "+14155552671"),
                        ContactItem("Emergency Broadcast", "+18005550100"),
                        ContactItem("Dev Lab Server", "+16505550144")
                    )
                )
            }
            _contacts.value = contactList
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = context.applicationContext as Application
            val db = AppDatabase.getInstance(context)
            val repo = SmsRepository(db.smsDao())
            val engine = SmsEngine(context, repo)
            return SmsBlastViewModel(app, repo, engine) as T
        }
    }
}
