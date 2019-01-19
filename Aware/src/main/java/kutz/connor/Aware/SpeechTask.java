package kutz.connor.Aware;

import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;


public class SpeechTask extends AsyncTask<Object, Void, Void> {

    private static TextToSpeech tts;

    @Override
    public Void doInBackground(Object... objects) {
        Context context = (Context)objects[0];
        final String message = (String)objects[1];
        if(context == null || message == null) {
            Log.d("SpeechTask", "invalid funtion call");
            return null;
        }

        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                broadcastSpeech(message);
            }
        });

        return null;
    }

    private void broadcastSpeech(String message){
        tts.speak(message, TextToSpeech.QUEUE_ADD, null, message);
    }
}
