package kutz.connor.testharness;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;

import static java.lang.Thread.sleep;

public class MicrophoneService extends Service{

    private MediaRecorder mediaRecorder = null;
    private static final int ONGOING_NOTIFICATION_ID = 1;
    int startingAmplitude;
    int startingVolume;
    int maxVolume;
    int currentAmplitude;
    static boolean isRunning = false;
    private Thread micThread;

    @Override
    public void onCreate()
    {
        micThread = new Thread();
        createNotification();

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

        mediaRecorder = startMediaRecorder();
        String currentThreadName = Thread.currentThread().getName();
        micThread.start();
        if (Thread.currentThread().getName() == currentThreadName){
            return START_STICKY;
        }
        else{
            measureAmplitude();
            isRunning = true;
        }
        return START_STICKY;
    }


    @Override
    //not used because not binding
    public void onDestroy()
    {
        mediaRecorder.stop();
        isRunning = false;
    }

    private void createNotification(){
        CharSequence name = getString(R.string.microphoneServiceNotificationChannelName);
        String description = getString(R.string.microphoneServiceNotificationChannelDescription);
        String id = getString(R.string.microphoneServiceNotificationChannelID);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel;
        channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, getString(R.string.microphoneServiceNotificationChannelID))
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(getText(R.string.microphoneServiceNotificationTitle))
                .setContentText(getText(R.string.microphoneServiceNotificationMessage))
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    private MediaRecorder startMediaRecorder(){

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
        }
        return mediaRecorder;
    }


    public void measureAmplitude() {
        while (true) {
            try {
                currentAmplitude = mediaRecorder.getMaxAmplitude();
                Log.d("Amplitude Measurement:", Integer.toString(currentAmplitude));
                sleep(1000);
            } catch (Exception e) { }
        }
    }

}
