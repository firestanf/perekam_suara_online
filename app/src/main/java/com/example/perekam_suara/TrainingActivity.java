package com.example.perekam_suara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaRecorder;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.livinglifetechway.quickpermissions.annotations.WithPermissions;
import com.tsuryo.androidcountdown.Counter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import be.tarsos.dsp.io.android.AndroidAudioInputStream;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TrainingActivity extends AppCompatActivity {
    private static final int PERMISSION_RECORD_AUDIO = 0;
    //private MediaRecorder myAudioRecorder = new MediaRecorder();
    private Button start, stop;
    private Spinner sp1;
    private TextView tx,status;
    private String file_name,base_file_name;
    private String output_location;
    private RecordWaveTask recordTask = null;
    private int timer =0;
    private ImageView lirik;
    private Runnable runnable;
    private boolean recorded;
    private File wav,rar;
    private Handler handler;
    private String android_id;
    private int BUFFER = 1024;
    private String zipname = "";
    private List<File> data_split = new ArrayList<>();
    private List<String> data_split_loc = new ArrayList<>();

    public static final int SPLIT_FILE_LENGTH_MS = 30000;

    private MediaRecorder myAudioRecorder;


    public Observable<Boolean> zip(File z) {
        return Observable.fromCallable(() -> {
            Boolean x = false;
            if (wav.isFile() && wav.canRead() ) {
                try {
                    BufferedInputStream origin = null;
                    Log.v("Compress", "Adding: " + output_location);
                    FileOutputStream dest = new FileOutputStream(z);
                    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                    byte data[] = new byte[BUFFER];
                    FileInputStream fi = new FileInputStream(wav);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(output_location.substring(output_location.lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        Log.v("Compress", "adding" + count);
                        out.write(data, 0, count);
                    }
                    origin.close();
                    out.close();
                    x=true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.v("Compress", e.toString());
                    x=false;
                }
            }
            return x;
        });
    }

    private Observable<Response> upload_file (File file_split,String file_location_split) {
        return Observable.fromCallable(() -> {
            Boolean x = false;
            OkHttpClient postman = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();


            long fiel_size=file_location_split.length();;
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"" + file_location_split + "\""),new CountingFileRequestBody(file_split, "audio/wav", new CountingFileRequestBody.ProgressListener() {
                                @Override
                                public void transferred(long num) {
                                    float progress = (num / (float) fiel_size) * 100;
                                    int c=(int)progress;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            status.setText("uploading : "+c+"%");
                                            Log.v("upload", c+"");
                                        }
                                    });
                                }
                            })
                    )

                    .build();

            Request r = new Request.Builder()
                    .post(body)
                    .url("http://apiquerybyhumming.herokuapp.com/upload_file")
                    .build();

            postman.newCall(r).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String hasil = response.body().string();

                    try {
                        JSONObject j = new JSONObject(hasil);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });

                    } catch (JSONException e) {
                        Log.e("ERROR SEND", Log.getStackTraceString(e));
                    }
                }
            });
            return x;
        });
    }


    private Observable<Boolean> file_spliting ()
    {
        return Observable.fromCallable(() -> {
            Boolean x = false;
            try {
                // Get the wave file from the embedded resources
                WavFile inputWavFile = WavFile.openWavFile(wav);

                // Get the number of audio channels in the wav file
                int numChannels = inputWavFile.getNumChannels();
                // set the maximum number of frames for a target file,
                // based on the number of milliseconds assigned for each file
                int maxFramesPerFile = (int) inputWavFile.getSampleRate() * SPLIT_FILE_LENGTH_MS / 1000;

                // Create a buffer of maxFramesPerFile frames
                double[] buffer = new double[maxFramesPerFile * numChannels];

                int framesRead;
                int fileCount = 0;
                do {
                    // Read frames into buffer
                    framesRead = inputWavFile.readFrames(buffer, maxFramesPerFile);
                    String vcv=base_file_name + "_" + (fileCount + 1) + ".wav";
                    File c =new File(vcv);
                    data_split_loc.add(getFilesDir() + "/"+vcv);
                    WavFile outputWavFile= WavFile.newWavFile(
                            c,
                            inputWavFile.getNumChannels(),
                            framesRead,
                            inputWavFile.getValidBits(),
                            inputWavFile.getSampleRate());

                    // Write the buffer
                    outputWavFile.writeFrames(buffer, framesRead);
                    outputWavFile.close();
                    data_split.add(outputWavFile.getFile());
                    fileCount++;
                } while (framesRead != 0);
                // Close the input file
                inputWavFile.close();
                x=true;
            } catch (Exception e) {
//                System.err.println(e);
            }
            return x;
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        int[] colors = {Color.parseColor("#fcd092"),
                Color.parseColor("#f59c20")};
        RelativeLayout rl = findViewById(R.id.rl);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                colors
        );
        Toast.makeText(getApplicationContext(), "restart aplikasi jika pertama kali install", Toast.LENGTH_LONG).show();
        lirik=findViewById(R.id.lirik);
        gradientDrawable.setCornerRadius(0f);
        lirik.setImageResource(R.drawable.hhb);
        sp1 = findViewById(R.id.sp1);
        status = findViewById(R.id.status);
        recorded=false;
        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int pos = sp1.getSelectedItemPosition();
                if (pos == 0) {
                    lirik.setImageDrawable(getResources().getDrawable(R.drawable.hhb));
                } else if (pos == 1) {
                    lirik.setImageDrawable(getResources().getDrawable(R.drawable.pba));
                } else if (pos == 2) {
                    lirik.setImageDrawable(getResources().getDrawable(R.drawable.indonesia_raya));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        //Set Gradient
        rl.setBackground(gradientDrawable);
        if (ContextCompat.checkSelfPermission(TrainingActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(TrainingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(TrainingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(TrainingActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_RECORD_AUDIO);
            return;
        }

        tx = findViewById(R.id.tx);
        tx.setText("0:00");
        /*mCounter = findViewById(R.id.counter);
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, 30);
        System.out.println(calendar.getTime());
        mCounter.setDate(calendar.getTime());//countdown starts*/


        //or use:




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

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

//        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        myAudioRecorder.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT);
//        myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        myAudioRecorder.setAudioChannels(1);
//        myAudioRecorder.setAudioEncodingBitRate(128000);
//        myAudioRecorder.setAudioSamplingRate(48000);
//        myAudioRecorder.setOutputFile(output_location);


        recordTask = (RecordWaveTask) getLastCustomNonConfigurationInstance();
        if (recordTask == null) {
            recordTask = new RecordWaveTask(this);
        } else {
            recordTask.setContext(this);
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recorded == false){
                    start.setEnabled(false);
                    sp1.setEnabled(false);
                    handler = new Handler(Looper.getMainLooper());
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            int minutes = 0;
                            if(timer>60)
                            {
                                minutes = timer/60;
                                int xc=timer%60;
                                tx.setText(String.format("%d:%02d", minutes, xc));
                            }else{
                                tx.setText(String.format("%d:%02d", minutes, timer));
                            }
                            timer++;
                            handler.postDelayed(this, 1000); // Optional, to repeat the task.
                        }
                    };
                    handler.postDelayed(runnable, 1000);
                    listen();
                }

            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    // myAudioRecorder.stop();
                    if (!recordTask.isCancelled() && recordTask.getStatus() == AsyncTask.Status.RUNNING) {
                        recordTask.cancel(false);
                    } else {
                        Toast.makeText(TrainingActivity.this, "Task not running.", Toast.LENGTH_SHORT).show();
                    }
                } catch (RuntimeException stopException) {
                    // handle cleanup here
                    Log.i("test", "" + stopException);
                }
                if(recorded)
                {
                    sp1.setEnabled(false);
                          /*myAudioRecorder.release();
                myAudioRecorder = null;*/
                    stop.setEnabled(false);
                    timer=0;
                    kirim();
                    handler.removeCallbacks(runnable);
                    recorded=false;
                /*try {
                    myAudioRecorder.stop();
                } catch (RuntimeException stopException) {
                    // handle cleanup here
                    Log.i("test", "" + stopException);
                }*/
                    Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void kirim() {
//        File f = new File(output_location);
//        Toast.makeText(getApplicationContext(), "FILE ADA DI " + output_location, Toast.LENGTH_LONG).show();

//        rar = new File(getFilesDir(), zipname);

//        zip(rar).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(data -> {

          file_spliting().subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(data -> {
                      if(data){

                          List<Observable<Boolean>> api_list = new ArrayList<>();
                          for (int i = 0; i < data_split.size(); i++)
                          {
                              api_list.add(upload_file(data_split.get(i),data_split_loc.get(i)));
                          }


                          Observable.concat(api_list)
                                  .subscribe(new Observer<Boolean>() {

                                      @Override
                                      public void onSubscribe(Disposable d) {

                                      }

                                      @Override
                                      public void onNext(Boolean value) {
                                          Log.i("x---", "" + value);
                                      }

                                      @Override
                                      public void onError(Throwable e) {

                                          tx.setText("0:00");
                                          sp1.setEnabled(true);
                                          stop.setEnabled(true);
                                          start.setEnabled(true);
                                          status.setText("Gagal");
                                          recorded =false;
                                      }

                                      @Override
                                      public void onComplete() {
                                          tx.setText("0:00");
                                          sp1.setEnabled(true);
                                          stop.setEnabled(true);
                                          start.setEnabled(true);
                                          status.setText("berhasil");
                                          recorded =false;
                                      }
                                  });




                    }else
                    {
                        Toast.makeText(getApplicationContext(), "error wav", Toast.LENGTH_LONG).show();
                        recorded =false;
                        tx.setText("0:00");
                        sp1.setEnabled(true);
                        stop.setEnabled(true);
                        start.setEnabled(true);
                        status.setText("gagal");
                        zipname="";
                    }

                }, error -> {
                    //do something on error
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
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss", Locale.US);
            String time = df.format(new Date());
            base_file_name= sp1.getSelectedItem().toString().replace(" ", "_") + time + "_" + android_id;
            file_name = base_file_name + ".wav";

            output_location = getFilesDir() + "/"+file_name;
            wav = new File(getFilesDir(), file_name);
            zipname= base_file_name+ ".zip";
            recorded=true;
            //Toast.makeText(this, wavFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            recordTask.execute(wav);
            status.setText("recording");

        } catch (IllegalStateException ise) {
            // make something ...
        } catch (Exception ioe) {
            // make something
        }
    }
}

