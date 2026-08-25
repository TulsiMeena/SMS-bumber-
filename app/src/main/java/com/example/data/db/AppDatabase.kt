package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.SmsDeliveryStatus
import com.example.data.model.SmsLog
import com.example.data.model.SmsTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromStatus(status: SmsDeliveryStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): SmsDeliveryStatus = try {
        SmsDeliveryStatus.valueOf(value)
    } catch (e: Exception) {
        SmsDeliveryStatus.SENT
    }
}

@Database(
    entities = [SmsLog::class, SmsTemplate::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smsDao(): SmsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sms_blast_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).smsDao().insertTemplates(getDefaultTemplates())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private fun getDefaultTemplates(): List<SmsTemplate> {
            return listOf(
                SmsTemplate(
                    title = "🚀 Network Stress Ping",
                    category = "Stress Test",
                    templateBody = "SMS-BLAST-TEST: Packet #{index} of {total} [Sync Code: {code}] at {time}",
                    defaultCount = 10,
                    defaultDelaySeconds = 1.0f,
                    isFavorite = true,
                    isSystemDefault = true
                ),
                SmsTemplate(
                    title = "🔐 OTP Verification Test",
                    category = "Authentication",
                    templateBody = "Your one-time security passkey is {code}. Do NOT share this code with anyone. (Ref #{index})",
                    defaultCount = 5,
                    defaultDelaySeconds = 1.5f,
                    isFavorite = true,
                    isSystemDefault = true
                ),
                SmsTemplate(
                    title = "📢 Critical Alert Broadcast",
                    category = "Broadcasting",
                    templateBody = "URGENT BROADCAST: System maintenance scheduled today. Ticket ID: {code} - Sent {time}",
                    defaultCount = 3,
                    defaultDelaySeconds = 2.0f,
                    isFavorite = false,
                    isSystemDefault = true
                ),
                SmsTemplate(
                    title = "🚨 Emergency SOS Ping",
                    category = "Emergency",
                    templateBody = "EMERGENCY SOS ALERT #{index}: Need urgent response. Timestamp: {time}, Latency check {random}",
                    defaultCount = 5,
                    defaultDelaySeconds = 0.8f,
                    isFavorite = true,
                    isSystemDefault = true
                ),
                SmsTemplate(
                    title = "🛒 Flash Deal Promo",
                    category = "Marketing",
                    templateBody = "Special Flash Deal! Get 40% OFF with voucher #{code}. Offer valid for 24 hours. Batch #{index}",
                    defaultCount = 5,
                    defaultDelaySeconds = 2.0f,
                    isFavorite = false,
                    isSystemDefault = true
                ),
                SmsTemplate(
                    title = "🎉 Holiday Celebration Blast",
                    category = "Social",
                    templateBody = "Wishing you a joyous celebration and immense happiness! Warm regards. Message #{index} ({time})",
                    defaultCount = 5,
                    defaultDelaySeconds = 1.5f,
                    isFavorite = false,
                    isSystemDefault = true
                )
            )
        }
    }
}
