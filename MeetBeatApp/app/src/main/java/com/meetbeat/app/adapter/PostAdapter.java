package com.meetbeat.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.R;
import com.meetbeat.app.util.ImageLoader;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public interface OnPostLongClickListener {
        void onPostLongClick(String urlFoto);
    }

    private final List<String> posts;
    private final OnPostLongClickListener listener;

    public PostAdapter(List<String> posts, OnPostLongClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String urlFoto = posts.get(position);
        Context context = holder.itemView.getContext();
        ImageLoader.carregar(context, urlFoto, holder.imagePost);
        holder.imagePost.setOnLongClickListener(v -> {
            listener.onPostLongClick(urlFoto);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePost;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePost = itemView.findViewById(R.id.imagePost);
        }
    }
}