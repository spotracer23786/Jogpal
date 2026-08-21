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
        endLng: Double
    ): Result<RouteResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Calculating route: Start($startLat, $startLng) -> End($endLat, $endLng)")

            // OSRM Walking API (using 'foot' profile)
            // Note: OSRM uses longitude,latitude order in the URL
            val urlString = String.format(
                Locale.US,
                "https://router.project-osrm.org/route/v1/foot/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=polyline",
                startLng, startLat, endLng, endLat
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

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) {
                return@withContext Result.failure(Exception("NO_ROUTE_FOUND"))
            }

            val route = routes.getJSONObject(0)
            val distanceMeters = route.getDouble("distance")
            val durationSeconds = route.getDouble("duration")
            val polyline = route.getString("geometry")
            
            Log.d(TAG, "OSRM SUCCESS: dist=${distanceMeters}m, dur=${durationSeconds}s")
            
            // Plausibility check: if walking 16km in 18 mins, something is wrong with OSRM profile
            val speedKmh = (distanceMeters / 1000.0) / (durationSeconds / 3600.0)
            Log.d(TAG, "Calculated Speed: %.2f km/h".format(Locale.US, speedKmh))

            val result = RouteResult(
                startLat = startLat,
                startLng = startLng,
                endLat = endLat,
                endLng = endLng,
                distanceKm = Math.round((distanceMeters / 1000.0) * 100) / 100.0, // 2 decimal places
                durationMinutes = (durationSeconds / 60).toInt(),
                encodedPolyline = polyline,
                destinationName = "Selected Point"
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Routing exception", e)
            Result.failure(e)
        }
    }
}
