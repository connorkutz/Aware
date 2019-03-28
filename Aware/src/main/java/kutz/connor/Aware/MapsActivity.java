package kutz.connor.Aware;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static kutz.connor.Aware.TestActivity.MY_PERMISSIONS_REQUEST_RECORD_AUDIO;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    public static final String CURRENT_LAT_EXTRA = "CURRENT_LATITUDE_EXTRA";
    public static final String CURRENT_LON_EXTRA = "CURRENT_LONGITUDE_EXTRA";
    public static final UserSettings currentUserSettings = new UserSettings();
    public static ArrayList<LatLng> crimeList = new ArrayList<>();
    public static LocationCallback locationCallback;
    LocationHelper locationHelper;
    Location currentLocation;
    FloatingActionButton settingsButton;
    FloatingActionButton serviceButton;
    FirebaseUser currentUser;
    static Circle userMarker;


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

        currentUser = getIntent().getParcelableExtra("user");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("UserSettings/");
        myRef = myRef.child(currentUser.getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap userSettings = (HashMap)dataSnapshot.getValue();
                if(userSettings != null) {
                    currentUserSettings.activeVolumeEnabled = (Boolean) userSettings.get("activeVolumeEnabled");
                    currentUserSettings.crimeDensityAlertsEnabled = (Boolean) userSettings.get("crimeDensityAlertsEnabled");
                    currentUserSettings.nameRecognitionEnabled = (Boolean) userSettings.get("nameRecognitionEnabled");
                    currentUserSettings.noiseRecognitionEnabled = (Boolean) userSettings.get("noiseRecognitionEnabled");
                    currentUserSettings.realTimeAlertsEnabled = (Boolean) userSettings.get("realTimeAlertsEnabled");
                }
                Log.d("MapsActivity", "currentUserSettings: " + currentUserSettings.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("MapsActivity","The read failed: " + databaseError.getCode());
            }
        });
        locationHelper = new LocationHelper(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                currentLocation = locationResult.getLastLocation();
                CrimeAlertHelper.location = currentLocation;
                Log.d("MapsActivity", currentLocation.toString());
                updateLocation(currentLocation);
            }
        };
        locationHelper.startLocationUpdates();

        settingsButton = findViewById(R.id.settingsButton);
        serviceButton = findViewById(R.id.serviceButton);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                intent.putExtra("crimeList", crimeList);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });
        serviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(MicrophoneService.isRunning){
                    stopMicrophoneService();
                }
                else{
                    startMicrophoneService();
                }

                mMap.clear();

                if(CrimeAlertHelper.isRunning){
                    CrimeAlertHelper.stopCrimeAlerts();
                }
                else {
                    CrimeAlertHelper.startCrimeAlerts(currentUserSettings, MapsActivity.this, crimeList, currentLocation);
                }
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
        mMap = googleMap;
        GetCrimeDataTask getCrimeDataTask = new GetCrimeDataTask();
        getCrimeDataTask.execute();
        Location location = locationHelper.getCurrentLocation(this);
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        userMarker = mMap.addCircle(new CircleOptions().center(new LatLng(lat, lon)).fillColor(-16776961).radius(50).strokeWidth(0));
        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 14)
        );
    }

    //@Override
    //public void onResume(){
    //    updateLocation(currentLocation);
    //}
    @Override
    public void onBackPressed(){
        //do nothing
    }

    public static void updateLocation(Location location){
        userMarker.remove();
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        userMarker = mMap.addCircle(new CircleOptions().center(new LatLng(lat, lon)).fillColor(-16776961).radius(50).strokeWidth(0));
        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 14)
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


    public void stopMicrophoneService() {
        if (MicrophoneService.isRunning) {
            Intent endServiceIntent = new Intent(this, MicrophoneService.class);
            stopService(endServiceIntent);
            serviceButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24px));

        } else {
            Toast.makeText(getApplicationContext(), "service already stopped", Toast.LENGTH_SHORT).show();
        }
    }
    public void startMicrophoneService() {
        if (!MicrophoneService.isRunning) {
            Intent startServiceIntent = new Intent(this, MicrophoneService.class);
            startServiceIntent.putExtra("volume", currentUserSettings.activeVolumeEnabled);
            startServiceIntent.putExtra("density", currentUserSettings.crimeDensityAlertsEnabled);
            Toast.makeText(getApplicationContext(), "start service", Toast.LENGTH_SHORT).show();
            startForegroundService(startServiceIntent);
            Log.d("MapsActivity", Boolean.toString(MicrophoneService.isRunning));
            serviceButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_pause_24px));


        } else {
            Toast.makeText(getApplicationContext(), "service already running", Toast.LENGTH_SHORT).show();
        }
    }
}
