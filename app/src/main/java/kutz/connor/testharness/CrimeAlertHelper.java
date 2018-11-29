package kutz.connor.testharness;

import android.util.Log;

import static java.lang.Thread.sleep;

public class CrimeAlertHelper {
    static Thread crimeThread;
    static boolean isRunning;


    public static void startCrimeAlerts(){
        Runnable r = new Runnable(){
            public void run(){
                pollForCrime();
            }
        };

        crimeThread = new Thread(r);
        crimeThread.start();
        isRunning = true;
    }

    public void stopCrimeAlerts(){
        isRunning = false;
    }

    private static void pollForCrime(){
        while(true) {
            if (!isRunning) {
                return;
            } else {
                try {
                    sleep(5000);
                    Log.d("connor", "polling is running");
                } catch (InterruptedException i) {
                    Log.d("IE", i.toString());
                }
            }
        }
    }
}
