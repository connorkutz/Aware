package kutz.connor.Aware;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;



public class LocationHelper {

    FusedLocationProviderClient fusedLocationClient;


    public LocationHelper(Context context){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public Location getCurrentLocation(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        try {
            Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            return location;
        }
        catch (SecurityException e){//exception not possible here
            }
        return null;
    }
    public void startLocationUpdates(){
        LocationRequest locationRequest = new LocationRequest();
        //interval to update every 2.5 minutes
        locationRequest.setInterval(150000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest,
                MapsActivity.locationCallback,
                null /* Looper */);
    }
}