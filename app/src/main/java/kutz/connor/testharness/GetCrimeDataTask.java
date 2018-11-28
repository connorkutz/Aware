package kutz.connor.testharness;

import android.os.AsyncTask;
import android.util.Log;

import com.google.maps.android.data.geojson.GeoJsonFeature;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetCrimeDataTask extends AsyncTask<Void, Void, JSONObject> {

    // link for last 30 days of crime in GeoJSON format
    // https://opendata.arcgis.com/datasets/dc3289eab3d2400ea49c154863312434_8.geojson


    static String url = "https://opendata.arcgis.com/datasets/dc3289eab3d2400ea49c154863312434_8.geojson";
    static OkHttpClient client = new OkHttpClient();


    @Override
    public JSONObject doInBackground(Void... voids){
        Request request = new Request.Builder()
                .url(url)
                .build();

        try(Response response = client.newCall(request).execute()){
            String responseString = response.body().string();
            Log.d("Crime Data", responseString);
            if(response.isSuccessful() && response.body() != null){
                try {
                    JSONObject geoJSONData = new JSONObject(responseString);

                    return geoJSONData;
                } catch(JSONException j){
                    Log.d("JSE", j.toString());
                }
            }
        }catch(IOException e){
            Log.d("IOE", e.toString());
        }
        return null;
    }

    @Override
    public void onPostExecute(JSONObject result){
        MapsActivity.addOverlay(result);
    }

}
