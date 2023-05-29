package pl.pw.geoapp


import android.annotation.SuppressLint
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.Symbol


class MainActivity : AppCompatActivity(), View.OnTouchListener {

    companion object {
        private const val TAG = "pw.MainActivity"
    }

    private lateinit var mapView: MapView
    private val viewpoint = Viewpoint(52.2206242, 21.0099656, 2000.0)
    private lateinit var gestureDetector: GestureDetectorCompat

    //private val graphicsOverlay = GraphicsOverlay()
    private val addedPointsCollection = PointCollection(SpatialReferences.getWgs84())

    @SuppressLint("ClickableViewAccessibility")
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
                    Log.d(TAG, "onLongPress: $motionEvent")
                    val point = android.graphics.Point(motionEvent.x.toInt(), motionEvent.y.toInt())
                    val mapPoint: Point = mapView.screenToLocation(point)
                    val projectedPoint: Point =
                        GeometryEngine.project(mapPoint, SpatialReferences.getWgs84()) as Point
                    addPoint(projectedPoint)
                    addedPointsCollection.add(projectedPoint.x, projectedPoint.y)
                    Log.d(TAG, "pointsCollection: ${addedPointsCollection.size}")
                    if (addedPointsCollection.size == 2) {
                        addPolyline(addedPointsCollection)
                    }
                }

                override fun onDown(motionEvent: MotionEvent): Boolean {
                    Log.d(TAG, "onDown: $motionEvent")
                    return false
                }
            })

        findViewById<FrameLayout>(R.id.touchable_view).setOnTouchListener(this)
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
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Połączono z internetem", Toast.LENGTH_SHORT).show()
                setupMap()
                loadFeatureServiceURL()
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Brak połączenia z internetem", Toast.LENGTH_SHORT).show()
            }
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

    private fun addPoint(point: Point) {
        // create a point symbol that is an small red circle
        val simpleMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f).apply {
            // create a blue outline symbol and assign it to the outline property of the simple marker symbol
            outline = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(0, 0, 255), 2f)
        }
        addOverlay(point, simpleMarkerSymbol)
    }

    private fun addOverlay(geometry: Geometry, symbol: Symbol) {
        // create a graphics overlay and add it to the graphicsOverlays property of the map view
        val pointGraphic = Graphic(geometry, symbol)
        val graphicsOverlay = GraphicsOverlay().apply {
            graphics.add(pointGraphic)
        }
        mapView.graphicsOverlays.add(graphicsOverlay)
    }

    private fun addPolyline(pointCollection: PointCollection) {
        // create a blue line symbol for the polyline
        val polylineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 3f)
        // Create a polylineBuilder with a spatial reference and add three points to it.
        val polylineBuilder = PolylineBuilder(pointCollection, SpatialReferences.getWgs84())
        // Then get the polyline from the polyline builder
        val polyline = polylineBuilder.toGeometry()
        addOverlay(polyline, polylineSymbol)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean = gestureDetector.onTouchEvent(motionEvent)
}