package com.assistant.device;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\t\u001a\u00020\nH\u0016J\u0010\u0010\u000b\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\rH\u0016J\u0010\u0010\u000e\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\rH\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/assistant/device/NotificationService;", "Landroid/service/notification/NotificationListenerService;", "()V", "TAG", "", "database", "Lcom/assistant/device/AssistantDatabase;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "onCreate", "", "onNotificationPosted", "sbn", "Landroid/service/notification/StatusBarNotification;", "onNotificationRemoved", "app_debug"})
public final class NotificationService extends android.service.notification.NotificationListenerService {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String TAG = "NotificationService";
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    private com.assistant.device.AssistantDatabase database;
    
    public NotificationService() {
        super();
    }
    
    @java.lang.Override
    public void onCreate() {
    }
    
    @java.lang.Override
    public void onNotificationPosted(@org.jetbrains.annotations.NotNull
    android.service.notification.StatusBarNotification sbn) {
    }
    
    @java.lang.Override
    public void onNotificationRemoved(@org.jetbrains.annotations.NotNull
    android.service.notification.StatusBarNotification sbn) {
    }
}