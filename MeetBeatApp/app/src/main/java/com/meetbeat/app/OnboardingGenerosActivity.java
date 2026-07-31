package com.meetbeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.adapter.GeneroCardAdapter;
import com.meetbeat.app.model.Genero;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingGenerosActivity extends AppCompatActivity {

    private ApiService apiService;
    private GeneroCardAdapter adapter;
    private long meuId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_generos);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerGeneros);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        List<Genero> listaVazia = new ArrayList<>();
        adapter = new GeneroCardAdapter(listaVazia);
        recyclerView.setAdapter(adapter);

        carregarGeneros(recyclerView);

        findViewById(R.id.btnContinuarGeneros).setOnClickListener(v -> salvarGenerosEContinuar());
        findViewById(R.id.btnVoltarOnboarding).setOnClickListener(v -> finish());
    }

    private void carregarGeneros(RecyclerView recyclerView) {
        apiService.getGeneros().enqueue(new Callback<List<Genero>>() {
            @Override
            public void onResponse(Call<List<Genero>> call, Response<List<Genero>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new GeneroCardAdapter(response.body());
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Genero>> call, Throwable t) {
                Toast.makeText(OnboardingGenerosActivity.this, "Erro ao carregar gêneros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void salvarGenerosEContinuar() {
        for (Long generoId : adapter.getSelecionados()) {
            apiService.adicionarInteresse(meuId, generoId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) { }

                @Override
                public void onFailure(Call<Void> call, Throwable t) { }
            });
        }

        startActivity(new Intent(this, OnboardingBioActivity.class));
    }
}