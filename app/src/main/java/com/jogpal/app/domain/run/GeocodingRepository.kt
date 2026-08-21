package com.jogpal.app.domain.run

import com.jogpal.app.domain.model.GeoPoint

data class SearchResult(
    val name: String,
    val address: String,
    val location: GeoPoint
)

interface GeocodingRepository {
    suspend fun searchPlaces(query: String): Result<List<SearchResult>>
}
