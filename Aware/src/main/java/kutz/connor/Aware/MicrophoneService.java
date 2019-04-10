package kutz.connor.Aware;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.os.Environment;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.lang.Thread.sleep;

public class MicrophoneService extends Service{

    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final int NOISE_LEVEL_1 = 1500;
    private static final int NOISE_LEVEL_2 = 3000;
    private static final int NOISE_LEVEL_3 = 4500;
    private static final int NOISE_LEVEL_4 = 6000;
    private MediaRecorder mediaRecorder;
    static boolean isRunning = false;
    static boolean finished = false;
    Thread mediaThread;
    static LinkedList<Integer> avg;
    AudioManager audioManager;
    int maximumLevel;
    int startLevel;
    private static final String modelPath = "ConvertedSDModel.tflite";
    File audioFile = null;
    String audioPath = null;
    private List<String> labelList;
    private static final String LABEL_PATH = "labels.txt";


    @Override
    public void onCreate()
    {
        audioPath = getExternalCacheDir().getAbsolutePath();
        audioPath += "/Recording.3gp";
        audioFile = new File(audioPath);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        maximumLevel = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        startLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        avg = new LinkedList<>();
        for(int i = 0; i < 5; i++){
            avg.add(1000);
        }
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        createNotificationChannel();
        Notification notification = new Notification.Builder(this, getString(R.string.microphoneServiceNotificationChannelID))
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(getText(R.string.microphoneServiceNotificationTitle))
                .setContentText(getText(R.string.microphoneServiceNotificationMessage))
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        isRunning = true;
        Runnable r = new Runnable(){
            public void run(){
                startMediaRecorder();
            }
        };
        mediaThread = new Thread(r);
        mediaThread.start();

        return Service.START_STICKY;
    }


    @Override
    //not used because not binding
    public void onDestroy()
    {
        //mediaRecorder.stop();
        isRunning = false;
    }

    private void createNotificationChannel(){
        CharSequence name = getString(R.string.microphoneServiceNotificationChannelName);
        String description = getString(R.string.microphoneServiceNotificationChannelDescription);
        String id = getString(R.string.microphoneServiceNotificationChannelID);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel;
        channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private class avgVolumeTask extends AsyncTask<Integer, Integer, Long>{

        @Override
        protected Long doInBackground(Integer... level) {
            int average = 0;
            int currentVolume = level[0];
            avg.remove();
            avg.add(currentVolume);

            for(int i = 0; i < 5; i ++){
                average += avg.get(i);
            }
            average = average / 5;
            int volume;
            if(average < NOISE_LEVEL_1){
                volume = startLevel;
            }
            else if(average < NOISE_LEVEL_2){
                volume = startLevel + 2;
            }
            else if(average < NOISE_LEVEL_3){
                volume = startLevel + 4;
            }
            else if(average < NOISE_LEVEL_4){
                volume = startLevel + 6;
            }
            else{
                volume = startLevel + 8;
            }
            if (volume > maximumLevel) {
                volume = maximumLevel;
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            finished = true;
            Log.d("average", Integer.toString(average));
            Log.d("volume set", Integer.toString(volume));
            return null;
        }
    }

    private MappedByteBuffer loadModel() throws IOException{
        AssetFileDescriptor fd = getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fd.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fd.getStartOffset();
        long declaredLength = fd.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float[] computeMFCC() throws IOException {


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(audioFile));
        int read;
        byte[] buff = new byte[1024];
        while ((read = in.read(buff)) > 0)

        {
            out.write(buff, 0, read);
        }
        out.flush();
        byte[] bytes = out.toByteArray();
        int times = Double.SIZE / Byte.SIZE;
        double[] doubleArray = new double[bytes.length / times];
        for(int i=0;i<doubleArray.length;i++){
            doubleArray[i] = ByteBuffer.wrap(bytes, i*times, times).getDouble();
        }

        MFCC mffc = new MFCC();
        return mffc.process(doubleArray, doubleArray.length);
    }

    private List<String> loadLabelList() throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(getAssets().open(LABEL_PATH)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private class soundRecognition extends AsyncTask<Integer, Integer, Long>{
        @Override
        protected Long doInBackground(Integer... level) {
            float[] mfccValues = null;
            Interpreter tflite = null;
            float[][] labelProbArray = null;
            try {
                mfccValues = computeMFCC();
                labelList = loadLabelList();
                labelProbArray = new float[1][labelList.size()];
                tflite = new Interpreter(loadModel());
            } catch (IOException e) {
                e.printStackTrace();
            }
            tflite.run(mfccValues, labelProbArray);

            for (int i = 0; i < labelProbArray[0].length; i++) {
                float value = labelProbArray[0][i];
                Log.d("Output for " + Integer.toString(i) + ": ", Float.toString(value));
            }
            try {
                new FileOutputStream(audioFile).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    private void startMediaRecorder(){
        while(true){
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFile);
            try{
                mediaRecorder.prepare();
            } catch(IOException e){
                e.printStackTrace();
            }
            mediaRecorder.start();

            try {
                int amplitude = mediaRecorder.getMaxAmplitude();
                Log.d("volume" , Integer.toString(amplitude));
                //finished = false;
                avgVolumeTask task = new avgVolumeTask();
                task.execute(amplitude);
                soundRecognition task2 = new soundRecognition();
                task2.execute();
                //while(finished = false);
                sleep(1500);
                mediaRecorder.stop();
                mediaRecorder.release();

            }
            catch(Exception e){
                Log.d("Exception in startMediaRecorder()", e.toString());
            }
            if(!isRunning){
                mediaRecorder.stop();
                return;
            }
        }
    }



}