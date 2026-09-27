package com.meetbeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.adapter.CurtidaEnviadaAdapter;
import com.meetbeat.app.model.CurtidaRecebida;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurtidasEnviadasActivity extends AppCompatActivity {

    private ApiService apiService;
    private long meuId;
    private TextView textEstado;
    private RecyclerView recyclerView;
    private final List<CurtidaRecebida> curtidas = new ArrayList<>();
    private CurtidaEnviadaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtidas_enviadas);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        textEstado = findViewById(R.id.textEstadoCurtidasEnviadas);
        recyclerView = findViewById(R.id.recyclerCurtidasEnviadas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CurtidaEnviadaAdapter(curtidas, this::abrirDetalhe);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnVoltarCurtidasEnviadas).setOnClickListener(v -> finish());

        carregarCurtidasEnviadas();
    }

    private void carregarCurtidasEnviadas() {
        apiService.getCurtidasEnviadas(meuId).enqueue(new Callback<List<CurtidaRecebida>>() {
            @Override
            public void onResponse(Call<List<CurtidaRecebida>> call, Response<List<CurtidaRecebida>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    curtidas.clear();
                    curtidas.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (curtidas.isEmpty()) {
                        mostrarEstado("Você ainda não curtiu ninguém");
                    } else {
                        esconderEstado();
                    }
                } else {
                    mostrarEstado("Erro ao carregar curtidas enviadas");
                }
            }

            @Override
            public void onFailure(Call<List<CurtidaRecebida>> call, Throwable t) {
                mostrarEstado("Falha de conexão: " + t.getMessage());
            }
        });
    }

    private void abrirDetalhe(CurtidaRecebida pessoa) {
        Intent intent = new Intent(this, MatchDetalheActivity.class);
        intent.putExtra(MatchDetalheActivity.EXTRA_NOME, pessoa.getNome());
        intent.putExtra(MatchDetalheActivity.EXTRA_FOTO, pessoa.getFotoShow());
        intent.putStringArrayListExtra(MatchDetalheActivity.EXTRA_GENEROS, new ArrayList<>(pessoa.getGenerosComuns()));
        startActivity(intent);
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
