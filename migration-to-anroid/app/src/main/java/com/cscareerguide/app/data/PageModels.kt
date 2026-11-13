package com.cscareerguide.app.data

import com.cscareerguide.app.data.bridge.BridgeError
import com.cscareerguide.app.data.model.CatalogKind

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: BridgeError) : AppResult<Nothing>
}

data class PagedData<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val next: String? = null,
    val previous: String? = null,
)

data class CatalogQuery(
    val kind: CatalogKind,
    val query: String = "",
    val city: String = "",
    val role: String = "",
    val topic: String = "",
    val difficulty: String = "",
    val page: Int = 1,
    val pageSize: Int = 20,
)
