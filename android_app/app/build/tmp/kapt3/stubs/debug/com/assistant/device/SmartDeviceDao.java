package com.assistant.device;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0005J\u0014\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00030\u0007H\'J\u001b\u0010\b\u001a\u0004\u0018\u00010\u00042\u0006\u0010\t\u001a\u00020\nH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000bJ\u0018\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u00072\u0006\u0010\t\u001a\u00020\nH\'J\u0019\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0004H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0010\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0011"}, d2 = {"Lcom/assistant/device/SmartDeviceDao;", "", "getAllDevicesDirect", "", "Lcom/assistant/device/SmartDeviceEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllDevicesFlow", "Lkotlinx/coroutines/flow/Flow;", "getDeviceById", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDeviceFlowById", "insertOrUpdate", "", "device", "(Lcom/assistant/device/SmartDeviceEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao
public abstract interface SmartDeviceDao {
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertOrUpdate(@org.jetbrains.annotations.NotNull
    com.assistant.device.SmartDeviceEntity device, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM smart_devices")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.assistant.device.SmartDeviceEntity>> getAllDevicesFlow();
    
    @androidx.room.Query(value = "SELECT * FROM smart_devices")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getAllDevicesDirect(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.assistant.device.SmartDeviceEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM smart_devices WHERE id = :id")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getDeviceById(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.assistant.device.SmartDeviceEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM smart_devices WHERE id = :id")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<com.assistant.device.SmartDeviceEntity> getDeviceFlowById(@org.jetbrains.annotations.NotNull
    java.lang.String id);
}