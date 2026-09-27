package com.meetbeat.app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.adapter.CurtidaAdapter;
import com.meetbeat.app.model.CurtidaRecebida;
import com.meetbeat.app.model.SwipeResponse;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurtidasActivity extends AppCompatActivity {

    private ApiService apiService;
    private long meuId;
    private TextView textEstado;
    private RecyclerView recyclerView;
    private final List<CurtidaRecebida> curtidas = new ArrayList<>();
    private CurtidaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtidas);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        textEstado = findViewById(R.id.textEstadoCurtidas);
        recyclerView = findViewById(R.id.recyclerCurtidas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CurtidaAdapter(curtidas, this::curtirDeVolta);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnVoltarCurtidas).setOnClickListener(v -> finish());

        carregarCurtidas();
    }

    private void carregarCurtidas() {
        apiService.getCurtidas(meuId).enqueue(new Callback<List<CurtidaRecebida>>() {
            @Override
            public void onResponse(Call<List<CurtidaRecebida>> call, Response<List<CurtidaRecebida>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    curtidas.clear();
                    curtidas.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (curtidas.isEmpty()) {
                        mostrarEstado("Ninguém curtiu você ainda");
                    } else {
                        esconderEstado();
                    }
                } else if (response.code() == 403) {
                    mostrarEstado("Ative a opção \"Mostrar minhas curtidas\" no seu perfil para ver quem curtiu você");
                } else {
                    mostrarEstado("Erro ao carregar curtidas");
                }
            }

            @Override
            public void onFailure(Call<List<CurtidaRecebida>> call, Throwable t) {
                mostrarEstado("Falha de conexão: " + t.getMessage());
            }
        });
    }

    private void curtirDeVolta(CurtidaRecebida pessoa) {
        apiService.curtir(meuId, pessoa.getId()).enqueue(new Callback<SwipeResponse>() {
            @Override
            public void onResponse(Call<SwipeResponse> call, Response<SwipeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CurtidasActivity.this, response.body().getMensagem(), Toast.LENGTH_LONG).show();
                    int posicao = curtidas.indexOf(pessoa);
                    if (posicao != -1) {
                        curtidas.remove(posicao);
                        adapter.notifyItemRemoved(posicao);
                        if (curtidas.isEmpty()) {
                            mostrarEstado("Ninguém curtiu você ainda");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SwipeResponse> call, Throwable t) {
                Toast.makeText(CurtidasActivity.this, "Falha ao curtir: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarEstado(String mensagem) {
        textEstado.setText(mensagem);
        textEstado.setVisibility(android.view.View.VISIBLE);
        recyclerView.setVisibility(android.view.View.GONE);
    }

    private void esconderEstado() {
        textEstado.setVisibility(android.view.View.GONE);
        recyclerView.setVisibility(android.view.View.VISIBLE);
    }
}
