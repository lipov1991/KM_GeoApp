package pl.pw.geoapp


import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "pw.MainActivity"
    }

    private lateinit var mapView: MapView
    private val viewpoint = Viewpoint(52.2206242, 21.0099656, 2000.0)
    val locationMarker = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFF0000FF.toInt(), 10f)
    private val wgs84 = SpatialReferences.getWgs84()

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

        setUpMapOnTouchListener()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpMapOnTouchListener() {
        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {

            override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
                val screenStartPoint = android.graphics.Point(motionEvent.x.roundToInt(), motionEvent.y.roundToInt())
                val mapStartPoint = mapView.screenToLocation(screenStartPoint)
                val startPointGraphic = Graphic(mapStartPoint, locationMarker).apply {
                    geometry = GeometryEngine.project(mapStartPoint, wgs84)
                }
                val graphicOverlay = GraphicsOverlay().apply { graphics. add(startPointGraphic) }
                mapView.graphicsOverlays.add(graphicOverlay)
                return super.onSingleTapConfirmed(motionEvent)
            }
        }
    }
}