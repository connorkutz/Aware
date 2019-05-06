package kutz.connor.Aware;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;
    public static final UserSettings currentUserSettings = new UserSettings();
    public static ArrayList<LatLng> crimeList = new ArrayList<>();
    public static LocationCallback locationCallback;
    public static DatabaseReference alertsRef;
    public static DatabaseReference settingsRef;
    private int numAlerts = 0;
    LocationHelper locationHelper;
    Location currentLocation;
    FloatingActionButton settingsButton;
    FloatingActionButton serviceButton;
    FirebaseUser currentUser;
    static Circle userMarker;
    static ArrayList<Alert> myAlerts;


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
        DatabaseReference myRef = database.getReference(currentUser.getUid());
        myRef = myRef.child("UserSettings");
        settingsRef = myRef;
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
        myRef = database.getReference("Alerts");
        alertsRef = myRef;
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Alert>> t = new GenericTypeIndicator<List<Alert>>() {};
                List<Alert> alerts = dataSnapshot.getValue(t);
                if(currentUserSettings.realTimeAlertsEnabled) {
                    if (alerts != null) {
                        //Log.d("MapsActivity:", "alerts type = " + alerts.getClass().getName());
                        //Log.d("MapsActivity", "currentAlerts: " + alerts.toString());
                        //ArrayList<Alert> myAlerts = ((ArrayList<Alert>) alerts);
                        myAlerts = (ArrayList<Alert>) dataSnapshot.getValue();
                        if (alerts.size() > numAlerts) {
                            numAlerts = alerts.size();
                            playLatestAlert(alerts);
                        }
                    } else {
                        numAlerts = 0;
                    }
                }
            }
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
                intent.putExtra("settings", currentUserSettings);
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
                    if(currentUserSettings.activeVolumeEnabled) {
                        startMicrophoneService();
                    }
                }

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
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        GetCrimeDataTask getCrimeDataTask = new GetCrimeDataTask();
        getCrimeDataTask.execute();
    }

    @Override
    public void onBackPressed(){
        //do nothing
    }

    public static void updateLocation(Location location){
        if(userMarker != null)
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

    private void playLatestAlert(List<Alert> alerts){
        Alert latest = alerts.get(alerts.size() - 1);
        latest.announce(this);
    }

    private void playAllAlerts(ArrayList<Alert> alerts){
        Iterator<Alert> i = alerts.iterator();

        while(i.hasNext()){
            Alert a = i.next();
            a.announce(this);
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

    public static void createAlert(String description){
        Alert alert = new Alert(description);
        if(myAlerts == null){
            myAlerts = new ArrayList<>();
        }
        myAlerts.add(alert);
        alertsRef.setValue(myAlerts);
    }

    public static void clearAllAlerts(){
        myAlerts = new ArrayList<>();
        alertsRef.setValue(myAlerts);
    }

    public static void createSampleCrimeAlert(){
        Alert alert = new Alert("a robbery was reported at the M Street CVS");
        if(myAlerts == null){
            myAlerts = new ArrayList<>();
        }
        myAlerts.add(alert);
        alertsRef.setValue(myAlerts);
    }
}
