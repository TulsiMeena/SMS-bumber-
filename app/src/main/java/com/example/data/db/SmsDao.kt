package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SmsDeliveryStatus
import com.example.data.model.SmsLog
import com.example.data.model.SmsTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    // SMS Logs
    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SmsLog>>

    @Query("SELECT * FROM sms_logs WHERE status = :status ORDER BY timestamp DESC")
    fun getLogsByStatus(status: SmsDeliveryStatus): Flow<List<SmsLog>>

    @Query("SELECT * FROM sms_logs WHERE batchId = :batchId ORDER BY indexInBatch ASC")
    fun getLogsForBatch(batchId: String): Flow<List<SmsLog>>

    @Query("SELECT COUNT(*) FROM sms_logs")
    fun getTotalLogsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sms_logs WHERE status = 'SENT' OR status = 'DELIVERED'")
    fun getSuccessfulLogsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SmsLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<SmsLog>)

    @Query("DELETE FROM sms_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM sms_logs")
    suspend fun clearAllLogs()

    // Templates
    @Query("SELECT * FROM sms_templates ORDER BY isFavorite DESC, usageCount DESC, id ASC")
    fun getAllTemplates(): Flow<List<SmsTemplate>>

    @Query("SELECT * FROM sms_templates WHERE category = :category ORDER BY id ASC")
    fun getTemplatesByCategory(category: String): Flow<List<SmsTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: SmsTemplate): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplates(templates: List<SmsTemplate>)

    @Update
    suspend fun updateTemplate(template: SmsTemplate)

    @Delete
    suspend fun deleteTemplate(template: SmsTemplate)

    @Query("UPDATE sms_templates SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementTemplateUsage(id: Long)
}
