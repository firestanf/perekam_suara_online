package com.example.perekam_suara;

import android.graphics.drawable.Drawable;
import android.net.Uri;

public class SongList {

    public String song_name;
    public String artis_name;
    public Drawable image_url;
//    public double accuracy;
    public SongList(String sn,String an,Drawable img)
    {
        this.song_name = sn;
        this.artis_name=an;
        this.image_url=img;
//        this.accuracy=acc;
    }
//    public double getAccuracydouble() {
//        return accuracy;
//    }
    public String getSong_name() {
        return song_name;
    }

    public String getArtis_name() {
        return artis_name;
    }

    public Drawable getImage_url() {
        return image_url;
    }

//    public String getAccuracy() {
//        double percentage = (double) Math.ceil((1 - accuracy) * 100);
//        return Double.toString(percentage);
//    }
}
