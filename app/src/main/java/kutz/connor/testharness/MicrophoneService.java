package kutz.connor.testharness;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.os.Process
import java.io.IOException;

public class MicrophoneService extends Service{

    private MediaRecorder mediaRecorder = null;
    private static final int ONGOING_NOTIFICATION_ID = 1;
    int startingAmplitude;
    int startingVolume;
    int maxVolume;
    int currentAmplitude;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    @Override
    public void onCreate()
    {
       HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_FOREGROUND);
       thread.start();
       mServiceLooper = thread.getLooper();
       mServiceHandler = new ServiceHandler(mServiceLooper);
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
        createNotification();
        mediaRecorder = startMediaRecorder();
        return Service.START_STICKY;
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

        createNotification();
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

    private final class ServiceHandler extends Handler{
        public ServiceHandler(Looper looper){
            super(looper);
        }

        public void getCurrentAmplitude(){
            try{
                currentAmplitude = mediaRecorder.getMaxAmplitude();
                Log.d("Amplitude Measurement:", Integer.toString(currentAmplitude));
            } catch(Exception e){

            }
        }
    }

}
