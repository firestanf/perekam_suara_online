package com.example.perekam_suara;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RVA extends RecyclerView.Adapter<RVA.ViewHolder>{
    private ArrayList<SongList> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    RVA(Context context, ArrayList<SongList> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.songName.setText(getItem(position).getSong_name());
        holder.artisName.setText(getItem(position).getArtis_name());
//        holder.accuracy.setText(getItem(position).getAccuracy());
        holder.songImage.setImageDrawable(getItem(position).getImage_url());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView songName,artisName,accuracy;
        ImageView songImage;

        ViewHolder(View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.songName);
            artisName = itemView.findViewById(R.id.artistName);
            songImage = itemView.findViewById(R.id.songAlbum);
            accuracy = itemView.findViewById(R.id.accuracy);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    SongList getItem(int id) {
        SongList dat =mData.get(id);
        return dat;
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
