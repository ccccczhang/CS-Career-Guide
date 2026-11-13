package com.cscareerguide.app.data.bridge

import com.cscareerguide.app.data.PagedData
import com.cscareerguide.app.data.model.CatalogItem
import com.cscareerguide.app.data.model.CatalogKind
import com.cscareerguide.app.data.model.JobSummary
import com.cscareerguide.app.data.model.PracticeQuestion
import com.cscareerguide.app.data.model.PracticeRetrieval
import com.cscareerguide.app.data.model.QuestionEvidence
import com.cscareerguide.app.data.model.ResumeAward
import com.cscareerguide.app.data.model.ResumeCertificate
import com.cscareerguide.app.data.model.ResumeDraft
import com.cscareerguide.app.data.model.ResumeEducation
import com.cscareerguide.app.data.model.ResumeExperience
import com.cscareerguide.app.data.model.ResumeProject
import com.cscareerguide.app.data.model.SeniorAdvice
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

internal fun decodeCatalogPage(
    element: JsonElement,
    kind: CatalogKind,
): PagedData<CatalogItem> = decodePage(element) { item ->
    CatalogItem(
        id = item.string("id"),
        kind = kind,
        title = when (kind) {
            CatalogKind.JOBS -> item.string("title")
            CatalogKind.COMPANIES -> item.string("name")
            CatalogKind.QUESTIONS -> item.string("question")
        },
        subtitle = when (kind) {
            CatalogKind.JOBS -> (item["company"] as? JsonObject)?.string("name").orEmpty()
            CatalogKind.COMPANIES -> item.string("industry")
            CatalogKind.QUESTIONS -> item.string("role")
        },
        city = item.string("city"),
        role = item.string("role"),
        topic = item.string("topic"),
        difficulty = item.string("difficulty"),
        tags = item.stringList("tags"),
        description = when (kind) {
            CatalogKind.JOBS -> item.string("description")
            CatalogKind.COMPANIES -> item.string("public_profile")
                .ifBlank { item.string("description") }
            CatalogKind.QUESTIONS -> item.string("category")
        },
        keyPoints = item.stringList("key_points"),
        source = item.string("source"),
        sourceUrl = item.string("source_url"),
        collectedAt = item.string("collected_at"),
        permissionStatus = item.string("permission_status"),
        isDemo = item.boolean("is_demo"),
    )
}

internal fun decodeJobsPage(element: JsonElement): PagedData<JobSummary> =
    decodePage(element) { item ->
        JobSummary(
            id = item.string("id"),
            title = item.string("position_name"),
            company = item.string("company_name"),
            city = item.string("address"),
            salary = item.string("salary_range"),
            industry = item.string("industry"),
            companySize = item.string("company_size"),
            description = item.string("position_details"),
            companyDescription = item.string("company_details"),
        )
    }

internal fun decodeAdvicePage(element: JsonElement): PagedData<SeniorAdvice> =
    decodePage(element) { item ->
        SeniorAdvice(
            id = item.string("id"),
            company = item.string("company"),
            position = item.string("position"),
            author = item.string("senior_name"),
            graduationYear = item.string("graduation_year"),
            rating = item.double("rating"),
            content = item.string("advice"),
            currentCompany = item.string("current_company"),
            salaryInfo = item.string("salary_info"),
            pros = item.stringList("pros"),
            cons = item.stringList("cons"),
        )
    }

internal fun decodePracticeRetrieval(element: JsonElement): PracticeRetrieval {
    val payload = element.jsonObject
    val results = payload.array("results").map { result ->
        val item = result.jsonObject
        val evidence = item.array("evidence").map { rawEvidence ->
            val value = rawEvidence.jsonObject
            QuestionEvidence(
                sourceId = value.string("source_id"),
                fieldPath = value.string("field_path"),
                quote = value.string("quote"),
            )
        }
        PracticeQuestion(
            id = item.string("question_id"),
            question = item.string("question"),
            role = item.string("role"),
            topic = item.string("topic"),
            difficulty = item.string("difficulty"),
            keyPoints = item.stringList("key_points"),
            sourceEvidence = evidence.map { "${it.fieldPath}: ${it.quote}" },
            matchScore = item.double("score"),
            evidence = evidence,
            origin = item.string("origin"),
            source = item.string("source"),
            sourceUrl = item.string("source_url"),
            permissionStatus = item.string("permission_status"),
            collectedAt = item.string("collected_at"),
        )
    }
    val retrievalId = payload.string("retrieval_id")
    require(retrievalId.isNotBlank()) { "Missing retrieval_id" }
    return PracticeRetrieval(
        count = payload.int("count"),
        retrievalId = retrievalId,
        message = payload.string("message"),
        results = results,
    )
}

internal fun decodeResume(element: JsonElement): ResumeDraft {
    val payload = element.jsonObject
    return ResumeDraft(
        id = payload.string("id"),
        editRevision = payload.int("edit_revision"),
        avatarUrl = payload.string("avatar_url"),
        name = payload.string("name"),
        targetPosition = payload.string("target_position"),
        phone = payload.string("phone"),
        email = payload.string("email"),
        address = payload.string("address"),
        selfEvaluation = payload.string("self_evaluation"),
        skills = payload.string("skills"),
        education = payload.array("education").map { item ->
            val value = item.jsonObject
            ResumeEducation(
                school = value.string("school"),
                major = value.string("major"),
                period = value.string("period"),
                description = value.string("description"),
            )
        },
        experience = payload.array("experience").map { item ->
            val value = item.jsonObject
            ResumeExperience(
                company = value.string("company"),
                position = value.string("position"),
                period = value.string("period"),
                description = value.string("description"),
            )
        },
        projects = payload.array("projects").map { item ->
            val value = item.jsonObject
            ResumeProject(
                name = value.string("name"),
                role = value.string("role"),
                period = value.string("period"),
                url = value.string("url"),
                description = value.string("description"),
            )
        },
        certificates = payload.array("certificates").map { item ->
            val value = item.jsonObject
            ResumeCertificate(
                name = value.string("name"),
                issuer = value.string("issuer"),
                period = value.string("period"),
                description = value.string("description"),
            )
        },
        awards = payload.array("awards").map { item ->
            val value = item.jsonObject
            ResumeAward(
                name = value.string("name"),
                issuer = value.string("issuer"),
                period = value.string("period"),
                description = value.string("description"),
            )
        },
        createdAt = payload.string("created_at"),
        updatedAt = payload.string("updated_at"),
    )
}

internal fun encodeResume(draft: ResumeDraft): JsonObject = buildJsonObject {
    put("name", draft.name)
    put("target_position", draft.targetPosition)
    put("phone", draft.phone)
    put("email", draft.email)
    put("address", draft.address)
    put("self_evaluation", draft.selfEvaluation)
    put("skills", draft.skills)
    put("education", buildJsonArray {
        draft.education.forEach { item ->
            add(buildJsonObject {
                put("school", item.school)
                put("major", item.major)
                put("period", item.period)
                put("description", item.description)
            })
        }
    })
    put("experience", buildJsonArray {
        draft.experience.forEach { item ->
            add(buildJsonObject {
                put("company", item.company)
                put("position", item.position)
                put("period", item.period)
                put("description", item.description)
            })
        }
    })
    put("projects", buildJsonArray {
        draft.projects.forEach { item ->
            add(buildJsonObject {
                put("name", item.name)
                put("role", item.role)
                put("period", item.period)
                put("url", item.url)
                put("description", item.description)
            })
        }
    })
    put("certificates", buildJsonArray {
        draft.certificates.forEach { item ->
            add(buildJsonObject {
                put("name", item.name)
                put("issuer", item.issuer)
                put("period", item.period)
                put("description", item.description)
            })
        }
    })
    put("awards", buildJsonArray {
        draft.awards.forEach { item ->
            add(buildJsonObject {
                put("name", item.name)
                put("issuer", item.issuer)
                put("period", item.period)
                put("description", item.description)
            })
        }
    })
}

private fun <T> decodePage(
    element: JsonElement,
    transform: (JsonObject) -> T,
): PagedData<T> {
    val payload = element.jsonObject
    return PagedData(
        items = payload.array("results").map { transform(it.jsonObject) },
        total = payload.int("count"),
        page = payload.int("page").coerceAtLeast(1),
        pageSize = payload.int("page_size").coerceAtLeast(1),
        next = payload.optionalString("next")?.takeIf(String::isNotBlank),
        previous = payload.optionalString("previous")?.takeIf(String::isNotBlank),
    )
}

private fun JsonObject.array(key: String): List<JsonElement> =
    ((this[key] as? JsonArray) ?: JsonArray(emptyList())).toList()

private fun JsonObject.stringList(key: String): List<String> =
    array(key).mapNotNull { (it as? JsonPrimitive)?.contentOrNull }

private fun JsonObject.double(key: String): Double =
    (this[key] as? JsonPrimitive)?.contentOrNull?.toDoubleOrNull() ?: 0.0

private fun JsonObject.int(key: String): Int =
    (this[key] as? JsonPrimitive)?.contentOrNull?.toIntOrNull() ?: 0
