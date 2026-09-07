package com.assistant.device;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\t\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\u0018\u00002\u00020\u0001B)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\u0014\u001a\u00020\u00052\u0006\u0010\u0015\u001a\u00020\u00052\u0006\u0010\u0016\u001a\u00020\u0005H\u0002J!\u0010\u0017\u001a\u00020\u00052\u0006\u0010\u0018\u001a\u00020\u00052\u0006\u0010\u0019\u001a\u00020\u001aH\u0082@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001bJ\u0010\u0010\u001c\u001a\u00020\u00052\u0006\u0010\u001d\u001a\u00020\u0005H\u0002J\u0010\u0010\u001e\u001a\u00020\u00052\u0006\u0010\u0018\u001a\u00020\u0005H\u0002J\u0018\u0010\u001f\u001a\u00020\u00052\u0006\u0010 \u001a\u00020\u00052\u0006\u0010!\u001a\u00020\u0005H\u0002J\u0019\u0010\"\u001a\u00020\u00052\u0006\u0010#\u001a\u00020\u0005H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010$J\u0019\u0010%\u001a\u00020\u00052\u0006\u0010#\u001a\u00020\u0005H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010$J\u0010\u0010&\u001a\u00020\u00052\u0006\u0010\'\u001a\u00020(H\u0002J\u0010\u0010)\u001a\u00020\u00052\u0006\u0010*\u001a\u00020\u0005H\u0002J\u0006\u0010+\u001a\u00020,R\u000e\u0010\t\u001a\u00020\u0005X\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006-"}, d2 = {"Lcom/assistant/device/OllamaClient;", "", "context", "Landroid/content/Context;", "endpointUrl", "", "modelName", "apiKey", "(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "TAG", "chatHistory", "", "Lorg/json/JSONObject;", "client", "Lokhttp3/OkHttpClient;", "database", "Lcom/assistant/device/AssistantDatabase;", "jsonMediaType", "Lokhttp3/MediaType;", "sanitizedEndpoint", "executeCreateCalendarEvent", "title", "startTime", "executeHistoricalSearch", "query", "sinceTime", "", "(Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "executeOpenApp", "appName", "executeWebSearch", "executeWhatsAppMessage", "contact", "message", "getResponse", "userQuery", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getWebResponse", "queryOllama", "messages", "Lorg/json/JSONArray;", "queryOllamaGenerate", "prompt", "resetChatSession", "", "app_debug"})
public final class OllamaClient {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String endpointUrl = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String modelName = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String apiKey = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String TAG = "OllamaClient";
    @org.jetbrains.annotations.NotNull
    private final com.assistant.device.AssistantDatabase database = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String sanitizedEndpoint = null;
    @org.jetbrains.annotations.NotNull
    private final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull
    private final okhttp3.MediaType jsonMediaType = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<org.json.JSONObject> chatHistory = null;
    
    public OllamaClient(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String endpointUrl, @org.jetbrains.annotations.NotNull
    java.lang.String modelName, @org.jetbrains.annotations.Nullable
    java.lang.String apiKey) {
        super();
    }
    
    public final void resetChatSession() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getResponse(@org.jetbrains.annotations.NotNull
    java.lang.String userQuery, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.String queryOllama(org.json.JSONArray messages) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getWebResponse(@org.jetbrains.annotations.NotNull
    java.lang.String userQuery, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.String queryOllamaGenerate(java.lang.String prompt) {
        return null;
    }
    
    private final java.lang.Object executeHistoricalSearch(java.lang.String query, long sinceTime, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.String executeWebSearch(java.lang.String query) {
        return null;
    }
    
    private final java.lang.String executeWhatsAppMessage(java.lang.String contact, java.lang.String message) {
        return null;
    }
    
    private final java.lang.String executeCreateCalendarEvent(java.lang.String title, java.lang.String startTime) {
        return null;
    }
    
    private final java.lang.String executeOpenApp(java.lang.String appName) {
        return null;
    }
}