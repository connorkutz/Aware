package kutz.connor.testharness;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.AudioManager;
import android.widget.SeekBar;


public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        //creates and instance of AudioManager
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        //gets the current volume and max level of music stream
        int maximumLevel = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //initializes seek bar
        SeekBar musicVolumeSeekBar = (SeekBar)findViewById(R.id.musicVolumeSeekBar);

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
                audioManager.setStreamVolume(audioManager.STREAM_MUSIC, level, 0);
            }
        };
        
        musicVolumeSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }


}
