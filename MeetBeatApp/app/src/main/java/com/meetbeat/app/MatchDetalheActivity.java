package com.meetbeat.app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.meetbeat.app.util.ImageLoader;
import com.meetbeat.app.util.VisualHelper;

import java.util.ArrayList;

public class MatchDetalheActivity extends AppCompatActivity {

    public static final String EXTRA_NOME = "extra_nome";
    public static final String EXTRA_FOTO = "extra_foto";
    public static final String EXTRA_GENEROS = "extra_generos";
    public static final String EXTRA_BIO = "extra_bio";

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

        String bio = getIntent().getStringExtra(EXTRA_BIO);
        VisualHelper.mostrarSeHouverTexto(findViewById(R.id.textBioDetalhe), bio);
        findViewById(R.id.textTituloBioDetalhe).setVisibility(
                findViewById(R.id.textBioDetalhe).getVisibility());

        if (generos != null) {
            textGeneros.setText(String.join(", ", generos));
        }

        // Carrega a foto (local ou via URL do servidor, dependendo do formato salvo)
        ImageLoader.carregar(this, foto, imageFoto);

        findViewById(R.id.btnVoltarDetalhe).setOnClickListener(v -> finish());
    }
}