package com.jetwatch.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jetwatch.shared.R
import com.jetwatch.domain.model.Aircraft
import com.jetwatch.domain.model.BoundingBox
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private const val STYLE_URI = "https://tiles.openfreemap.org/styles/liberty"
private const val SOURCE_ID = "aircraft"
private const val LAYER_ID = "aircraft-layer"
private const val LABEL_LAYER_ID = "aircraft-labels"
private const val IMAGE_ID = "plane"

@Composable
actual fun AircraftMap(
    aircraft: List<Aircraft>,
    onBoundsChanged: (BoundingBox) -> Unit,
    onAircraftClick: (Aircraft) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context) }
    val aircraftState = rememberUpdatedState(aircraft)
    val boundsState = rememberUpdatedState(onBoundsChanged)
    val clickState = rememberUpdatedState(onAircraftClick)
    val mapRef = remember { MapHolder() }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                onCreate(Bundle())
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) onStart()
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) onResume()
                getMapAsync { map ->
                    mapRef.map = map
                    map.uiSettings.isRotateGesturesEnabled = true
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-1.2921, 36.8219), 6.0))
                    map.setStyle(Style.Builder().fromUri(STYLE_URI)) { style ->
                        style.addImage(IMAGE_ID, context.planeBitmap())
                        style.addSource(GeoJsonSource(SOURCE_ID, collection(aircraftState.value)))
                        style.addLayer(planeLayer())
                        style.addLayer(labelLayer())
                        publishBounds(map, boundsState.value)
                    }
                    map.addOnCameraIdleListener {
                        publishBounds(map, boundsState.value)
                    }
                    map.addOnMapClickListener { latLng ->
                        val density = resources.displayMetrics.density
                        val match = nearestAircraft(map, latLng, aircraftState.value, density)
                            ?: return@addOnMapClickListener false
                        clickState.value(match)
                        true
                    }
                }
            }
        },
        update = {
            val style = mapRef.map?.style ?: return@AndroidView
            val source = style.getSourceAs<GeoJsonSource>(SOURCE_ID) ?: return@AndroidView
            source.setGeoJson(collection(aircraft))
        },
    )
}

private class MapHolder {
    var map: MapLibreMap? = null
}

private fun publishBounds(map: MapLibreMap, onBoundsChanged: (BoundingBox) -> Unit) {
    val bounds = map.projection.visibleRegion.latLngBounds
    onBoundsChanged(
        BoundingBox(
            south = bounds.southWest.latitude,
            west = bounds.southWest.longitude,
            north = bounds.northEast.latitude,
            east = bounds.northEast.longitude,
        ),
    )
}

private fun collection(aircraft: List<Aircraft>): FeatureCollection {
    val features = aircraft.map { plane ->
        Feature.fromGeometry(Point.fromLngLat(plane.longitude, plane.latitude)).apply {
            addStringProperty("icao24", plane.icao24)
            addStringProperty("callsign", plane.callSign.orEmpty())
            addNumberProperty("heading", plane.headingDegrees ?: 0.0)
        }
    }
    return FeatureCollection.fromFeatures(features)
}

private fun nearestAircraft(
    map: MapLibreMap,
    latLng: LatLng,
    aircraft: List<Aircraft>,
    density: Float,
): Aircraft? {
    if (aircraft.isEmpty()) return null
    val tap = map.projection.toScreenLocation(latLng)
    val threshold = 64f * density
    val nearest = aircraft.minBy { plane ->
        val point = map.projection.toScreenLocation(LatLng(plane.latitude, plane.longitude))
        val dx = point.x - tap.x
        val dy = point.y - tap.y
        dx * dx + dy * dy
    }
    val point = map.projection.toScreenLocation(LatLng(nearest.latitude, nearest.longitude))
    val dx = point.x - tap.x
    val dy = point.y - tap.y
    return nearest.takeIf { dx * dx + dy * dy <= threshold * threshold }
}

private fun labelLayer(): SymbolLayer = SymbolLayer(LABEL_LAYER_ID, SOURCE_ID).withProperties(
    PropertyFactory.textField(Expression.get("callsign")),
    PropertyFactory.textSize(12f),
    PropertyFactory.textOffset(arrayOf(0f, 1.5f)),
    PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP),
    PropertyFactory.textAllowOverlap(true),
    PropertyFactory.textIgnorePlacement(true),
    PropertyFactory.textColor("#10233F"),
    PropertyFactory.textHaloColor("#FFFFFF"),
    PropertyFactory.textHaloWidth(1.4f),
    PropertyFactory.textFont(arrayOf("Noto Sans Regular")),
)

private fun planeLayer(): SymbolLayer = SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
    PropertyFactory.iconImage(IMAGE_ID),
    PropertyFactory.iconRotate(Expression.get("heading")),
    PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),
    PropertyFactory.iconAllowOverlap(true),
    PropertyFactory.iconIgnorePlacement(true),
    PropertyFactory.iconSize(1.05f),
)

private fun android.content.Context.planeBitmap(): Bitmap {
    val drawable = ContextCompat.getDrawable(this, R.drawable.ic_plane)!!
    val size = (36 * resources.displayMetrics.density).toInt().coerceAtLeast(48)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, size, size)
    drawable.draw(canvas)
    return bitmap
}
