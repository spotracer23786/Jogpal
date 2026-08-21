package com.jogpal.app.data.run

import com.jogpal.app.domain.model.GeoPoint
import com.jogpal.app.domain.run.GeocodingRepository
import com.jogpal.app.domain.run.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class GeocodingRepositoryImpl : GeocodingRepository {
    override suspend fun searchPlaces(query: String): Result<List<SearchResult>> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&addressdetails=1&limit=5"
            
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "JogpalApp/1.0")
            
            if (connection.responseCode != 200) {
                return@withContext Result.failure(Exception("HTTP_ERROR_${connection.responseCode}"))
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(response)
            
            val results = mutableListOf<SearchResult>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val lat = obj.getDouble("lat")
                val lon = obj.getDouble("lon")
                val displayName = obj.getString("display_name")
                
                // Extract a shorter name if possible
                val name = displayName.split(",").firstOrNull() ?: displayName
                
                results.add(
                    SearchResult(
                        name = name,
                        address = displayName,
                        location = GeoPoint(lat, lon)
                    )
                )
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
