package kutz.connor.testharness;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;

public class MicrophoneService extends Service{
    @Override
    public IBinder onBind(Intent intent)
    {
        IBinder iBinder = null;
        return iBinder;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
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
