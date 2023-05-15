package pl.pw.geoapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.geometry.Point


import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol


class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val viewpoint = Viewpoint(52.2206242, 21.0099656, 2000.0)
    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById<MapView>(R.id.mapView)

        setApiKeyForApp()

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val connectivityManager =
            getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)

        if (connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) == null) {
            Toast.makeText(this@MainActivity, "Brak dostępnej sieci", Toast.LENGTH_SHORT).show()
        }

        gestureDetector =
            GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onLongPress(motionEvent: MotionEvent) {
                    val point = android.graphics.Point(motionEvent.x.toInt(), motionEvent.y.toInt())
                    val mapPoint: Point = mapView.screenToLocation(point)
                    val projectedPoint: Point =
                        GeometryEngine.project(mapPoint, SpatialReferences.getWgs84()) as Point
                    addGraphics(projectedPoint)
                }
            })
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Toast.makeText(this@MainActivity, "Połączono z internetem", Toast.LENGTH_SHORT).show()
            setupMap()
            loadFeatureServiceURL()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Toast.makeText(this@MainActivity, "Brak połączenia z internetem", Toast.LENGTH_SHORT).show()
        }
    }

    // set up your map here. You will call this method from onCreate()
    @SuppressLint("ClickableViewAccessibility")
    private fun setupMap() {

        // create a map with the BasemapStyle streets
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        // set the viewpoint, Viewpoint(latitude, longitude, scale)
        mapView.setViewpoint(viewpoint)

//        mapView.setOnTouchListener{view, motionEvent ->
//            val point = android.graphics.Point(motionEvent.x.toInt(), motionEvent.y.toInt())
//            val mapPoint: Point = mapView.screenToLocation(point)
//            val projectedPoint: Point = GeometryEngine.project(mapPoint, SpatialReferences.getWgs84()) as Point
//            addGraphics(projectedPoint)
//            false
//        }
    }

    private fun setFeatureLayer(layer: FeatureLayer) {
        // clears the existing layer on the map
        mapView.map.operationalLayers.clear()
        // adds the new layer to the map
        mapView.map.operationalLayers.add(layer)
    }

    private fun loadFeatureServiceURL() {
        // initialize the service feature table using a URL
        val serviceFeatureTable =
            ServiceFeatureTable(resources.getString(R.string.map_service_url))
        // create a feature layer with the feature table
        val featureLayer = FeatureLayer(serviceFeatureTable)
        // set the feature layer on the map
        setFeatureLayer(featureLayer)
    }

    private fun setApiKeyForApp() {
        // set your API key
        // Note: it is not best practice to store API keys in source code. The API key is referenced
        // here for the convenience of this tutorial.

        ArcGISRuntimeEnvironment.setLicense(getString(R.string.arc_gis_license))
        ArcGISRuntimeEnvironment.setApiKey(getString(R.string.maps_api_key))

    }

    private fun addGraphics(point: Point) {

        // create a graphics overlay and add it to the graphicsOverlays property of the map view
        val graphicsOverlay = GraphicsOverlay()
        mapView.graphicsOverlays.add(graphicsOverlay)

        // create a point geometry with a location and spatial reference
        // Point(latitude, longitude, spatial reference)
        //val point = Point(-118.8065, 34.0005, SpatialReferences.getWgs84())

        // create a point symbol that is an small red circle
        val simpleMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f)

        // create a blue outline symbol and assign it to the outline property of the simple marker symbol
        val blueOutlineSymbol =
            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(0, 0, 255), 2f)
        simpleMarkerSymbol.outline = blueOutlineSymbol

        // create a graphic with the point geometry and symbol
        val pointGraphic = Graphic(point, simpleMarkerSymbol)

        // add the point graphic to the graphics overlay
        graphicsOverlay.graphics.add(pointGraphic)

        // Create a polylineBuilder with a spatial reference and add three points to it.
        // Then get the polyline from the polyline builder

        // create a blue line symbol for the polyline
//        val polylineSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(0, 0, 255), 3f)
//        val polylineBuilder = PolylineBuilder(SpatialReference.wgs84()) {
//            addPoint(-118.8215, 34.0139)
//            addPoint(-118.8148, 34.0080)
//            addPoint(-118.8088, 34.0016)
//        }
//        val polyline = polylineBuilder.toGeometry()
//
//        // create a polyline graphic with the polyline geometry and symbol
//        val polylineGraphic = Graphic(polyline, polylineSymbol)
//
//        // add the polyline graphic to the graphics overlay
//        graphicsOverlay.graphics.add(polylineGraphic)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

}