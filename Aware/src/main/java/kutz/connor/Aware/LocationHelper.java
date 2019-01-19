package kutz.connor.Aware;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;


public class LocationHelper {
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
}