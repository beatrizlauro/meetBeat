package com.meetbeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MatchDetalheActivity extends AppCompatActivity {

    public static final String EXTRA_NOME = "extra_nome";
    public static final String EXTRA_FOTO = "extra_foto";
    public static final String EXTRA_GENEROS = "extra_generos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detalhe);

        TextView textNome = findViewById(R.id.textNomeDetalhe);
        TextView textGeneros = findViewById(R.id.textGenerosDetalhe);
        ImageView imageFoto = findViewById(R.id.imageFotoDetalhe);

        String nome = getIntent().getStringExtra(EXTRA_NOME);
        String foto = getIntent().getStringExtra(EXTRA_FOTO);
        ArrayList<String> generos = getIntent().getStringArrayListExtra(EXTRA_GENEROS);

        textNome.setText(nome);

        if (generos != null) {
            textGeneros.setText(String.join(", ", generos));
        }

        if (foto != null && !foto.isEmpty()) {
            int resId = getResources().getIdentifier(foto, "drawable", getPackageName());
            if (resId != 0) {
                imageFoto.setImageResource(resId);
            }
        }

        findViewById(R.id.btnVoltarDetalhe).setOnClickListener(v -> finish());
    }
}