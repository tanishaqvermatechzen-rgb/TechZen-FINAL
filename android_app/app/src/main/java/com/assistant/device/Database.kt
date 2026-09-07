package com.assistant.device

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "sender") val sender: String?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "is_read") val isReadToUser: Boolean = false,
    @ColumnInfo(name = "notification_key") val notificationKey: String = ""
)

@Entity(tableName = "smart_devices")
data class SmartDeviceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: String, // "light", "thermostat", "lock", "plug"
    @ColumnInfo(name = "is_on") val isOn: Boolean,
    @ColumnInfo(name = "value") val value: Int = 0, // e.g. brightness or temperature
    @ColumnInfo(name = "status_text") val statusText: String = "" // e.g. "Locked" or "Cool Mode"
)

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE (notification_key = :key OR (package_name = :pkg AND text = :text)) AND timestamp >= :sinceTime LIMIT 1")
    suspend fun getRecentByKeyOrText(key: String, pkg: String, text: String, sinceTime: Long): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE timestamp >= :sinceTime AND is_read = 0 ORDER BY timestamp DESC")
    suspend fun getUnreadLast24Hours(sinceTime: Long): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE timestamp >= :sinceTime ORDER BY timestamp DESC")
    suspend fun getAllLast24Hours(sinceTime: Long): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE timestamp < :sinceTime ORDER BY timestamp DESC LIMIT 100")
    suspend fun getOlderNotifications(sinceTime: Long): List<NotificationEntity>

    @Query("UPDATE notifications SET is_read = 1 WHERE timestamp >= :sinceTime AND is_read = 0")
    suspend fun markLast24HoursAsRead(sinceTime: Long)

    @Query("SELECT COUNT(*) FROM notifications WHERE timestamp >= :sinceTime AND is_read = 0")
    fun getUnreadCountFlow(sinceTime: Long): Flow<Int>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 50")
    fun getRecentNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentNotificationsDirect(): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE timestamp >= :sinceTime")
    fun getTotalCountFlow(sinceTime: Long): Flow<Int>

    @Query("DELETE FROM notifications WHERE timestamp < :sinceTime")
    suspend fun deleteOldNotifications(sinceTime: Long)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
}

@Dao
interface SmartDeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(device: SmartDeviceEntity)

    @Query("SELECT * FROM smart_devices")
    fun getAllDevicesFlow(): Flow<List<SmartDeviceEntity>>

    @Query("SELECT * FROM smart_devices")
    suspend fun getAllDevicesDirect(): List<SmartDeviceEntity>

    @Query("SELECT * FROM smart_devices WHERE id = :id")
    suspend fun getDeviceById(id: String): SmartDeviceEntity?

    @Query("SELECT * FROM smart_devices WHERE id = :id")
    fun getDeviceFlowById(id: String): Flow<SmartDeviceEntity?>
}

@Entity(tableName = "voice_chats")
data class VoiceChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "user_query") val userQuery: String,
    @ColumnInfo(name = "assistant_response") val assistantResponse: String,
    @ColumnInfo(name = "provider") val provider: String = "Gemini"
)

@Dao
interface VoiceChatDao {
    @Insert
    suspend fun insert(chat: VoiceChatEntity)

    @Query("SELECT * FROM voice_chats ORDER BY timestamp DESC LIMIT 50")
    fun getRecentVoiceChatsFlow(): Flow<List<VoiceChatEntity>>

    @Query("DELETE FROM voice_chats WHERE timestamp < :sinceTime")
    suspend fun deleteOldVoiceChats(sinceTime: Long)
}

@Database(entities = [NotificationEntity::class, SmartDeviceEntity::class, VoiceChatEntity::class], version = 3, exportSchema = false)
abstract class AssistantDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun smartDeviceDao(): SmartDeviceDao
    abstract fun voiceChatDao(): VoiceChatDao

    companion object {
        @Volatile
        private var INSTANCE: AssistantDatabase? = null

        fun getDatabase(context: Context): AssistantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AssistantDatabase::class.java,
                    "assistant_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
