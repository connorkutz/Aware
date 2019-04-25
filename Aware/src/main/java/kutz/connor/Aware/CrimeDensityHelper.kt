package kutz.connor.Aware

import android.content.Context
import android.location.Location
import com.google.android.gms.maps.model.LatLng

class CrimeDensityHelper(val context: Context) {

    private val dcSquareMileage = 68.34
    private val highCrimeThreshold = 500


    fun getAverageCrimeDensity(latLonList: List<LatLng>): Double{
        val numCrimes = latLonList.size
        return numCrimes/dcSquareMileage
    }

    fun getLocalCrimeDensity(latLonList: List<LatLng>, location: Location): Double{
        // +- 0.0041666 degrees is .25 miles
        // looking at number of crimes withing .25mile x .25mile square
        val localSquareMileage = .0625
        val buffer = .004166
        val lat = location.latitude
        val lon = location.longitude

        var crimeCounter = 0
        for(latLon in latLonList){
            if(latLon.latitude < lat + buffer && latLon.latitude > lat - buffer)
                if(latLon.longitude < lon + buffer && latLon.longitude > lon - buffer){
                    crimeCounter++
                }
        }

        val localDensity = crimeCounter/localSquareMileage
        if(localDensity > highCrimeThreshold){
            alertHighCrimeDensity()
        }

        return crimeCounter/localSquareMileage
    }

    private fun alertHighCrimeDensity(){
        Alert("Crime density is higher than average").announce(context)
    }

}