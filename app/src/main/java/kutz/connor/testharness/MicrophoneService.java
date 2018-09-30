package kutz.connor.testharness;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.Manifest.permission;

public class MicrophoneService extends Service{

    private MediaRecorder mediaRecorder;
    private static final int ONGOING_NOTIFICATION_ID = 1;

    @Override
    public void onCreate()
    {
        mediaRecorder = new MediaRecorder();
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
        Notification notification = new Notification.Builder(this, Notification.CATEGORY_SERVICE)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(getText(R.string.microphoneServiceNotificationTitle))
                .setContentText(getText(R.string.microphoneServiceNotificationMessage))
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
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
}
