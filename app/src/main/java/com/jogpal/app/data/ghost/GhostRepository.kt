package com.jogpal.app.data.ghost

import com.jogpal.app.domain.ghost.GhostRun
import com.jogpal.app.domain.model.GeoPoint
import com.jogpal.app.domain.run.RunRepository
import com.jogpal.app.core.common.PolylineUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GhostRepository(
    private val runRepository: RunRepository = com.jogpal.app.data.run.RunRepositoryImpl()
) {

    fun getEligibleGhostRuns(): Flow<List<GhostRun>> {
        return runRepository.getRunHistory().map { completedPlans ->
            if (completedPlans.isEmpty()) {
                getMockGhostRuns()
            } else {
                val userGhostRuns = completedPlans.mapIndexed { index, plan ->
                    val dist = plan.actualDistanceKm ?: plan.distanceKm
                    val dur = plan.actualDurationSeconds ?: ((plan.estimatedDurationMinutes ?: 30) * 60L)
                    val paceFormatted = calculatePaceString(dist, dur)
                    val polyline = plan.actualPolyline ?: plan.encodedPolyline ?: MOCK_POLYLINE_1

                    val points = PolylineUtils.decodePolyline(polyline)

                    GhostRun(
                        id = plan.id.ifEmpty { "ghost_$index" },
                        title = plan.title.ifEmpty { "Past Run #${index + 1}" },
                        dateFormatted = plan.date.ifEmpty { "Recent Run" },
                        distanceKm = dist,
                        durationSeconds = dur,
                        pace = paceFormatted,
                        isPersonalBest = index == 0,
                        encodedPolyline = polyline,
                        waypoints = points
                    )
                }
                userGhostRuns
            }
        }
    }

    fun getGhostRunById(id: String, currentRuns: List<GhostRun>): GhostRun? {
        return currentRuns.find { it.id == id } ?: getMockGhostRuns().find { it.id == id } ?: getMockGhostRuns().firstOrNull()
    }

    companion object {
        private fun calculatePaceString(distanceKm: Double, durationSeconds: Long): String {
            if (distanceKm <= 0.0 || durationSeconds <= 0) return "0:00/km"
            val totalMinutes = durationSeconds / 60.0
            val pacePerKm = totalMinutes / distanceKm
            val minutes = pacePerKm.toInt()
            val seconds = ((pacePerKm - minutes) * 60).toInt()
            return String.format("%d:%02d/km", minutes, seconds)
        }

        // Standard mock polylines for realistic route rendering around central lat/lng
        private const val MOCK_POLYLINE_1 = "m{~vFaa_yM_A?s@CgAAcAC[?y@AgAAcA?cACm@C_AAeA?cAC]?"
        private const val MOCK_POLYLINE_2 = "k{~vF_b_yM_B?u@DgBAdBD]?"

        fun getMockGhostRuns(): List<GhostRun> {
            val baseLat = 37.7749
            val baseLng = -122.4194

            // Generate realistic loop points for mock run 1 (5.02 km)
            val mockWaypoints1 = mutableListOf<GeoPoint>()
            val steps1 = 60
            for (i in 0..steps1) {
                val angle = Math.toRadians((i.toDouble() / steps1) * 360.0)
                val lat = baseLat + (0.015 * Math.sin(angle))
                val lng = baseLng + (0.018 * (1 - Math.cos(angle)))
                mockWaypoints1.add(GeoPoint(lat, lng))
            }

            // Mock run 2 (3.20 km)
            val mockWaypoints2 = mutableListOf<GeoPoint>()
            val steps2 = 40
            for (i in 0..steps2) {
                val angle = Math.toRadians((i.toDouble() / steps2) * 360.0)
                val lat = baseLat + (0.010 * Math.sin(angle * 2))
                val lng = baseLng + (0.012 * Math.cos(angle))
                mockWaypoints2.add(GeoPoint(lat, lng))
            }

            // Mock run 3 (10.00 km)
            val mockWaypoints3 = mutableListOf<GeoPoint>()
            val steps3 = 100
            for (i in 0..steps3) {
                val angle = Math.toRadians((i.toDouble() / steps3) * 360.0)
                val lat = baseLat + (0.030 * Math.sin(angle))
                val lng = baseLng + (0.035 * Math.cos(angle))
                mockWaypoints3.add(GeoPoint(lat, lng))
            }

            return listOf(
                GhostRun(
                    id = "ghost_mock_1",
                    title = "Sunday Morning Run",
                    dateFormatted = "Aug 30, 2026",
                    distanceKm = 5.02,
                    durationSeconds = 1961, // 32:41
                    pace = "6:31/km",
                    isPersonalBest = true,
                    encodedPolyline = MOCK_POLYLINE_1,
                    waypoints = mockWaypoints1
                ),
                GhostRun(
                    id = "ghost_mock_2",
                    title = "Central Park Speed Loop",
                    dateFormatted = "Aug 26, 2026",
                    distanceKm = 3.20,
                    durationSeconds = 1115, // 18:35
                    pace = "5:48/km",
                    isPersonalBest = false,
                    encodedPolyline = MOCK_POLYLINE_2,
                    waypoints = mockWaypoints2
                ),
                GhostRun(
                    id = "ghost_mock_3",
                    title = "Endurance 10K Push",
                    dateFormatted = "Aug 20, 2026",
                    distanceKm = 10.00,
                    durationSeconds = 3420, // 57:00
                    pace = "5:42/km",
                    isPersonalBest = false,
                    encodedPolyline = MOCK_POLYLINE_1,
                    waypoints = mockWaypoints3
                )
            )
        }
    }
}
