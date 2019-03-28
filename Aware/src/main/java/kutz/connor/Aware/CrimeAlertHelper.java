package kutz.connor.Aware;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

import static java.lang.Thread.sleep;

public class CrimeAlertHelper {
    private static Thread crimeThread;
    static boolean isRunning;
    private static UserSettings userSettings;
    private static CrimeDensityHelper cdHelper;
    public static Location location;


    public static void startCrimeAlerts(UserSettings currentSettings, final Context context, final ArrayList<LatLng> crimeList, Location givenLocation){
        location = givenLocation;
        userSettings = currentSettings;
        if(userSettings.crimeDensityAlertsEnabled) {
            cdHelper = new CrimeDensityHelper(context);
            Runnable r = new Runnable() {
                public void run() {
                    pollForCrime(context, crimeList);
                }
            };
            crimeThread = new Thread(r);
            crimeThread.start();
            isRunning = true;
        }
    }

    public static void stopCrimeAlerts(){
        isRunning = false;
    }

    private static void pollForCrime(Context context, ArrayList<LatLng> crimeList){
        while(isRunning) {
            if(userSettings.crimeDensityAlertsEnabled){

                double avg = cdHelper.getAverageCrimeDensity(crimeList);
                double local = cdHelper.getLocalCrimeDensity(crimeList, location);
                Log.d("TestActivity", "Avg:" + avg + "\n" + "local:" + local + "\n");

                //wait 2.5 minutes
                try {
                    sleep(150000);
                }
                catch(InterruptedException e){
                    Log.d("CrimeAlertHelper", e.toString());
                }
            }
        }
    }
}
