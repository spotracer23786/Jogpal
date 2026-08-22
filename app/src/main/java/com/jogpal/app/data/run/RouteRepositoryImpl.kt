package com.jogpal.app.data.run

import android.util.Log
import com.jogpal.app.domain.run.RouteRepository
import com.jogpal.app.domain.run.RouteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class RouteRepositoryImpl : RouteRepository {
    
    private val TAG = "RouteRepo"
    
    override suspend fun calculateRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        alternatives: Boolean
    ): Result<List<RouteResult>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Calculating route (alt=$alternatives): Start($startLat, $startLng) -> End($endLat, $endLng)")

            // OSRM Walking API (using 'foot' profile)
            // Note: OSRM uses longitude,latitude order in the URL
            val urlString = String.format(
                Locale.US,
                "https://router.project-osrm.org/route/v1/foot/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=polyline&alternatives=%b",
                startLng, startLat, endLng, endLat, alternatives
            )
            
            Log.d(TAG, "Requesting OSRM: $urlString")

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "JogpalApp/1.0")
            connection.setRequestProperty("Accept", "application/json")
            
            val responseCode = try {
                connection.responseCode
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed before response code", e)
                throw Exception("CONNECTION_FAILED: ${e.message}")
            }
            
            Log.d(TAG, "HTTP Response Code: $responseCode")

            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "HTTP Error ($responseCode): $errorBody")
                return@withContext Result.failure(Exception("OSRM_HTTP_$responseCode"))
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "Response length: ${response.length}")
            
            val json = JSONObject(response)
            val code = json.optString("code")
            Log.d(TAG, "OSRM status: $code")

            if (code != "Ok") {
                val message = json.optString("message", code)
                return@withContext Result.failure(Exception("OSRM_API_ERROR: $message"))
            }

            val routesJson = json.getJSONArray("routes")
            if (routesJson.length() == 0) {
                return@withContext Result.failure(Exception("NO_ROUTE_FOUND"))
            }

            val results = mutableListOf<RouteResult>()
            for (i in 0 until routesJson.length()) {
                val route = routesJson.getJSONObject(i)
                val distanceMeters = route.getDouble("distance")
                val durationSeconds = route.getDouble("duration")
                val polyline = route.getString("geometry")
                
                results.add(
                    RouteResult(
                        startLat = startLat,
                        startLng = startLng,
                        endLat = endLat,
                        endLng = endLng,
                        distanceKm = Math.round((distanceMeters / 1000.0) * 100) / 100.0,
                        durationMinutes = (durationSeconds / 60).toInt(),
                        encodedPolyline = polyline,
                        destinationName = if (i == 0) "Primary Route" else "Alternative ${i + 1}"
                    )
                )
            }
            
            Log.d(TAG, "OSRM SUCCESS: found ${results.size} routes")
            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "Routing exception", e)
            Result.failure(e)
        }
    }
}
