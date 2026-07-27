package com.meetbeat.app.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.meetbeat.app.R;
import com.meetbeat.app.network.RetrofitClient;

public class ImageLoader {

    public static void carregar(Context context, String fotoShow, ImageView imageView) {

        if (fotoShow == null || fotoShow.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        if (fotoShow.startsWith("/uploads/")) {
            // Foto nova: vem do servidor, carregamos via URL com Glide
            String urlCompleta = RetrofitClient.BASE_URL.replaceAll("/$", "") + fotoShow;
            Glide.with(context)
                    .load(urlCompleta)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView);
        } else {
            // Foto antiga: é um recurso local do app (drawable)
            int resId = context.getResources().getIdentifier(fotoShow, "drawable", context.getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}