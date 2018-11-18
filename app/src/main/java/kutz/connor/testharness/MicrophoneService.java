package kutz.connor.testharness;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
import java.util.LinkedList;

import static java.lang.Thread.sleep;

public class MicrophoneService extends Service{

    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final int NOISE_LEVEL_1 = 1500;
    private static final int NOISE_LEVEL_2 = 3000;
    private static final int NOISE_LEVEL_3 = 4500;
    private static final int NOISE_LEVEL_4 = 6000;
    private MediaRecorder mediaRecorder;
    static boolean isRunning = false;
    static boolean finished = false;
    Thread mediaThread;
    static LinkedList<Integer> avg;
    AudioManager audioManager;
    int maximumLevel;
    int startLevel;


    @Override
    public void onCreate()
    {
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        maximumLevel = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        startLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        avg = new LinkedList<>();
        for(int i = 0; i < 5; i++){
            avg.add(1000);
        }
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        createNotificationChannel();
        Notification notification = new Notification.Builder(this, getString(R.string.microphoneServiceNotificationChannelID))
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(getText(R.string.microphoneServiceNotificationTitle))
                .setContentText(getText(R.string.microphoneServiceNotificationMessage))
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        isRunning = true;
        Runnable r = new Runnable(){
            public void run(){
                startMediaRecorder();
            }
        };
        mediaThread = new Thread(r);
        mediaThread.start();

        return Service.START_STICKY;
    }


    @Override
    //not used because not binding
    public void onDestroy()
    {
        //mediaRecorder.stop();
        isRunning = false;
    }

    private void createNotificationChannel(){
        CharSequence name = getString(R.string.microphoneServiceNotificationChannelName);
        String description = getString(R.string.microphoneServiceNotificationChannelDescription);
        String id = getString(R.string.microphoneServiceNotificationChannelID);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel;
        channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private class avgVolumeTask extends AsyncTask<Integer, Integer, Long>{

        @Override
        protected Long doInBackground(Integer... level) {
            int average = 0;
            int currentVolume = level[0];
            avg.remove();
            avg.add(currentVolume);

            for(int i = 0; i < 5; i ++){
                average += avg.get(i);
            }
            average = average / 5;
            int volume;
            if(average < NOISE_LEVEL_1){
                volume = startLevel;
            }
            else if(average < NOISE_LEVEL_2){
                volume = startLevel + 2;
            }
            else if(average < NOISE_LEVEL_3){
                volume = startLevel + 4;
            }
            else if(average < NOISE_LEVEL_4){
                volume = startLevel + 6;
            }
            else{
                volume = startLevel + 8;
            }
            if (volume > maximumLevel) {
                volume = maximumLevel;
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            finished = true;
            Log.d("average", Integer.toString(average));
            Log.d("volume set", Integer.toString(volume));
            return null;
        }
    }


    private void startMediaRecorder(){
        if(mediaRecorder == null){
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile("/dev/null");
            try{
                mediaRecorder.prepare();
            } catch(IOException e){
                e.printStackTrace();
            }
            mediaRecorder.start();


            while(true){
                try {
                    int amplitude = mediaRecorder.getMaxAmplitude();
                    Log.d("volume" , Integer.toString(amplitude));
                    //finished = false;
                    avgVolumeTask task = new avgVolumeTask();
                    task.execute(amplitude);
                    //while(finished = false);
                    sleep(1500);

                }
                catch(Exception e){
                    Log.d("Exception in startMediaRecorder()", e.toString());
                }
                if(!isRunning){
                    mediaRecorder.stop();
                    return;
                }
            }
        }
    }



}