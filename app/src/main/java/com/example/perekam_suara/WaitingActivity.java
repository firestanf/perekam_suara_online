package com.example.perekam_suara;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.livinglifetechway.quickpermissions.annotations.WithPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WaitingActivity extends AppCompatActivity {

    @Override
    @WithPermissions(
            permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}
    )

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        Bundle extras = getIntent().getExtras();
        int[] colors = {Color.parseColor("#fcd092"),
                Color.parseColor("#f59c20")};
        LinearLayout rl = findViewById(R.id.rl);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                colors

        );
        gradientDrawable.setCornerRadius(0f);
        rl.setBackground(gradientDrawable);
        if (extras != null) {
            String value = extras.getString("file_location");
            Log.e("VALUENYA", value);


            File f = new File(value);
            OkHttpClient postman = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();


            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", value,
                            RequestBody.create(MediaType.parse("audio/wav"), f))

                    .build();

            Request r = new Request.Builder()
                    .post(body)
                    .url("http://apiquerybyhumming.herokuapp.com/predict")
                    .build();

            postman.newCall(r).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Gagal, coba lagi " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String hasil = response.body().string();
                    Log.e("hASIL", hasil);

                    try {
                        JSONObject j = new JSONObject(hasil);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {


                                    JSONArray jsonArray = j.getJSONArray("result");

                                    List<Integer> data = new ArrayList<Integer>();
                                    for(int i = 0; i < jsonArray.length(); i++){
                                        data.add(Integer.parseInt(jsonArray.getString(i)));
                                    }


                                    Intent myIntent = new Intent(WaitingActivity.this, ResultActivity.class);
//                                    double[] data = {0.1986338496208191, 0.19893546402454376, 0.20120246708393097, 0.20143060386180878, 0.199797585606575};
                                    myIntent.putExtra("Result", convertIntegers(data));
                                    startActivity(myIntent);
                                    finish();

                                } catch (JSONException e) {
                                    Log.e("ERROR SEND", e.toString());
                                    e.printStackTrace();
                                }

                            }
                        });

                    } catch (JSONException e) {
                        Log.e("ERROR SEND", Log.getStackTraceString(e));
                        Intent myIntent = new Intent(WaitingActivity.this, MainActivity.class);
                        startActivity(myIntent);
                        finish();
                    }
                }
            });


            //The key argument here must match that used in the other activity
        }
    }

    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }
}