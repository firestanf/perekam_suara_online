package com.example.perekam_suara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.livinglifetechway.quickpermissions.annotations.WithPermissions;
import com.tsuryo.androidcountdown.Counter;

import java.io.File;

public class ListeningActivity extends AppCompatActivity {
    private static final int PERMISSION_RECORD_AUDIO = 0;
    //private MediaRecorder myAudioRecorder = new MediaRecorder();
    private Button stop;
    private TextView tx;
    private String output_location;
    private RecordWaveTask recordTask = null;
    private Counter mCounter;
    private int timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening);
        int[] colors = {Color.parseColor("#fcd092"),
                Color.parseColor("#f59c20")};
        RelativeLayout rl = findViewById(R.id.rl);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                colors

        );
        gradientDrawable.setCornerRadius(0f);

        //Set Gradient
        rl.setBackground(gradientDrawable);
        if (ContextCompat.checkSelfPermission(ListeningActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ListeningActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ListeningActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(ListeningActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_RECORD_AUDIO);
            return;
        }

        tx = findViewById(R.id.tx);
        /*mCounter = findViewById(R.id.counter);
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, 30);
        System.out.println(calendar.getTime());
        mCounter.setDate(calendar.getTime());//countdown starts*/


        //or use:


        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                tx.setText(timer + "");
                timer++;
                handler.postDelayed(this, 1000); // Optional, to repeat the task.
            }
        };
        handler.postDelayed(runnable, 1000);

        /*
         * Additional attributes:
         * */
       /* mCounter.setIsShowingTextDesc(true);
        mCounter.setTextColor(R.color.colorPrimary);
        mCounter.setMaxTimeUnit(TimeUnits.HOUR);
        mCounter.setTextSize(30);

        mCounter.setListener(new Counter.Listener() {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("TEST", millisUntilFinished + "");
            }

            @Override
            public void onTick(long days, long hours, long minutes, long seconds) {
                if (seconds == 0) {
                    stop.performClick();
                } else if (seconds < 30) {
                    tx.setText(seconds + "");
                }

            }
        });*/

        output_location = getFilesDir() + "/audio.wav";
        stop = (Button) findViewById(R.id.stop);
        /*myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT);
        myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        myAudioRecorder.setAudioChannels(1);
        myAudioRecorder.setAudioEncodingBitRate(128000);
        myAudioRecorder.setAudioSamplingRate(48000);
        myAudioRecorder.setOutputFile(output_location);
*/

        recordTask = (RecordWaveTask) getLastCustomNonConfigurationInstance();
        if (recordTask == null) {
            recordTask = new RecordWaveTask(this);
        } else {
            recordTask.setContext(this);
        }


        listen();
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    // myAudioRecorder.stop();
                    if (!recordTask.isCancelled() && recordTask.getStatus() == AsyncTask.Status.RUNNING) {
                        recordTask.cancel(false);
                    } else {
                        Toast.makeText(ListeningActivity.this, "Task not running.", Toast.LENGTH_SHORT).show();
                    }
                } catch (RuntimeException stopException) {
                    // handle cleanup here
                    Log.i("test", "" + stopException);
                }
                /*myAudioRecorder.release();
                myAudioRecorder = null;*/
                stop.setEnabled(false);

                Intent myIntent = new Intent(ListeningActivity.this, WaitingActivity.class);
                myIntent.putExtra("file_location", output_location);
                startActivity(myIntent);
                finish();
                /*try {
                    myAudioRecorder.stop();
                } catch (RuntimeException stopException) {
                    // handle cleanup here
                    Log.i("test", "" + stopException);
                }*/
                Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        recordTask.setContext(null);
        return recordTask;
    }

    @WithPermissions(
            permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}
    )
    private void listen() {
        try {
            switch (recordTask.getStatus()) {
                case RUNNING:
                    Toast.makeText(this, "Task already running...", Toast.LENGTH_SHORT).show();
                    return;
                case FINISHED:
                    recordTask = new RecordWaveTask(this);
                    break;
                case PENDING:
                    if (recordTask.isCancelled()) {
                        recordTask = new RecordWaveTask(this);
                    }
            }
            File wavFile = new File(getFilesDir(), "/audio.wav");
            //Toast.makeText(this, wavFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            recordTask.execute(wavFile);
        } catch (IllegalStateException ise) {
            // make something ...
        } catch (Exception ioe) {
            // make something
        }
    }
}