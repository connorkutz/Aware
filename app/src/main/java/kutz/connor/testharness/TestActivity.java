package kutz.connor.testharness;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.AudioManager;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.content.pm.PackageManager;


public class TestActivity extends AppCompatActivity {

    public static int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        //creates and instance of AudioManager
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        //initializes seek bar and buttons
        SeekBar musicVolumeSeekBar = (SeekBar)findViewById(R.id.musicVolumeSeekBar);
        Button startServiceButton = (Button)findViewById(R.id.startServiceButton);
        Button endServiceButton = (Button)findViewById(R.id.endServiceButton);

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


        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] permissionResults = {-1};

                ActivityCompat.requestPermissions(TestActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                onRequestPermissionsResult(MY_PERMISSIONS_REQUEST_RECORD_AUDIO, new String[]{Manifest.permission.RECORD_AUDIO}, permissionResults);
                if(MY_PERMISSIONS_REQUEST_RECORD_AUDIO == PackageManager.PERMISSION_GRANTED) {
                    Intent startServiceIntent = new Intent(v.getContext(), MicrophoneService.class);
                    Toast.makeText(getApplicationContext(), "start service", Toast.LENGTH_SHORT).show();
                    startForegroundService(startServiceIntent);
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
}
