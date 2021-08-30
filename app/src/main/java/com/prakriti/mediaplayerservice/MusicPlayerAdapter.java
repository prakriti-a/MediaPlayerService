package com.prakriti.mediaplayerservice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerAdapter extends RecyclerView.Adapter<MusicPlayerAdapter.MusicPlayerViewHolder> {

    private Context context;
    private List<String> titleList, artistList;

    public MusicPlayerAdapter(Context context) {
        this.context = context;
        titleList = new ArrayList<>();
        artistList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MusicPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new MusicPlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicPlayerViewHolder holder, int position) {
        holder.getSong().setText(titleList.get(position));
        holder.getArtist().setText(artistList.get(position));
    }

    @Override
    public int getItemCount() {
        return titleList.size();
    }

    public static class MusicPlayerViewHolder extends RecyclerView.ViewHolder {

        private TextView song, artist;
        public MusicPlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            song = itemView.findViewById(R.id.txtSongName);
            artist = itemView.findViewById(R.id.txtArtist);
        }

        public TextView getSong() {
            return song;
        }

        public TextView getArtist() {
            return artist;
        }
    }
}
