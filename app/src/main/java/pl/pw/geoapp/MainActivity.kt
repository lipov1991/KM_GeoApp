package pl.pw.geoapp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var mapView: MapView
    private val viewpoint = Viewpoint(52.2206242, 21.0099656, 2000.0)
    private var activeMode = "Lig8*6ht"

    private lateinit var sensorManager: SensorManager
    private var brightness: Sensor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById<MapView>(R.id.mapView)

        setApiKeyForApp()

        setUpSensorStuff()

        setupMap()

        loadFeatureServiceURL()
    }

    private fun setUpSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        brightness = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    private fun brightness(brightness: Float): String {
        return when (brightness.toInt()) {
            in 0..30 -> "Dark"
            else -> "Light"
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val light = event.values[0]
            val light_status = brightness(light)
            Log.d("LIGHT_STATUS", light_status)

            if (light_status != activeMode && light_status == "Dark"){
                activeMode = light_status
                changeMapStyle(BasemapStyle.ARCGIS_DARK_GRAY)
            }
            else if (light_status != activeMode && light_status == "Light"){
                activeMode = light_status
                changeMapStyle(BasemapStyle.ARCGIS_LIGHT_GRAY)
            }
        }
    }
    private fun changeMapStyle(mapStyle:BasemapStyle) {
        val basemap = Basemap(mapStyle)
        mapView.map.basemap = basemap
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }
    override fun onPause() {
        mapView.pause()
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
        sensorManager.registerListener(this, brightness, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }


    // set up your map here. You will call this method from onCreate()
    private fun setupMap(mapstyle: BasemapStyle = BasemapStyle.ARCGIS_LIGHT_GRAY) {

        // create a map with the BasemapStyle streets
        val map = ArcGISMap(mapstyle)

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
}