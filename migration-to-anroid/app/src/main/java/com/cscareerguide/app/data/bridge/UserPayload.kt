package com.cscareerguide.app.data.bridge

import com.cscareerguide.app.data.model.UserProfile
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

internal object UserPayload {
    fun decode(payload: JsonObject): UserProfile {
        val username = payload.string("username")
        return UserProfile(
            id = payload["id"]?.let { (it as? JsonPrimitive)?.content }.orEmpty(),
            username = username,
            displayName = payload.string("name").ifBlank { username },
            school = payload.string("school"),
            major = payload.string("major"),
            grade = payload.string("grade"),
            profile = payload.string("profile"),
            education = payload.string("education"),
            email = payload.string("email"),
            address = payload.string("address"),
        )
    }

    fun encode(user: UserProfile): JsonObject = buildJsonObject {
        put("id", user.id)
        put("username", user.username)
        put("name", user.displayName)
        put("school", user.school)
        put("major", user.major)
        put("grade", user.grade)
        put("profile", user.profile)
        put("education", user.education)
        put("email", user.email)
        put("address", user.address)
    }
}

internal fun JsonObject.string(key: String): String =
    (this[key] as? JsonPrimitive)?.contentOrNull.orEmpty()

internal fun JsonObject.optionalString(key: String): String? =
    (this[key] as? JsonPrimitive)?.contentOrNull

internal fun JsonObject.boolean(key: String): Boolean =
    (this[key] as? JsonPrimitive)?.contentOrNull?.toBooleanStrictOrNull() == true
