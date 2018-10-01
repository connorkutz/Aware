package kutz.connor.testharness;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

public class MicrophoneService extends Service{

    private MediaRecorder mediaRecorder;
    private static final int ONGOING_NOTIFICATION_ID = 1;

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

        startMediaRecorder();

        return Service.START_STICKY;
    }

    @Override
    //not used because not binding
    public void onDestroy()
    { }

    private final class ServiceHandler extends Handler{
        public ServiceHandler(Looper looper){
            super(looper);
        }
    }

    private void createNotificationChannel(){
        CharSequence name = getString(R.string.microphoneServiceNotificationChannelName);
        String description = getString(R.string.microphoneServiceNotificationChannelDescription);
        String id = getString(R.string.microphoneServiceNotificationChannelID);
        int importance = NotificationManager.IMPORTANCE_MAX;
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void startMediaRecorder(){
        if(mediaRecorder == null){
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.);
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
    }
}
