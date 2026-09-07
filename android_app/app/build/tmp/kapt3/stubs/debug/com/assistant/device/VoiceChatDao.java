package com.assistant.device;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u0019\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bH\'J\u0019\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\nH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\r\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u000e"}, d2 = {"Lcom/assistant/device/VoiceChatDao;", "", "deleteOldVoiceChats", "", "sinceTime", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRecentVoiceChatsFlow", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/assistant/device/VoiceChatEntity;", "insert", "chat", "(Lcom/assistant/device/VoiceChatEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao
public abstract interface VoiceChatDao {
    
    @androidx.room.Insert
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull
    com.assistant.device.VoiceChatEntity chat, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM voice_chats ORDER BY timestamp DESC LIMIT 50")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.assistant.device.VoiceChatEntity>> getRecentVoiceChatsFlow();
    
    @androidx.room.Query(value = "DELETE FROM voice_chats WHERE timestamp < :sinceTime")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteOldVoiceChats(long sinceTime, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}