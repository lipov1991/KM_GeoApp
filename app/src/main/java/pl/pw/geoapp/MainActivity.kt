package pl.pw.geoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import pl.pw.geoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var clicked = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = findViewById<MapView>(R.id.mapView)

        setApiKeyForApp()

        setupMap()

        loadFeatureServiceURL()



        // floating buttons to changing map style
        binding.layersButton.setOnClickListener{
            Toast.makeText(this, "Styles Button clicked", Toast.LENGTH_SHORT).show()
            onLayersBtnClicked()

        }

        binding.topoStyleButton.setOnClickListener{
            Toast.makeText(this, "topographic map", Toast.LENGTH_SHORT).show()
        }

        binding.satImgStyleButton.setOnClickListener{
            Toast.makeText(this, "satellite image", Toast.LENGTH_SHORT).show()
        }

    }
//hello
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


    // set up your map here. You will call this method from onCreate()
    private fun setupMap() {

        // create a map with the BasemapStyle streets
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        // set the viewpoint, Viewpoint(latitude, longitude, scale)
        mapView.setViewpoint(Viewpoint(34.0270, -118.8050, 72000.0))
    }

    private fun setApiKeyForApp(){
        // set your API key
        // Note: it is not best practice to store API keys in source code. The API key is referenced
        // here for the convenience of this tutorial.

        ArcGISRuntimeEnvironment.setLicense(R.string.arc_gis_license.toString())
        ArcGISRuntimeEnvironment.setApiKey(R.string.maps_api_key.toString())

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


    //change var clicked status (true/false)
    private fun onLayersBtnClicked() {
        setVisibility(clicked)
        clicked = !clicked
    }

    //make style buttons visible/invisible
    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            binding.topoStyleButton.visibility = View.VISIBLE
            binding.satImgStyleButton.visibility = View.VISIBLE
        }else{
            binding.topoStyleButton.visibility = View.INVISIBLE
            binding.satImgStyleButton.visibility = View.INVISIBLE
        }
    }


}