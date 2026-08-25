package com.example.data.repository

import com.example.data.db.SmsDao
import com.example.data.model.SmsDeliveryStatus
import com.example.data.model.SmsLog
import com.example.data.model.SmsTemplate
import kotlinx.coroutines.flow.Flow

class SmsRepository(private val smsDao: SmsDao) {

    val allLogs: Flow<List<SmsLog>> = smsDao.getAllLogs()
    val allTemplates: Flow<List<SmsTemplate>> = smsDao.getAllTemplates()
    val totalLogsCount: Flow<Int> = smsDao.getTotalLogsCount()
    val successfulLogsCount: Flow<Int> = smsDao.getSuccessfulLogsCount()

    fun getLogsByStatus(status: SmsDeliveryStatus): Flow<List<SmsLog>> {
        return smsDao.getLogsByStatus(status)
    }

    suspend fun saveLog(log: SmsLog): Long {
        return smsDao.insertLog(log)
    }

    suspend fun deleteLog(id: Long) {
        smsDao.deleteLogById(id)
    }

    suspend fun clearHistory() {
        smsDao.clearAllLogs()
    }

    suspend fun saveTemplate(template: SmsTemplate): Long {
        return smsDao.insertTemplate(template)
    }

    suspend fun updateTemplate(template: SmsTemplate) {
        smsDao.updateTemplate(template)
    }

    suspend fun deleteTemplate(template: SmsTemplate) {
        smsDao.deleteTemplate(template)
    }

    suspend fun incrementTemplateUsage(id: Long) {
        smsDao.incrementTemplateUsage(id)
    }
}
