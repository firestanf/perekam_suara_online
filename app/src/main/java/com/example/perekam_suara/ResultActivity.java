package com.example.perekam_suara;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity implements RVA.ItemClickListener{

    public ArrayList<SongList> song_data = new ArrayList();
    public ArrayList<SongList> picked_song =new ArrayList();
    RVA adapter;
    private Button play;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Bundle extras = getIntent().getExtras();
        String output_location =getFilesDir()+"/audio.wav";

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        String[] song = {"Jika","Maafkan Aku","Beharap tak berpisah","Cinta Membawamu Kembali","Pupus"};
//        String[] artis  = {"Melly goeslau","Tiara","Reza","Dewa 19","Dewa 19"};
//        int[] img  = {R.drawable.jika,R.drawable.maafkan,R.drawable.btb,R.drawable.ckm,R.drawable.pupus};
//
//        if (extras != null) {
//            double[] value = (double[]) getIntent().getSerializableExtra("Result");
//
//            for (int n = 0; n < value.length; n++) {
//                song_data.add(new SongList(song[n],artis[n],ContextCompat.getDrawable(this,img[n]),value[n]));
//            }
//
//
//            Collections.sort(song_data, new Comparator<SongList>() {
//                @Override
//                public int compare(SongList a, SongList b) {
//                    return Double.compare(a.getAccuracydouble() , b.getAccuracydouble());
//                }
//            });
//
//            //The key argument here must match that used in the other activity
//        }


        song_data.add(new SongList("Beharap tak berpisah","Reza",ContextCompat.getDrawable(this, R.drawable.btb)));
        song_data.add(new SongList("Cinta Membawamu Kembali","Dewa 19",ContextCompat.getDrawable(this, R.drawable.ckm)));
        song_data.add(new SongList("Jika","Melly goeslau", ContextCompat.getDrawable(this, R.drawable.jika)));

        song_data.add(new SongList("Maafkan Aku","Tiara",ContextCompat.getDrawable(this, R.drawable.maafkan)));

        song_data.add(new SongList("Pupus","Dewa 19",ContextCompat.getDrawable(this, R.drawable.pupus)));

//        picked_song.add(song_data.get(0));
//        picked_song.add(song_data.get(1));
//        picked_song.add(song_data.get(2));

        if (extras != null) {
            int[] value = (int[])getIntent().getSerializableExtra("Result");
            for (int n :value)
            {
                picked_song.add(song_data.get(n));
            }
            //The key argument here must match that used in the other activity
        }

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvAnimals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RVA(this, picked_song);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        play = (Button) findViewById(R.id.play);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(output_location);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    // make something
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
        finish();
        return true;
    }

    @Override
    public void onItemClick(View view, int position) {
//        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}