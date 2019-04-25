package kutz.connor.Aware;
import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


class LocationHelper {

    private FusedLocationProviderClient fusedLocationClient;

    LocationHelper(Context context){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    void startLocationUpdates(){
        LocationRequest locationRequest = new LocationRequest();
        //interval to update every 2.5 minutes
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest,
                MapsActivity.locationCallback,
                null /* Looper */);
    }
}