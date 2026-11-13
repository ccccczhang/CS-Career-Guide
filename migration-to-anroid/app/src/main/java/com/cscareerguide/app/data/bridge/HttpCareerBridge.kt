package com.cscareerguide.app.data.bridge

import com.cscareerguide.app.data.PagedData
import com.cscareerguide.app.data.model.CatalogItem
import com.cscareerguide.app.data.model.CatalogKind
import com.cscareerguide.app.data.model.JobSummary
import com.cscareerguide.app.data.model.PracticeRetrieval
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.SeniorAdvice
import com.cscareerguide.app.data.model.UserProfile
import com.cscareerguide.app.data.session.AuthSessionStore
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class HttpCareerBridge(
    apiBaseUrl: String,
    private val sessionStore: AuthSessionStore,
    private val client: OkHttpClient = OkHttpClient(),
    private val allowCleartext: Boolean,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : CareerBridge {
    private val baseUrl: HttpUrl? = validateBaseUrl(apiBaseUrl, allowCleartext)
    private val streamingClient = client.newBuilder()
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val configurationError = if (baseUrl == null) {
        if (apiBaseUrl.isBlank()) {
            "API_BASE_URL 未配置。"
        } else {
            "API_BASE_URL 无效；必须是不含 /api 的 HTTP(S) 根地址。"
        }
    } else {
        null
    }

    override suspend fun login(username: String, password: String): BridgeResult<AuthSession> {
        val body = buildJsonObject {
            put("username", username)
            put("password", password)
        }
        return request(
            route = BridgeRoutes.LOGIN,
            method = "POST",
            body = body,
            authenticated = false,
            decode = ::decodeAuthSession,
        )
    }

    override suspend fun register(
        username: String,
        password: String,
        confirmation: String,
    ): BridgeResult<AuthSession> {
        val body = buildJsonObject {
            put("username", username)
            put("password", password)
            put("confirm_password", confirmation)
            put("email", "")
        }
        return request(
            route = BridgeRoutes.REGISTER,
            method = "POST",
            body = body,
            authenticated = false,
            decode = ::decodeAuthSession,
        )
    }

    override suspend fun logout(token: String): BridgeResult<Unit> = request(
        route = BridgeRoutes.LOGOUT,
        method = "POST",
        body = buildJsonObject { },
        authenticated = true,
        authorizationToken = token,
        decode = { Unit },
    )

    override suspend fun profile(): BridgeResult<UserProfile> = request(
        route = BridgeRoutes.PROFILE,
        method = "GET",
        authenticated = true,
        decode = { payload -> UserPayload.decode(payload.jsonObject) },
    )

    override suspend fun updateProfile(update: ProfileUpdate): BridgeResult<UserProfile> {
        val body = buildJsonObject {
            put("name", update.displayName)
            put("school", update.school)
            put("major", update.major)
            put("grade", update.grade)
            put("education", update.education)
            put("email", update.email)
            put("address", update.address)
            put("profile", update.profile)
        }
        return request(
            route = BridgeRoutes.PROFILE,
            method = "PUT",
            body = body,
            authenticated = true,
            decode = { payload -> UserPayload.decode(payload.jsonObject) },
        )
    }

    override suspend fun catalog(query: CatalogBridgeQuery): BridgeResult<PagedData<CatalogItem>> =
        request(
            route = BridgeRoutes.catalog(query.kind),
            method = "GET",
            authenticated = true,
            queryParameters = catalogParameters(query),
            decode = { decodeCatalogPage(it, query.kind) },
        )

    override suspend fun resume(): BridgeResult<ResumeDraft?> {
        val result = request(
            route = BridgeRoutes.RESUME,
            method = "GET",
            authenticated = true,
            decode = ::decodeResume,
        )
        return if (result is BridgeResult.Failure && result.error.httpStatus == 404) {
            BridgeResult.Success(null)
        } else {
            result
        }
    }

    override suspend fun saveResume(draft: ResumeDraft): BridgeResult<ResumeDraft> = request(
        route = BridgeRoutes.RESUME,
        method = if (draft.id.isBlank()) "POST" else "PUT",
        body = encodeResume(draft),
        authenticated = true,
        decode = ::decodeResume,
    )

    override suspend fun uploadResumeAvatar(
        bytes: ByteArray,
        fileName: String,
        mediaType: String,
    ): BridgeResult<String> {
        val root = baseUrl ?: return BridgeResult.NotConfigured(configurationError.orEmpty())
        val token = sessionStore.token() ?: return authenticationRequired(
            BridgeRoutes.RESUME_AVATAR_UPLOAD,
        )
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "avatar",
                fileName,
                bytes.toRequestBody(mediaType.toMediaType()),
            )
            .build()
        val request = Request.Builder()
            .url(root.resolve(BridgeRoutes.RESUME_AVATAR_UPLOAD)!!)
            .header("Accept", "application/json")
            .header("Authorization", "Token $token")
            .post(body)
            .build()
        return execute(request, BridgeRoutes.RESUME_AVATAR_UPLOAD) {
            it.jsonObject.string("avatar_url")
        }
    }

    override suspend fun deleteResumeAvatar(): BridgeResult<Unit> = request(
        route = BridgeRoutes.RESUME_AVATAR_DELETE,
        method = "DELETE",
        authenticated = true,
        decode = { Unit },
    )

    override suspend fun analyzeResume(
        request: ResumeAnalysisRequest,
    ): BridgeResult<JsonElement> = request(
        route = BridgeRoutes.RESUME_ANALYSES,
        method = "POST",
        body = buildJsonObject {
            put("jd_text", request.jdText)
            put("resume_revision", request.resumeRevision)
            put("request_id", request.requestId)
        },
        authenticated = true,
        decode = { it },
    )

    override suspend fun applyResumeAnalysis(
        request: ResumeApplyRequest,
    ): BridgeResult<JsonElement> = request(
        route = BridgeRoutes.applyResumeAnalysis(request.analysisId),
        method = "POST",
        body = buildJsonObject {
            put("base_revision", request.baseRevision)
            put("request_id", request.requestId)
            put(
                "accepted_suggestion_ids",
                kotlinx.serialization.json.buildJsonArray {
                    request.acceptedSuggestionIds.forEach { add(JsonPrimitive(it)) }
                },
            )
        },
        authenticated = true,
        decode = { it },
    )

    override suspend fun jobs(
        search: String,
        page: Int,
        pageSize: Int,
    ): BridgeResult<PagedData<JobSummary>> = request(
        route = BridgeRoutes.COMPANY_JOBS,
        method = "GET",
        authenticated = true,
        queryParameters = buildMap {
            if (search.isNotBlank()) put("search", search)
            put("page", page.coerceAtLeast(1).toString())
            put("page_size", pageSize.coerceIn(1, 100).toString())
        },
        decode = ::decodeJobsPage,
    )

    override suspend fun seniorAdvice(
        search: String,
        ordering: String,
        page: Int,
        pageSize: Int,
    ): BridgeResult<PagedData<SeniorAdvice>> = request(
        route = BridgeRoutes.SENIOR_ADVICE,
        method = "GET",
        authenticated = true,
        queryParameters = buildMap {
            if (search.isNotBlank()) put("search", search)
            put("ordering", ordering)
            put("page", page.coerceAtLeast(1).toString())
            put("page_size", pageSize.coerceIn(1, 50).toString())
        },
        decode = ::decodeAdvicePage,
    )

    override suspend fun retrieveQuestions(
        request: PracticeRetrieveRequest,
    ): BridgeResult<PracticeRetrieval> {
        if (request.role.isBlank() || request.requestId.isBlank()) {
            return invalidRequest(
                BridgeRoutes.QUESTION_RETRIEVE,
                "岗位方向和 request_id 不能为空。",
            )
        }
        if (request.jobId != null && request.jdText.isNotBlank()) {
            return invalidRequest(
                BridgeRoutes.QUESTION_RETRIEVE,
                "job_id 和 jd_text 不能同时填写。",
            )
        }
        val body = buildJsonObject {
            put("role", request.role)
            if (request.topic.isNotBlank()) put("topic", request.topic)
            put("difficulty", request.difficulty.ifBlank { "easy" })
            request.jobId?.let { put("job_id", it) }
            if (request.jdText.isNotBlank()) put("jd_text", request.jdText)
            put("top_k", request.topK.coerceIn(1, 20))
            put("request_id", request.requestId)
        }
        return request(
            route = BridgeRoutes.QUESTION_RETRIEVE,
            method = "POST",
            body = body,
            authenticated = true,
            decode = ::decodePracticeRetrieval,
        )
    }

    override suspend fun listPracticeSessions(): BridgeResult<Unit> =
        notImplemented(BridgeRoutes.PRACTICE_SESSIONS)

    override suspend fun listEvaluationRuns(): BridgeResult<Unit> =
        notImplemented(BridgeRoutes.EVALUATION_RUNS)

    override suspend fun streamInterviewChat(
        request: InterviewChatRequest,
        onEvent: (InterviewStreamEvent) -> Unit,
    ): BridgeResult<InterviewStreamResult> {
        val root = baseUrl ?: return BridgeResult.NotConfigured(configurationError ?: BridgeRoutes.INTERVIEW_CHAT)
        val token = sessionStore.token()
            ?: return authenticationRequired(BridgeRoutes.INTERVIEW_CHAT)
        val body = buildJsonObject {
            put("message", request.message)
            put("mode", request.mode)
            put("interview_style", request.style)
            put("session_id", request.sessionId)
            put("enable_tts", request.enableTts)
        }.toString().toRequestBody(JSON_MEDIA_TYPE)
        val url = root.resolve(BridgeRoutes.INTERVIEW_CHAT)
            ?: return configurationFailure(BridgeRoutes.INTERVIEW_CHAT)
        val httpRequest = Request.Builder()
            .url(url)
            .header("Accept", "text/event-stream")
            .header("Authorization", "Token $token")
            .post(body)
            .build()
        return executeInterviewStream(httpRequest, BridgeRoutes.INTERVIEW_CHAT, onEvent)
    }

    private suspend fun executeInterviewStream(
        request: Request,
        route: String,
        onEvent: (InterviewStreamEvent) -> Unit,
    ): BridgeResult<InterviewStreamResult> = suspendCancellableCoroutine { continuation ->
        val call = streamingClient.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!continuation.isActive) return
                continuation.resume(
                    BridgeResult.Failure(
                        route,
                        BridgeError(
                            httpStatus = null,
                            businessCode = null,
                            message = "面试流连接失败，请稍后重试。",
                            details = null,
                            fieldErrors = emptyMap(),
                            retryable = true,
                            authExpired = false,
                        ),
                    ),
                )
            }

            override fun onResponse(call: Call, response: Response) {
                if (!continuation.isActive) {
                    response.close()
                    return
                }
                response.use {
                    if (!it.isSuccessful) {
                        val text = runCatching { it.body?.string().orEmpty() }.getOrElse { "" }
                        if (it.code == 401) sessionStore.clear()
                        continuation.resume(
                            BridgeResult.Failure(route, parseHttpError(it.code, text)),
                        )
                        return
                    }
                    val body = it.body ?: run {
                        continuation.resume(incompleteStreamFailure(route, "面试响应没有数据流。"))
                        return
                    }
                    val parsed = runCatching {
                        parseInterviewSse(body.source(), json, onEvent)
                    }.getOrElse { error ->
                        continuation.resume(
                            incompleteStreamFailure(
                                route,
                                if (error is IOException) {
                                    "面试流读取中断，请稍后重试。"
                                } else {
                                    "面试流响应无法解析。"
                                },
                            ),
                        )
                        return
                    }
                    val failureMessage = when {
                        parsed.serverError != null -> parsed.serverError
                        !parsed.receivedData -> "面试接口未返回流式数据。"
                        !parsed.done || !parsed.ended ->
                            "面试流未完整结束，已保留收到的文字。"
                        else -> null
                    }
                    if (failureMessage != null) {
                        continuation.resume(
                            BridgeResult.Failure(
                                route,
                                BridgeError(
                                    httpStatus = it.code,
                                    businessCode = if (parsed.serverError != null) {
                                        "INTERVIEW_STREAM_ERROR"
                                    } else {
                                        "INTERVIEW_STREAM_INCOMPLETE"
                                    },
                                    message = failureMessage,
                                    details = null,
                                    fieldErrors = emptyMap(),
                                    retryable = true,
                                    authExpired = false,
                                ),
                            ),
                        )
                    } else {
                        continuation.resume(
                            BridgeResult.Success(
                                InterviewStreamResult(
                                    content = parsed.content,
                                    sessionId = parsed.sessionId,
                                    done = parsed.done,
                                    ended = parsed.ended,
                                ),
                            ),
                        )
                    }
                }
            }
        })
    }

    private fun incompleteStreamFailure(route: String, message: String): BridgeResult<Nothing> =
        BridgeResult.Failure(
            route,
            BridgeError(
                httpStatus = 200,
                businessCode = "INTERVIEW_STREAM_INCOMPLETE",
                message = message,
                details = null,
                fieldErrors = emptyMap(),
                retryable = true,
                authExpired = false,
            ),
        )

    private suspend fun <T> request(
        route: String,
        method: String,
        body: JsonObject? = null,
        authenticated: Boolean,
        authorizationToken: String? = null,
        queryParameters: Map<String, String> = emptyMap(),
        decode: (JsonElement) -> T,
    ): BridgeResult<T> {
        val root = baseUrl ?: return BridgeResult.NotConfigured(configurationError ?: route)
        val requestBody = body?.toString()?.toRequestBody(JSON_MEDIA_TYPE)
        val resolvedUrl = root.resolve(route) ?: return configurationFailure(route)
        val url = resolvedUrl.newBuilder().apply {
            queryParameters.forEach { (key, value) -> addQueryParameter(key, value) }
        }.build()
        val builder = Request.Builder()
            .url(url)
            .header("Accept", "application/json")

        if (requestBody != null) {
            builder.method(method, requestBody)
        } else {
            builder.method(method, null)
        }

        if (authenticated) {
            val token = authorizationToken ?: sessionStore.token()
                ?: return authenticationRequired(route)
            builder.header("Authorization", "Token $token")
        }

        return execute(builder.build(), route, decode)
    }

    private suspend fun <T> execute(
        request: Request,
        route: String,
        decode: (JsonElement) -> T,
    ): BridgeResult<T> = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!continuation.isActive) return
                continuation.resume(
                    BridgeResult.Failure(
                        route,
                        BridgeError(
                            httpStatus = null,
                            businessCode = null,
                            message = "网络连接失败，请稍后重试。",
                            details = null,
                            fieldErrors = emptyMap(),
                            retryable = true,
                            authExpired = false,
                        ),
                    ),
                )
            }

            override fun onResponse(call: Call, response: Response) {
                if (!continuation.isActive) {
                    response.close()
                    return
                }
                response.use {
                    val text = runCatching { it.body?.string().orEmpty() }.getOrElse { "" }
                    if (!it.isSuccessful) {
                        if (it.code == 401) sessionStore.clear()
                        continuation.resume(
                            BridgeResult.Failure(route, parseHttpError(it.code, text)),
                        )
                        return
                    }

                    if (text.isBlank()) {
                        @Suppress("UNCHECKED_CAST")
                        continuation.resume(BridgeResult.Success(Unit as T))
                        return
                    }

                    try {
                        continuation.resume(
                            BridgeResult.Success(decode(json.parseToJsonElement(text))),
                        )
                    } catch (error: Exception) {
                        val failure: BridgeResult<T> = BridgeResult.Failure(
                            route,
                            BridgeError(
                                httpStatus = it.code,
                                businessCode = "INVALID_RESPONSE",
                                message = if (error is SerializationException) {
                                    "服务器返回了无法解析的数据。"
                                } else {
                                    "服务器响应缺少必要字段。"
                                },
                                details = null,
                                fieldErrors = emptyMap(),
                                retryable = false,
                                authExpired = false,
                            ),
                        )
                        continuation.resume(failure)
                    }
                }
            }
        })
    }

    private fun decodeAuthSession(element: JsonElement): AuthSession {
        val root = element.jsonObject
        val token = root.string("token")
        require(token.isNotBlank()) { "Missing token" }
        val userObject = root["user"] as? JsonObject
            ?: throw IllegalArgumentException("Missing user")
        return AuthSession(user = UserPayload.decode(userObject), token = token)
    }

    private fun parseHttpError(status: Int, text: String): BridgeError {
        val payload = runCatching { json.parseToJsonElement(text) as? JsonObject }.getOrNull()
        val message = payload?.let {
            it.optionalString("message")
                ?: it.optionalString("error")
                ?: it.optionalString("detail")
                ?: it.optionalString("result")
        }.orEmpty().ifBlank {
            if (status == 401) "登录状态已失效，请重新登录。" else "请求失败（HTTP $status）。"
        }
        val details = payload?.get("details")
        val fieldSource = (details as? JsonObject) ?: payload
        val fieldErrors = buildMap {
            fieldSource?.forEach { (key, value) ->
                if (key in NON_FIELD_KEYS) return@forEach
                val messages = when (value) {
                    is JsonArray -> value.mapNotNull {
                        (it as? JsonPrimitive)?.contentOrNull
                    }
                    is JsonPrimitive -> listOfNotNull(value.contentOrNull)
                    else -> emptyList()
                }
                if (messages.isNotEmpty()) put(key, messages)
            }
        }
        return BridgeError(
            httpStatus = status,
            businessCode = payload?.optionalString("code"),
            message = message,
            details = details,
            fieldErrors = fieldErrors,
            retryable = status in RETRYABLE_STATUSES,
            authExpired = status == 401,
        )
    }

    private fun configurationFailure(route: String): BridgeResult<Nothing> =
        BridgeResult.Failure(
            route,
            BridgeError(
                httpStatus = null,
                businessCode = "CONFIGURATION_ERROR",
                message = configurationError ?: "API 配置无效。",
                details = null,
                fieldErrors = emptyMap(),
                retryable = false,
                authExpired = false,
            ),
        )

    private fun invalidRequest(route: String, message: String): BridgeResult<Nothing> =
        BridgeResult.Failure(
            route,
            BridgeError(
                httpStatus = null,
                businessCode = "INVALID_REQUEST",
                message = message,
                details = null,
                fieldErrors = emptyMap(),
                retryable = false,
                authExpired = false,
            ),
        )

    private fun authenticationRequired(route: String): BridgeResult.Failure =
        BridgeResult.Failure(
            route,
            BridgeError(
                httpStatus = 401,
                businessCode = "AUTHENTICATION_REQUIRED",
                message = "登录状态已失效，请重新登录。",
                details = null,
                fieldErrors = emptyMap(),
                retryable = false,
                authExpired = true,
            ),
        )

    private fun catalogParameters(query: CatalogBridgeQuery): Map<String, String> = buildMap {
        if (query.query.isNotBlank()) put("q", query.query)
        when (query.kind) {
            CatalogKind.COMPANIES -> {
                if (query.city.isNotBlank()) put("city", query.city)
            }
            CatalogKind.JOBS -> {
                if (query.city.isNotBlank()) put("city", query.city)
                if (query.role.isNotBlank()) put("role", query.role)
            }
            CatalogKind.QUESTIONS -> {
                if (query.role.isNotBlank()) put("role", query.role)
                if (query.topic.isNotBlank()) put("topic", query.topic)
                if (query.difficulty.isNotBlank()) put("difficulty", query.difficulty)
            }
        }
        put("page", query.page.coerceAtLeast(1).toString())
        put("page_size", query.pageSize.coerceIn(1, 100).toString())
    }

    private fun notImplemented(route: String): BridgeResult<Nothing> =
        BridgeResult.Failure(
            route,
            BridgeError(
                httpStatus = null,
                businessCode = "CLIENT_ROUTE_PENDING",
                message = "该路由已登记，Android 适配将在后续阶段接入。",
                details = null,
                fieldErrors = emptyMap(),
                retryable = false,
                authExpired = false,
            ),
        )

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        val RETRYABLE_STATUSES = setOf(408, 429, 500, 502, 503, 504)
        val NON_FIELD_KEYS = setOf(
            "code",
            "message",
            "error",
            "detail",
            "result",
            "success",
            "details",
        )

        fun validateBaseUrl(raw: String, allowCleartext: Boolean): HttpUrl? {
            val url = raw.trim().toHttpUrlOrNull() ?: return null
            if (url.encodedPath != "/" || url.query != null || url.fragment != null) return null
            if (url.scheme == "http" && !allowCleartext) return null
            if (url.scheme !in setOf("http", "https")) return null
            return url
        }
    }
}
