package com.meetbeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meetbeat.app.model.AtualizarBio;
import com.meetbeat.app.model.AtualizarMusicaArtista;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.UserSession;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingBioActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText editBio, editMusica, editArtista;
    private long meuId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_bio);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        editBio = findViewById(R.id.editBioOnboarding);
        editMusica = findViewById(R.id.editMusicaFavorita);
        editArtista = findViewById(R.id.editArtistaFavorito);

        findViewById(R.id.btnContinuarBio).setOnClickListener(v -> salvarTudoEContinuar());
        findViewById(R.id.btnPularBio).setOnClickListener(v -> irParaProximaTela());
        findViewById(R.id.btnVoltarOnboarding).setOnClickListener(v -> finish());
    }

    private void salvarTudoEContinuar() {
        String bio = editBio.getText().toString().trim();
        String musica = editMusica.getText().toString().trim();
        String artista = editArtista.getText().toString().trim();

        if (bio.isEmpty() && musica.isEmpty() && artista.isEmpty()) {
            irParaProximaTela();
            return;
        }

        if (!bio.isEmpty()) {
            apiService.atualizarBio(meuId, new AtualizarBio(bio)).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) { }

                @Override
                public void onFailure(Call<Void> call, Throwable t) { }
            });
        }

        if (!musica.isEmpty() || !artista.isEmpty()) {
            apiService.atualizarMusicaArtista(meuId, new AtualizarMusicaArtista(musica, artista)).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    irParaProximaTela();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(OnboardingBioActivity.this, "Erro ao salvar, mas você pode continuar", Toast.LENGTH_SHORT).show();
                    irParaProximaTela();
                }
            });
        } else {
            irParaProximaTela();
        }
    }

    private void irParaProximaTela() {
        startActivity(new Intent(this, OnboardingFotoPerfilActivity.class));
    }
}