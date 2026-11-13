package com.cscareerguide.app.data.bridge

import com.cscareerguide.app.data.model.UserProfile
import com.cscareerguide.app.data.model.CatalogKind
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.session.AuthSessionStore
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class HttpCareerBridgeTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun loginSendsAnonymousJsonAndMapsSession() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "token":"token-a",
                      "user":{"id":7,"username":"test_a","name":"测试甲","school":"示例大学"}
                    }
                    """.trimIndent(),
                ),
        )
        val bridge = bridge()

        val result = bridge.login("test_a", "secret")

        assertTrue(result is BridgeResult.Success)
        val session = (result as BridgeResult.Success).value
        assertEquals("token-a", session.token)
        assertEquals("7", session.user.id)
        assertEquals("测试甲", session.user.displayName)
        val request = server.takeRequest()
        assertEquals("/api/users/auth/login/", request.path)
        assertEquals("POST", request.method)
        assertNull(request.getHeader("Authorization"))
        assertTrue(request.body.readUtf8().contains("\"username\":\"test_a\""))
    }

    @Test
    fun validationErrorPreservesBusinessAndFieldData() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(
                    """
                    {
                      "code":"VALIDATION_ERROR",
                      "message":"Invalid input.",
                      "details":{"username":["该字段是必填项。"]}
                    }
                    """.trimIndent(),
                ),
        )
        val bridge = bridge()

        val result = bridge.login("", "secret")

        assertTrue(result is BridgeResult.Failure)
        val error = (result as BridgeResult.Failure).error
        assertEquals(400, error.httpStatus)
        assertEquals("VALIDATION_ERROR", error.businessCode)
        assertEquals("Invalid input.", error.message)
        assertEquals(listOf("该字段是必填项。"), error.fieldErrors["username"])
        assertFalse(error.retryable)
        assertFalse(error.authExpired)
    }

    @Test
    fun profileUsesTokenAndMapsUserFields() = runTest {
        val store = MemorySessionStore("token-a")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "id":7,"username":"test_a","name":"测试甲","gender":null,
                      "school":"示例大学","major":"软件工程","grade":"大四",
                      "education":"本科","profile":"后端方向","email":"a@example.com",
                      "address":"长沙"
                    }
                    """.trimIndent(),
                ),
        )
        val bridge = bridge(store)

        val result = bridge.profile()

        assertTrue(result is BridgeResult.Success)
        val user = (result as BridgeResult.Success).value
        assertEquals("测试甲", user.displayName)
        assertEquals("软件工程", user.major)
        assertEquals("长沙", user.address)
        assertEquals("Token token-a", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun unauthorizedProfileClearsSavedToken() = runTest {
        val store = MemorySessionStore("expired")
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"detail":"Authentication credentials were not provided."}"""),
        )
        val bridge = bridge(store)

        val result = bridge.profile()

        assertTrue(result is BridgeResult.Failure)
        val error = (result as BridgeResult.Failure).error
        assertTrue(error.authExpired)
        assertEquals(401, error.httpStatus)
        assertNull(store.token())
    }

    @Test
    fun profileUpdateUsesPutAndReturnsSavedUser() = runTest {
        val store = MemorySessionStore("token-a")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":7,"username":"test_a","name":"新名字","address":"杭州"}"""),
        )
        val bridge = bridge(store)

        val result = bridge.updateProfile(
            ProfileUpdate(
                displayName = "新名字",
                school = "",
                major = "",
                grade = "",
                education = "",
                email = "",
                address = "杭州",
                profile = "",
            ),
        )

        assertTrue(result is BridgeResult.Success)
        assertEquals("新名字", (result as BridgeResult.Success).value.displayName)
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/users/profile/", request.path)
        assertTrue(request.body.readUtf8().contains("\"name\":\"新名字\""))
    }

    @Test
    fun logoutAcceptsEmptySuccessBody() = runTest {
        val store = MemorySessionStore("token-a")
        server.enqueue(MockResponse().setResponseCode(200))
        val bridge = bridge(store)

        val result = bridge.logout("token-a")

        assertTrue(result is BridgeResult.Success)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("Token token-a", request.getHeader("Authorization"))
    }

    @Test
    fun catalogUsesKindSpecificQueryAndMapsMetadata() = runTest {
        val store = MemorySessionStore("token-a")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "count":1,"page":1,"page_size":10,
                      "results":[{
                        "id":"q-1","role":"Java后端","topic":"网络","difficulty":"easy",
                        "question":"TCP 为什么需要三次握手？","key_points":["同步双方序号"],
                        "category":"计算机网络","source":"团队自编","source_url":"https://example.com",
                        "permission_status":"allowed","collected_at":null,"is_demo":false,
                        "tags":["TCP"]
                      }]
                    }
                    """.trimIndent(),
                ),
        )
        val bridge = bridge(store)

        val result = bridge.catalog(
            CatalogBridgeQuery(
                kind = CatalogKind.QUESTIONS,
                query = "TCP",
                role = "Java后端",
                topic = "网络",
                difficulty = "easy",
                page = 1,
                pageSize = 10,
            ),
        )

        assertTrue(result is BridgeResult.Success)
        val page = (result as BridgeResult.Success).value
        assertEquals(1, page.total)
        assertEquals("TCP 为什么需要三次握手？", page.items.single().title)
        assertEquals(listOf("同步双方序号"), page.items.single().keyPoints)
        val request = server.takeRequest()
        assertTrue(request.path.orEmpty().startsWith("/api/career-platform/catalog/questions/?"))
        assertTrue(request.path.orEmpty().contains("q=TCP"))
        assertTrue(request.path.orEmpty().contains("difficulty=easy"))
        assertFalse(request.path.orEmpty().contains("city="))
    }

    @Test
    fun missingResumeMapsToEmptyStateInsteadOfError() = runTest {
        val store = MemorySessionStore("token-a")
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"detail":"Not found."}"""))
        val bridge = bridge(store)

        val result = bridge.resume()

        assertTrue(result is BridgeResult.Success)
        assertNull((result as BridgeResult.Success).value)
    }

    @Test
    fun resumeSavePostsArraysAndMapsServerRevision() = runTest {
        val store = MemorySessionStore("token-a")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "id":5,"edit_revision":2,"name":"测试甲","target_position":"Java后端",
                      "skills":"Java","education":[],"experience":[],"projects":[],
                      "certificates":[],"awards":[],"created_at":"2026-01-01",
                      "updated_at":"2026-01-02"
                    }
                    """.trimIndent(),
                ),
        )
        val bridge = bridge(store)

        val result = bridge.saveResume(
            ResumeDraft(name = "测试甲", targetPosition = "Java后端", skills = "Java"),
        )

        assertTrue(result is BridgeResult.Success)
        val saved = (result as BridgeResult.Success).value
        assertEquals("5", saved.id)
        assertEquals(2, saved.editRevision)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"target_position\":\"Java后端\""))
        assertTrue(body.contains("\"education\":[]"))
        assertTrue(body.contains("\"awards\":[]"))
    }

    @Test
    fun jobsMapListContract() = runTest {
        val store = MemorySessionStore("token-a")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {"count":1,"next":null,"previous":null,"results":[{
                      "id":1,"position_name":"Java后端","company_name":"示例公司",
                      "address":"长沙","salary_range":"8k-15k","industry":"软件",
                      "company_size":"100-499人","position_details":"开发接口",
                      "company_details":"示例企业"
                    }]}
                    """.trimIndent(),
                ),
        )
        val bridge = bridge(store)

        val result = bridge.jobs("Java", 1, 100)

        assertTrue(result is BridgeResult.Success)
        val job = (result as BridgeResult.Success).value.items.single()
        assertEquals("Java后端", job.title)
        assertEquals("示例公司", job.company)
        assertTrue(server.takeRequest().path.orEmpty().contains("search=Java"))
    }

    @Test
    fun retrievePostsFormalContractAndMapsFieldEvidence() = runTest {
        val store = MemorySessionStore("token-a")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "count":1,
                      "retrieval_id":"20000000-0000-4000-8000-000000000001",
                      "message":"",
                      "results":[{
                        "question_id":"30000000-0000-4000-8000-000000000001",
                        "question":"List 与 Set 的区别是什么？",
                        "key_points":["List 允许重复元素","Set 不保存重复元素"],
                        "evidence":[
                          {"source_id":"30000000-0000-4000-8000-000000000001",
                           "field_path":"source","quote":"团队自编示例"},
                          {"source_id":"30000000-0000-4000-8000-000000000001",
                           "field_path":"question","quote":"List 与 Set"}
                        ],
                        "score":0.6,
                        "origin":"catalog",
                        "role":"Java后端",
                        "topic":"集合",
                        "difficulty":"easy",
                        "permission_status":"allowed",
                        "collected_at":null,
                        "source":"团队自编示例",
                        "source_url":""
                      }]
                    }
                    """.trimIndent(),
                ),
        )
        val bridge = bridge(store)

        val result = bridge.retrieveQuestions(
            PracticeRetrieveRequest(
                role = "Java后端",
                topic = "集合",
                difficulty = "easy",
                jdText = "Java List Set 集合",
                topK = 5,
                requestId = "10000000-0000-4000-8000-000000000001",
            ),
        )

        assertTrue(result is BridgeResult.Success)
        val retrieval = (result as BridgeResult.Success).value
        assertEquals(1, retrieval.count)
        assertEquals("20000000-0000-4000-8000-000000000001", retrieval.retrievalId)
        val question = retrieval.results.single()
        assertEquals("30000000-0000-4000-8000-000000000001", question.id)
        assertEquals("List 与 Set 的区别是什么？", question.question)
        assertEquals(listOf("List 允许重复元素", "Set 不保存重复元素"), question.keyPoints)
        assertEquals(0.6, question.matchScore, 0.0)
        assertEquals("source: 团队自编示例", question.sourceEvidence.first())
        assertEquals(
            "30000000-0000-4000-8000-000000000001",
            question.evidence.first().sourceId,
        )
        assertEquals("source", question.evidence.first().fieldPath)
        assertEquals("团队自编示例", question.evidence.first().quote)
        assertEquals("question", question.evidence.last().fieldPath)
        assertEquals("团队自编示例", question.source)
        assertEquals("allowed", question.permissionStatus)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/career-platform/questions/retrieve/", request.path)
        assertEquals("Token token-a", request.getHeader("Authorization"))
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"role\":\"Java后端\""))
        assertTrue(body.contains("\"jd_text\":\"Java List Set 集合\""))
        assertTrue(body.contains("\"request_id\":\"10000000-0000-4000-8000-000000000001\""))
        assertTrue(body.contains("\"top_k\":5"))
        assertFalse(body.contains("\"job_id\""))
        assertFalse(body.contains("\"query\""))
    }

    @Test
    fun interviewStreamReadsSseThroughDoneAndEnd() = runTest {
        val store = MemorySessionStore("token-a")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream; charset=utf-8")
                .throttleBody(1, 1, TimeUnit.MILLISECONDS)
                .setBody(
                    "data: {\"type\":\"chunk\",\"content\":\"你好，\"}\r\n\r\n" +
                        "data: {\"type\":\"chunk\",\"content\":\"请介绍项目。\"}\r\n\r\n" +
                        "data: {\"type\":\"audio\",\"audio\":\"YXVkaW8=\"}\r\n\r\n" +
                        "data: [DONE]\r\n\r\n" +
                        "data: {\"type\":\"end\",\"session_id\":\"session-a\"}\r\n\r\n",
                ),
        )
        val bridge = bridge(store)
        val events = mutableListOf<InterviewStreamEvent>()

        val result = bridge.streamInterviewChat(
            InterviewChatRequest(
                message = "开始技术面试",
                style = "technical",
                enableTts = false,
            ),
        ) { event -> synchronized(events) { events += event } }

        assertTrue(result is BridgeResult.Success)
        val stream = (result as BridgeResult.Success).value
        assertEquals("你好，请介绍项目。", stream.content)
        assertEquals("session-a", stream.sessionId)
        assertTrue(stream.done)
        assertTrue(stream.ended)
        assertEquals(
            listOf(
                InterviewStreamEvent.Chunk("你好，"),
                InterviewStreamEvent.Chunk("请介绍项目。"),
                InterviewStreamEvent.Audio("YXVkaW8="),
                InterviewStreamEvent.Done,
                InterviewStreamEvent.Session("session-a"),
            ),
            synchronized(events) { events.toList() },
        )

        val request = server.takeRequest()
        assertEquals("/api/ai/llm/chat/", request.path)
        assertEquals("Token token-a", request.getHeader("Authorization"))
        assertEquals("text/event-stream", request.getHeader("Accept"))
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"mode\":\"interview\""))
        assertTrue(body.contains("\"interview_style\":\"technical\""))
        assertTrue(body.contains("\"enable_tts\":false"))
    }

    @Test
    fun incompleteInterviewStreamPreservesPartialTextButFails() = runTest {
        val store = MemorySessionStore("token-a")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setBody("data: {\"type\":\"chunk\",\"content\":\"已收到\"}\n\n"),
        )
        val bridge = bridge(store)
        val events = mutableListOf<InterviewStreamEvent>()

        val result = bridge.streamInterviewChat(
            InterviewChatRequest(message = "继续面试"),
        ) { event -> synchronized(events) { events += event } }

        assertTrue(result is BridgeResult.Failure)
        val error = (result as BridgeResult.Failure).error
        assertEquals("INTERVIEW_STREAM_INCOMPLETE", error.businessCode)
        assertTrue(error.message.contains("未完整结束"))
        assertEquals(
            listOf(InterviewStreamEvent.Chunk("已收到")),
            synchronized(events) { events.toList() },
        )
    }

    @Test
    fun releaseStyleBridgeRejectsCleartextBaseUrl() = runTest {
        val bridge = HttpCareerBridge(
            apiBaseUrl = server.url("/").toString(),
            sessionStore = MemorySessionStore(),
            allowCleartext = false,
        )

        val result = bridge.login("test_a", "secret")

        assertTrue(result is BridgeResult.NotConfigured)
    }

    private fun bridge(store: MemorySessionStore = MemorySessionStore()) = HttpCareerBridge(
        apiBaseUrl = server.url("/").toString(),
        sessionStore = store,
        allowCleartext = true,
    )

    private class MemorySessionStore(
        private var savedToken: String? = null,
        private var savedUser: UserProfile? = null,
    ) : AuthSessionStore {
        override fun token(): String? = savedToken
        override fun user(): UserProfile? = savedUser
        override fun save(token: String, user: UserProfile) {
            savedToken = token
            savedUser = user
        }
        override fun clear() {
            savedToken = null
            savedUser = null
        }
    }
}
