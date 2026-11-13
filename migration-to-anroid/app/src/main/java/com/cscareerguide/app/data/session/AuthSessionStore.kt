@file:Suppress("DEPRECATION")

package com.cscareerguide.app.data.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.cscareerguide.app.data.bridge.UserPayload
import com.cscareerguide.app.data.model.UserProfile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

interface AuthSessionStore {
    fun token(): String?
    fun user(): UserProfile?
    fun save(token: String, user: UserProfile)
    fun clear()
}

class EncryptedAuthSessionStore(
    context: Context,
    private val json: Json = Json,
) : AuthSessionStore {
    private val preferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun token(): String? = preferences.getString(KEY_TOKEN, null)

    override fun user(): UserProfile? {
        val raw = preferences.getString(KEY_USER, null) ?: return null
        return runCatching {
            UserPayload.decode(json.parseToJsonElement(raw).jsonObject)
        }.getOrNull()
    }

    override fun save(token: String, user: UserProfile) {
        preferences.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, UserPayload.encode(user).toString())
            .apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "career_secure_session"
        const val KEY_TOKEN = "token"
        const val KEY_USER = "user"
    }
}
