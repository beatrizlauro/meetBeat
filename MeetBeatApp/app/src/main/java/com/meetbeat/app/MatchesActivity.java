package com.meetbeat.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.adapter.MatchConfirmadoAdapter;
import com.meetbeat.app.model.MatchConfirmado;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        long meuId = UserSession.getUsuarioLogado(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerMatches);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<MatchConfirmado> matches = new ArrayList<>();
        MatchConfirmadoAdapter adapter = new MatchConfirmadoAdapter(matches);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getMatches(meuId).enqueue(new Callback<List<MatchConfirmado>>() {
            @Override
            public void onResponse(Call<List<MatchConfirmado>> call, Response<List<MatchConfirmado>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    matches.clear();
                    matches.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MatchesActivity.this, "Erro ao carregar matches", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MatchConfirmado>> call, Throwable t) {
                Toast.makeText(MatchesActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}