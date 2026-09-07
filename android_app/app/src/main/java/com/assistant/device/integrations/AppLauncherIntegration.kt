package com.assistant.device.integrations

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

class AppLauncherIntegration(private val context: Context) {
    private val TAG = "AppLauncherIntegration"

    fun openAppByName(appName: String): String {
        Log.i(TAG, "Requesting launcher for app: $appName")
        val pm = context.packageManager

        // 1. Query active launcher activities (Android 11+ standard resolution)
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        var appsList = resolveInfos.map {
            Pair(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.distinctBy { it.second }

        if (appsList.isEmpty()) {
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            appsList = packages.map { Pair(pm.getApplicationLabel(it).toString(), it.packageName) }.distinctBy { it.second }
        }

        val targetApp = appsList.find { 
            it.first.contains(appName, ignoreCase = true) || it.second.contains(appName, ignoreCase = true)
        }

        if (targetApp != null) {
            val intent = pm.getLaunchIntentForPackage(targetApp.second)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(intent)
                return "Opening ${targetApp.first}."
            }
        }

        // 2. Common Package Mappings dictionary fallback
        val commonMap = mapOf(
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "music" to "com.google.android.apps.youtube.music",
            "spotify" to "com.spotify.music",
            "gmail" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "calendar" to "com.google.android.calendar",
            "chrome" to "com.android.chrome",
            "messages" to "com.google.android.apps.messaging"
        )
        val cleanName = appName.lowercase()
        for ((key, pkg) in commonMap) {
            if (cleanName.contains(key)) {
                val intent = pm.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    context.startActivity(intent)
                    return "Opening $appName."
                }
            }
        }

        return "Could not find an app named $appName installed on this device."
    }

    fun listInstalledApps(): String {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val appNames = if (resolveInfos.isNotEmpty()) {
            resolveInfos.map { it.loadLabel(pm).toString() }.distinct().sorted()
        } else {
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            packages.map { pm.getApplicationLabel(it).toString() }.distinct().sorted()
        }
        return "Installed apps (${appNames.size}): ${appNames.joinToString(", ")}"
    }
}
