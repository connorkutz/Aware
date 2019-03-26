package kutz.connor.Aware;

import android.support.annotation.NonNull;

public class UserSettings {
    Boolean nameRecognitionEnabled;
    Boolean activeVolumeEnabled;
    Boolean noiseRecognitionEnabled;
    Boolean realTimeAlertsEnabled;
    Boolean crimeDensityAlertsEnabled;


    public UserSettings(){
        this.nameRecognitionEnabled = false;
        this.activeVolumeEnabled = false;
        this.noiseRecognitionEnabled = false;
        this.realTimeAlertsEnabled = false;
        this.crimeDensityAlertsEnabled = false;
    }
    public UserSettings(boolean name, boolean volume, boolean noise, boolean realtime, boolean density){
        this.nameRecognitionEnabled = name;
        this.activeVolumeEnabled = volume;
        this.noiseRecognitionEnabled = noise;
        this.realTimeAlertsEnabled = realtime;
        this.crimeDensityAlertsEnabled = density;
    }

    @NonNull
    public String toString() {
        String result = "nameRecognitionEnabled = " + this.nameRecognitionEnabled + "\n";
        result += "activeVolumeEnabled = " + this.activeVolumeEnabled + "\n";
        result += "noiseRecognitionEnabled = " + this.noiseRecognitionEnabled + "\n";
        result += "realTimeAlertsEnabled = " + this.realTimeAlertsEnabled + "\n";
        result += "crimeDensityAlertsEnabled = " + this.crimeDensityAlertsEnabled + "\n";
        return result;
    }
}
