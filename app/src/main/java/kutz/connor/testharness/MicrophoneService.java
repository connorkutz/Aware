package kutz.connor.testharness;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;

public class MicrophoneService extends Service{

    private MediaRecorder mediaRecorder;
    private static final int ONGOING_NOTIFICATION_ID = 1;
    static boolean isRunning = false;
    Thread mediaThread;

    @Override
    public void onCreate()
    {
        MediaRecorder mediaRecorder = null;
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        IBinder iBinder = null;
        return iBinder;
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
                    //check volume level
                    //adjust if changed
                    //repeat
                    Thread.sleep(1000);
                }
                catch(Exception e){
                }
                if(!isRunning){
                    mediaRecorder.stop();
                    return;
                }
            }
        }
    }

}