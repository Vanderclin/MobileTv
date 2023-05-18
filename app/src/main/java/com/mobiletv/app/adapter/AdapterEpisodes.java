package com.mobiletv.app.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mobiletv.app.R;
import com.mobiletv.app.pojo.EpisodeDetails;

import java.util.List;

public class AdapterEpisodes extends RecyclerView.Adapter<AdapterEpisodes.EpisodeViewHolder> {
    private List<EpisodeDetails> episodes;
    private OnEpisodeClickListener episodeClickListener;

    public AdapterEpisodes(List<EpisodeDetails> episodes, OnEpisodeClickListener episodeClickListener) {
        this.episodes = episodes;
        this.episodeClickListener = episodeClickListener;
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder mHolder, int position) {
        EpisodeDetails episode = episodes.get(position);
        Glide.with(mHolder.itemView.getContext()).load(episode.getCover()).placeholder(R.drawable.icon_placeholder_cards).into(mHolder.episodeCover);
        mHolder.episodeTitle.setText(episode.getTitle());
        mHolder.itemView.setOnClickListener(view -> {
            Uri address = Uri.parse(episode.getAddress());
            String title = String.valueOf(episode.getTitle());
            episodeClickListener.onEpisodeClick(address, title, position);
        });
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    public static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView episodeCover;
        public AppCompatTextView episodeTitle;

        public EpisodeViewHolder(View itemView) {
            super(itemView);
            episodeCover = itemView.findViewById(R.id.episode_cover);
            episodeTitle = itemView.findViewById(R.id.episode_title);
            episodeTitle.setSelected(true);
        }
    }

    public interface OnEpisodeClickListener {
        void onEpisodeClick(Uri address, String title, int position);
    }
}
