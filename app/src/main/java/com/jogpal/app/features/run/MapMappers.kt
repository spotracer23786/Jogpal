package com.jogpal.app.features.run

import org.maplibre.android.geometry.LatLng
import com.jogpal.app.domain.model.GeoPoint

fun GeoPoint.toLatLng(): LatLng = LatLng(lat, lng)

fun List<GeoPoint>.toLatLngList(): List<LatLng> = map { it.toLatLng() }

fun LatLng.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)
