package kutz.connor.Aware;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.Collection;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    public static final String CURRENT_LAT_EXTRA = "CURRENT_LATITUDE_EXTRA";
    public static final String CURRENT_LON_EXTRA = "CURRENT_LONGITUDE_EXTRA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        try {
            mapFragment.getMapAsync(this);
        }
        catch(NullPointerException n){
            Log.d("NPE", n.toString());
        }

        FloatingActionButton settingsButton = findViewById(R.id.settingsButton);
        FloatingActionButton serviceButton = findViewById(R.id.serviceButton);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        serviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        GetCrimeDataTask getCrimeDataTask = new GetCrimeDataTask();
        getCrimeDataTask.execute();
        mMap = googleMap;
        double lat = getIntent().getDoubleExtra(CURRENT_LAT_EXTRA, 0);
        double lon = getIntent().getDoubleExtra(CURRENT_LON_EXTRA, 0);

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lon))
                .visible(true)
        );

        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 13)
        );
    }

    public static void addHeatMap(Collection<LatLng> data){
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(data)
                .radius(45)
                .build();

        TileOverlay overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        overlay.setVisible(true);
        overlay.setFadeIn(true);
        overlay.setTransparency((float)0.11111);

        Log.d("MapsActivity", "added heatmap Transparent: " + overlay.getTransparency());

    }
}
