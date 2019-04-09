package kutz.connor.Aware;

import android.content.Context;
import android.location.Location;

public class Alert {

    public Alert(){ }

    Alert(String description){
        this.description = description;
    }

    public static final int NOISE = 0;
    public static final int CRIME = 1;
    public static final int DENSITY = 2;

    public String title;
    public String description;
    public Location location;
    public Float radius;
    public Integer type;

    public void announce(Context context){
        SpeechTask speechTask = new SpeechTask();
        String announcement = "Warning, " + description + " near your area";
        speechTask.execute(context, announcement);
    }

    public String toString(){
        return(title + ":\n" + description + "\n" + location.toString());
    }
}
