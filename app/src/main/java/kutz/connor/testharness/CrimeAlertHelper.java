package kutz.connor.testharness;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.lang.Thread.sleep;

public class CrimeAlertHelper {
    private static Thread crimeThread;
    static OkHttpClient client = new OkHttpClient();
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

                    // http request for crime data within 2000 meters of current location
                    /*
                    int radius = 2000; // crime radius in meters
                    double lon = 0.0;
                    double lat = 0.0;
                    String key = "NO_KEY_YET";
                    String url = ("http://api.spotcrime.com/crimes.json?Lat=" + lat + "&Lon=" + lon + "&Raduis=" + radius);
                    Request request = new Request.Builder()
                            .url(url)
                            .header("key", key)
                            .build();
                    try(Response response = client.newCall(request).execute()){
                        String responseString = response.body().string();
                        Log.d("Crime Data", responseString);
                        if(response.isSuccessful() && response.body() != null){
                            try {
                                JSONObject crimeAlerts = new JSONObject(responseString);

                                //extract data from crimeAlerts
                                //look for new instances
                                //if there is a new alert, publish to user

                            } catch(JSONException j){
                                Log.d("JSE", j.toString());
                            }
                        }
                    }catch(IOException e){
                        Log.d("IOE", e.toString());
                    }
                    */

                } catch (InterruptedException i) {
                    Log.d("IE", i.toString());
                }
            }
        }
    }
}
