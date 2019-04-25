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
import android.media.audiofx.DynamicsProcessing;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.os.Environment;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.gms.common.util.IOUtils;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import static java.lang.Thread.MAX_PRIORITY;
import static java.lang.Thread.sleep;

public class MicrophoneService extends Service {

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
    File convertedAudioFile = null;
    String convertedAudioPath = null;
    private List<String> labelList;
    private static final String LABEL_PATH = "labels.txt";
    private static final int RECORDING_LENGTH = (int) (16000 * 5000 / 1000);
    short[] recordingBuffer = new short[RECORDING_LENGTH];
    static ArrayList<short[]> bufferedShortData;
    static int numMfcc = 39;                        // No. of MFCCs (excluding delta and delta-delta) per frames
    static double frameRate = 125;                    // Frame rate in Hz
    static int samplePerFrm = 512;                // Frame size
    static int frameShift = (int) (16000 / frameRate);        // (16000/125) = 128
    static int nSubframePerBuf= 4;                                    // No. of subframes per recording buffer (device-dependent)
    static int nSubframePerMfccFrame = samplePerFrm / frameShift;    // 512/128 = 4
    static float[] x;
    static float[] subx = new float[frameShift];
    static ArrayList<float[]> subframeList = new ArrayList<>();
    FFmpeg ffmpeg;


    @Override
    public void onCreate() {
        audioPath = getExternalCacheDir().getAbsolutePath();
        audioPath += "/Recording.3gp";
        audioFile = new File(audioPath);
        audioFile = new File(audioPath);
        convertedAudioFile = new File(convertedAudioPath);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maximumLevel = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        startLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        avg = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            avg.add(1000);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = new Notification.Builder(this, getString(R.string.microphoneServiceNotificationChannelID))
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(getText(R.string.microphoneServiceNotificationTitle))
                .setContentText(getText(R.string.microphoneServiceNotificationMessage))
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        isRunning = true;
        Runnable r = new Runnable() {
            public void run() {
                startMediaRecorder();
            }
        };
        mediaThread = new Thread(r);
        mediaThread.start();

        return Service.START_STICKY;
    }


    @Override
    //not used because not binding
    public void onDestroy() {
        //mediaRecorder.stop();
        isRunning = false;
    }

    private void createNotificationChannel() {
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

    private class avgVolumeTask extends AsyncTask<Integer, Integer, Long> {

        @Override
        protected Long doInBackground(Integer... level) {
            int average = 0;
            int currentVolume = level[0];
            avg.remove();
            avg.add(currentVolume);

            for (int i = 0; i < 5; i++) {
                average += avg.get(i);
            }
            average = average / 5;
            int volume;
            if (average < NOISE_LEVEL_1) {
                volume = startLevel;
            } else if (average < NOISE_LEVEL_2) {
                volume = startLevel + 2;
            } else if (average < NOISE_LEVEL_3) {
                volume = startLevel + 4;
            } else if (average < NOISE_LEVEL_4) {
                volume = startLevel + 6;
            } else {
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

    private MappedByteBuffer loadModel() throws IOException {
        AssetFileDescriptor fd = getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fd.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fd.getStartOffset();
        long declaredLength = fd.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float[] computeMFCC() throws IOException, UnsupportedAudioFileException {

/*
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
        Log.d("length" , Integer.toString(doubleArray.length));

        Random r = new Random();
        double[] doubles = new double[2048];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = r.nextDouble();
        }
     */
        //AudioInputStream in = AudioSystem.getAudioInputStream(convertedAudioFile);
        FileInputStream in2 = new FileInputStream(audioFile);
        // InputStream to byte array
        byte[] buf = IOUtils.toByteArray(in2);
        int i = Integer.MAX_VALUE;

        // byte array to short array
        short[] shortArr = new short[buf.length / 2];
        ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArr);
        /*
        for (i=0; i<shortArr.length; i++){
            Log.d("short array at i", Short.toString(shortArr[i]));
        }
        */
        // short array to buf short array to frames

        //Log.d("before while1", " before while1");
        int count = 0;
        while (count <= shortArr.length) {                    // Still have data to process.
            for (int n = 0; n < nSubframePerBuf; n++) {            // Process audio signal in ArrayList and shift by one subframe each time
                int k = 0;
                for (i = (n * frameShift); i < (n + 1) * frameShift; i++) {
                    subx[k] = shortArr[i];
                    k++;
                }
                subframeList.add(subx);                            // Add the current subframe to the subframe list. Later, a number of
            }
            //Log.d("end for", "end for");
            count++;
        }
        //Log.d("after while1", " after while1");
        // Need at least nSubframePerMfccFrame to get one analysis frame
        x = extractOneFrameFromList(nSubframePerMfccFrame);

        MFCC mfcc = new MFCC(samplePerFrm, 16000, numMfcc);
        double[] mfccVals = mfcc.doMFCC(x);
        float[] floatArray = new float[mfccVals.length];
        for (i = 0 ; i < mfccVals.length; i++)
        {
            floatArray[i] = (float) mfccVals[i];
        }
        return floatArray;
    }

    /*
        private float [] getMFCC(){
            MFCC mfcc = new MFCC(20, 19600, 40)
            return mfcc.doMFCC()
        }
    */
    private static float[] extractOneFrameFromList(int M) {
        float x[] = new float[samplePerFrm];
        int n = 0;
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < subframeList.get(i).length; j++) {
                x[n] = subframeList.get(i)[j];
                n++;
            }
        }
        return x;
    }

    /*
        private float [] getMFCC2(){
            int sampleRate = 16000;
            int bufferSize = 512;
            int bufferOverlap = 128;
            new AndroidFFMPEGLocator(this);
            final List<float[]>mfccList = new ArrayList<>(200);
            InputStream inStream = null;
            try {
                inStream = new FileInputStream(audioPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            AudioDispatcher dispatcher = new AudioDispatcher(new UniversalAudioInputStream(inStream, new TarsosDSPAudioFormat(sampleRate, bufferSize, 1, true, true)), bufferSize, bufferOverlap);
            final MFCC mfcc = new MFCC(bufferSize, sampleRate, 40, 50, 300, 3000);
            dispatcher.addAudioProcessor(mfcc);
            dispatcher.addAudioProcessor(new AudioProcessor() {

                @Override
                public void processingFinished() {
                }

                @Override
                public boolean process(AudioEvent audioEvent) {
                    mfccList.add( mfcc.getMFCC());
                    return true;
                }
            });
            dispatcher.run();
            return
        }
    */
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

    private class soundRecognition extends AsyncTask<Integer, Integer, Long> {
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
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
            tflite.run(mfccValues, labelProbArray);

            for (int i = 0; i < labelProbArray[0].length; i++) {
                float value = labelProbArray[0][i];
                //if (i == 1f){
                    Log.d("Output at " + Integer.toString(i) + ": ", Float.toString(value));
                    //doAlert(i);
                //}
            }
            try {
                new FileOutputStream(audioFile).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    private void startMediaRecorder() {
        while (true) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFile);
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaRecorder.start();

            try {
                int amplitude = mediaRecorder.getMaxAmplitude();
                Log.d("volume", Integer.toString(amplitude));
                //finished = false;
                avgVolumeTask task = new avgVolumeTask();
                task.execute(amplitude);
                soundRecognition task2 = new soundRecognition();
                task2.execute();
                //while(finished = false);
                sleep(2000);
                mediaRecorder.stop();
                mediaRecorder.release();

            } catch (Exception e) {
                Log.d("Exception in startMediaRecorder()", e.toString());
            }
            if (!isRunning) {
                mediaRecorder.stop();
                return;
            }
        }
    }

    public File ConvertFileToAIFF(String inputPath, String outputPath) {
        AudioFileFormat inFileFormat;
        File inFile;
        File outFile;
        try {
            inFile = new File(inputPath);
            outFile = new File(outputPath);
        } catch (NullPointerException ex) {
            System.out.println("Error: one of the ConvertFileToAIFF" + " parameters is null!");
            return null;
        }
        try {
            // query file type
            inFileFormat = AudioSystem.getAudioFileFormat(inFile);
            if (inFileFormat.getType() != AudioFileFormat.Type.AIFF) {
                // inFile is not AIFF, so let's try to convert it.
                AudioInputStream inFileAIS = AudioSystem.getAudioInputStream(inFile);
                inFileAIS.reset(); // rewind
                if (AudioSystem.isFileTypeSupported(AudioFileFormat.Type.AIFF, inFileAIS)) {
                    // inFileAIS can be converted to AIFF.
                    // so write the AudioInputStream to the
                    // output file.
                    AudioSystem.write(inFileAIS, AudioFileFormat.Type.AIFF, outFile);
                    System.out.println("Successfully made AIFF file, " + outFile.getPath() + ", from " + inFileFormat.getType() + " file, " + inFile.getPath() + ".");
                    inFileAIS.close();
                    return outFile; // All done now
                } else
                    System.out.println("Warning: AIFF conversion of " + inFile.getPath() + " is not currently supported by AudioSystem.");
            } else
                System.out.println("Input file " + inFile.getPath() + " is AIFF." + " Conversion is unnecessary.");
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Error: " + inFile.getPath() + " is not a supported audio file type!");
            return null;
        } catch (IOException e) {
            System.out.println("Error: failure attempting to read " + inFile.getPath() + "!");
            return null;
        }
        return outFile;
    }

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler());
        } catch (FFmpegNotSupportedException e) {

        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler());


        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void convertFile(String inName, String outName){
        String [] cmd = {"ffmpeg -i " + inName + " -acodec pcm_u8 " + outName};
        loadFFMpegBinary();
        execFFmpegBinary(cmd);
    }

    private void doAlert(int label){
        if (label == 1){
            Log.d("car ", "car");
        }
        if (label == 6){
            Log.d("gun ", "gun");
        }
        if (label == 8){
            Log.d("sires", "siren");
        }
    }

}