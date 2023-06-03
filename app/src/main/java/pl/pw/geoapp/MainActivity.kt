package pl.pw.geoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(R.layout.activity_main)
        mapView = findViewById<MapView>(R.id.mapView)

        setApiKeyForApp()

        setupMap()

        // floating buttons to change map style
        binding.layers_button.setOnClickListener{
            onLayersBtnClicked()
        }

        binding.topo_style_button.setOnClickListener{
            Toast.makeText(this, "topoStyleBtn clicked", Toast.LENGTH_SHORT).show()
        }

        binding.sat_img_style_button.setOnClickListener{
            Toast.makeText(this, "satImgStyleBtn clicked", Toast.LENGTH_SHORT).show()
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


    //function which changes var clicked status (true/false)
    private fun onLayersBtnClicked() {
        setVisibility(clicked)
        clicked = !clicked
    }

    //function which makes style buttons visible/invisible
    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            topo_style_button.visibility = View.VISIBLE
            sat_img_style_button.visibility = View.VISIBLE
        }else{
            topo_style_button.visibility = View.INVISIBLE
            sat_img_style_button.visibility = View.INVISIBLE
        }
    }


}