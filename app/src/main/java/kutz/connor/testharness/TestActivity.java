package kutz.connor.testharness;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;



public class TestActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;
    public static MapsActivity mapsActivity;

    final LocationHelper locationHelper = new LocationHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        //creates and instance of AudioManager
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        final LocationHelper locationHelper = new LocationHelper();

        //initializes seek bar and buttons
        SeekBar musicVolumeSeekBar = (SeekBar)findViewById(R.id.musicVolumeSeekBar);
        final Button startServiceButton = (Button)findViewById(R.id.startServiceButton);
        Button endServiceButton = (Button)findViewById(R.id.endServiceButton);
        Button mapButton = (Button)findViewById(R.id.MapButton);

        //gets the current volume and max level of music stream
        int maximumLevel = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //sets seek bar to current volume level
        musicVolumeSeekBar.setMax(maximumLevel);
        musicVolumeSeekBar.setProgress(currentLevel);

        //creates active listener and applies to seekbar view
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onStopTrackingTouch(SeekBar musicVolumeSeekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar musicVolumeSeekBar) {}

            @Override
            public void onProgressChanged(SeekBar musicVolumeSeekBar, int level, boolean fromUser)
            {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
            }
        };

        musicVolumeSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(TestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(TestActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    // Permission is not granted
                }
                else{
                    Location location =  locationHelper.getCurrentLocation(TestActivity.this);
                    String latLon = Double.toString(location.getLatitude()) + Double.toString((location.getLongitude()));
                    Log.d("location", latLon );
                    Intent intent = new Intent(TestActivity.this, MapsActivity.class);
                    intent.putExtra(MapsActivity.CURRENT_LAT_EXTRA, location.getLatitude());
                    intent.putExtra(MapsActivity.CURRENT_LON_EXTRA, location.getLongitude());
                    startActivity(intent);
                }
            }
        }
        );

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(TestActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ActivityCompat.requestPermissions(TestActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                }
                else{
                    //permission already granted
                        startMicrophoneService();
                }
            }
        });

        endServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent endServiceIntent = new Intent(v.getContext(), MicrophoneService.class);
                Toast.makeText(getApplicationContext(), "end service", Toast.LENGTH_SHORT).show();
                stopService(endServiceIntent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // audio-related task you need to do.
                    startMicrophoneService();
                }
                else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "audio permission not granted", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Location location =  locationHelper.getCurrentLocation(TestActivity.this);
                    Intent intent = new Intent(TestActivity.this, MapsActivity.class);
                    intent.putExtra(MapsActivity.CURRENT_LAT_EXTRA, location.getLatitude());
                    intent.putExtra(MapsActivity.CURRENT_LON_EXTRA, location.getLongitude());
                    startActivity(intent);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "location permission not granted", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void startMicrophoneService(){
        if (!MicrophoneService.isRunning) {
            Intent startServiceIntent = new Intent(this, MicrophoneService.class);
            Toast.makeText(getApplicationContext(), "start service", Toast.LENGTH_SHORT).show();
            startForegroundService(startServiceIntent);

        }
        else {
            Toast.makeText(getApplicationContext(), "service already running", Toast.LENGTH_SHORT).show();
        }
    }
}
