package com.meetbeat.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.adapter.MatchConfirmadoAdapter;
import com.meetbeat.app.model.MatchConfirmado;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.EstadoLista;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchesActivity extends AppCompatActivity {

    private ApiService apiService;
    private long meuId;
    private final List<MatchConfirmado> matches = new ArrayList<>();
    private MatchConfirmadoAdapter adapter;
    private EstadoLista estado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerMatches);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MatchConfirmadoAdapter(matches);
        recyclerView.setAdapter(adapter);
        estado = new EstadoLista(this, recyclerView);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        carregarMatches();
    }

    private void carregarMatches() {
        estado.carregando();
        apiService.getMatches(meuId).enqueue(new Callback<List<MatchConfirmado>>() {
            @Override
            public void onResponse(Call<List<MatchConfirmado>> call, Response<List<MatchConfirmado>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    estado.erro(MatchesActivity.this::carregarMatches);
                    return;
                }
                matches.clear();
                matches.addAll(response.body());
                adapter.notifyDataSetChanged();

                if (matches.isEmpty()) {
                    // Esta tela é aberta a partir do feed, então "Ver sugestões" só precisa fechá-la
                    estado.mostrar(R.drawable.ic_genero_coracao, "Você ainda não tem matches",
                            "Quando você e outra pessoa se curtirem, o match aparece aqui. "
                                    + "Continue curtindo perfis no feed!",
                            "Ver sugestões", MatchesActivity.this::finish);
                } else {
                    estado.esconder();
                }
            }

            @Override
            public void onFailure(Call<List<MatchConfirmado>> call, Throwable t) {
                estado.erro(MatchesActivity.this::carregarMatches);
            }
        });
    }
}