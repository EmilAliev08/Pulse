package com.example.pulse

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object CoverManager {
    private const val PREFS_NAME = "custom_covers"
    private const val KEY_PREFIX = "cover_"

    // Сохранить картинку для трека и вернуть путь
    fun saveCover(context: Context, trackId: Long, uri: Uri): String {
        val fileName = "cover_$trackId.jpg"
        val file = File(context.filesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        val path = file.absolutePath

        // Сохраняем путь в SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PREFIX + trackId, path).apply()

        return path
    }

    // Получить путь к кастомной обложке
    fun getCoverPath(context: Context, trackId: Long): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val path = prefs.getString(KEY_PREFIX + trackId, null)
        return if (path != null && File(path).exists()) path else null
    }

    // Удалить кастомную обложку
    fun removeCover(context: Context, trackId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val path = prefs.getString(KEY_PREFIX + trackId, null)
        if (path != null) File(path).delete()
        prefs.edit().remove(KEY_PREFIX + trackId).apply()
    }
}