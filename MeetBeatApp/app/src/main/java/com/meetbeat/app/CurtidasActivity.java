package com.meetbeat.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.adapter.CurtidaAdapter;
import com.meetbeat.app.model.AtualizarMostrarCurtidas;
import com.meetbeat.app.model.CurtidaRecebida;
import com.meetbeat.app.model.SwipeResponse;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.EstadoLista;
import com.meetbeat.app.util.MensagensErro;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurtidasActivity extends AppCompatActivity {

    private ApiService apiService;
    private long meuId;
    private EstadoLista estado;
    private RecyclerView recyclerView;
    private final List<CurtidaRecebida> curtidas = new ArrayList<>();
    private CurtidaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtidas);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        recyclerView = findViewById(R.id.recyclerCurtidas);
        estado = new EstadoLista(this, recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CurtidaAdapter(curtidas, this::curtirDeVolta);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnVoltarCurtidas).setOnClickListener(v -> finish());

        carregarCurtidas();
    }

    private void carregarCurtidas() {
        estado.carregando();
        apiService.getCurtidas(meuId).enqueue(new Callback<List<CurtidaRecebida>>() {
            @Override
            public void onResponse(Call<List<CurtidaRecebida>> call, Response<List<CurtidaRecebida>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    curtidas.clear();
                    curtidas.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (curtidas.isEmpty()) {
                        mostrarVazio();
                    } else {
                        estado.esconder();
                    }
                } else if (response.code() == 403) {
                    // A preferência "Mostrar minhas curtidas" está desativada (visibilidade recíproca)
                    estado.mostrar(R.drawable.ic_genero_coracao, "Suas curtidas estão ocultas",
                            "Para ver quem curtiu você, suas curtidas também precisam ficar visíveis "
                                    + "para as outras pessoas.",
                            "Ativar \"Mostrar minhas curtidas\"", CurtidasActivity.this::ativarMostrarCurtidas);
                } else {
                    estado.erro(CurtidasActivity.this::carregarCurtidas);
                }
            }

            @Override
            public void onFailure(Call<List<CurtidaRecebida>> call, Throwable t) {
                estado.erro(CurtidasActivity.this::carregarCurtidas);
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
                            mostrarVazio();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SwipeResponse> call, Throwable t) {
                Toast.makeText(CurtidasActivity.this, MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarVazio() {
        estado.mostrar(R.drawable.ic_genero_coracao, "Ninguém curtiu seu perfil ainda",
                "Quando alguém curtir você, a pessoa aparece aqui e você pode curtir de volta. "
                        + "Uma bio e uma foto do feed caprichadas ajudam!",
                "Ver sugestões", this::finish);
    }

    private void ativarMostrarCurtidas() {
        estado.carregando();
        apiService.atualizarMostrarCurtidas(meuId, new AtualizarMostrarCurtidas(true)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    carregarCurtidas();
                } else {
                    estado.erro(CurtidasActivity.this::carregarCurtidas);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                estado.erro(CurtidasActivity.this::carregarCurtidas);
            }
        });
    }
}
