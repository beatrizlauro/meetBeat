package com.meetbeat.app.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.meetbeat.app.R;
import com.meetbeat.app.network.RetrofitClient;

public class ImageLoader {

    private static final RequestOptions OPCOES_PADRAO = new RequestOptions()
            .format(DecodeFormat.PREFER_RGB_565)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(1080, 1080);

    public static void carregar(Context context, String fotoShow, ImageView imageView) {

        if (fotoShow == null || fotoShow.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        if (fotoShow.startsWith("/uploads/")) {
            // Foto nova: vem do servidor, carregamos via URL
            String urlCompleta = RetrofitClient.BASE_URL.replaceAll("/$", "") + fotoShow;

            Glide.with(context)
                    .load(urlCompleta)
                    .apply(OPCOES_PADRAO)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView);
        } else {
            // Foto antiga: recurso local do app — também carregada via Glide, com redimensionamento
            int resId = context.getResources().getIdentifier(fotoShow, "drawable", context.getPackageName());

            if (resId != 0) {
                Glide.with(context)
                        .load(resId)
                        .apply(OPCOES_PADRAO)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}