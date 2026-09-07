package com.assistant.device;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0011\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ\u001f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ\u001f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ3\u0010\r\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u000f2\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0012J\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u000b0\nH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\u0015H\'J\u0016\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\u00152\u0006\u0010\u0006\u001a\u00020\u0007H\'J\u0016\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00170\u00152\u0006\u0010\u0006\u001a\u00020\u0007H\'J\u001f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ\u0019\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\u000bH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001cJ\u0019\u0010\u001d\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\b\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001e"}, d2 = {"Lcom/assistant/device/NotificationDao;", "", "deleteAllNotifications", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteOldNotifications", "sinceTime", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllLast24Hours", "", "Lcom/assistant/device/NotificationEntity;", "getOlderNotifications", "getRecentByKeyOrText", "key", "", "pkg", "text", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRecentNotificationsDirect", "getRecentNotificationsFlow", "Lkotlinx/coroutines/flow/Flow;", "getTotalCountFlow", "", "getUnreadCountFlow", "getUnreadLast24Hours", "insert", "notification", "(Lcom/assistant/device/NotificationEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markLast24HoursAsRead", "app_debug"})
@androidx.room.Dao
public abstract interface NotificationDao {
    
    @androidx.room.Insert
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull
    com.assistant.device.NotificationEntity notification, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM notifications WHERE (notification_key = :key OR (package_name = :pkg AND text = :text)) AND timestamp >= :sinceTime LIMIT 1")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getRecentByKeyOrText(@org.jetbrains.annotations.NotNull
    java.lang.String key, @org.jetbrains.annotations.NotNull
    java.lang.String pkg, @org.jetbrains.annotations.NotNull
    java.lang.String text, long sinceTime, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.assistant.device.NotificationEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM notifications WHERE timestamp >= :sinceTime AND is_read = 0 ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getUnreadLast24Hours(long sinceTime, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.assistant.device.NotificationEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM notifications WHERE timestamp >= :sinceTime ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getAllLast24Hours(long sinceTime, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.assistant.device.NotificationEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM notifications WHERE timestamp < :sinceTime ORDER BY timestamp DESC LIMIT 100")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getOlderNotifications(long sinceTime, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.assistant.device.NotificationEntity>> $completion);
    
    @androidx.room.Query(value = "UPDATE notifications SET is_read = 1 WHERE timestamp >= :sinceTime AND is_read = 0")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object markLast24HoursAsRead(long sinceTime, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM notifications WHERE timestamp >= :sinceTime AND is_read = 0")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getUnreadCountFlow(long sinceTime);
    
    @androidx.room.Query(value = "SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 50")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.assistant.device.NotificationEntity>> getRecentNotificationsFlow();
    
    @androidx.room.Query(value = "SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 50")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getRecentNotificationsDirect(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.assistant.device.NotificationEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM notifications WHERE timestamp >= :sinceTime")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getTotalCountFlow(long sinceTime);
    
    @androidx.room.Query(value = "DELETE FROM notifications WHERE timestamp < :sinceTime")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteOldNotifications(long sinceTime, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM notifications")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteAllNotifications(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}