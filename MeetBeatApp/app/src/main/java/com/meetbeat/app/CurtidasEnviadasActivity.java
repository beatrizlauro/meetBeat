package com.meetbeat.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.adapter.CurtidaEnviadaAdapter;
import com.meetbeat.app.model.CurtidaRecebida;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.EstadoLista;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurtidasEnviadasActivity extends AppCompatActivity {

    private ApiService apiService;
    private long meuId;
    private EstadoLista estado;
    private RecyclerView recyclerView;
    private final List<CurtidaRecebida> curtidas = new ArrayList<>();
    private CurtidaEnviadaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtidas_enviadas);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        recyclerView = findViewById(R.id.recyclerCurtidasEnviadas);
        estado = new EstadoLista(this, recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CurtidaEnviadaAdapter(curtidas, this::abrirDetalhe);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnVoltarCurtidasEnviadas).setOnClickListener(v -> finish());

        carregarCurtidasEnviadas();
    }

    private void carregarCurtidasEnviadas() {
        estado.carregando();
        apiService.getCurtidasEnviadas(meuId).enqueue(new Callback<List<CurtidaRecebida>>() {
            @Override
            public void onResponse(Call<List<CurtidaRecebida>> call, Response<List<CurtidaRecebida>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    curtidas.clear();
                    curtidas.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (curtidas.isEmpty()) {
                        estado.mostrar(R.drawable.ic_genero_coracao, "Nenhuma curtida pendente",
                                "Os perfis que você curtir e que ainda não curtiram você de volta aparecem aqui.",
                                "Ver sugestões", CurtidasEnviadasActivity.this::finish);
                    } else {
                        estado.esconder();
                    }
                } else {
                    estado.erro(CurtidasEnviadasActivity.this::carregarCurtidasEnviadas);
                }
            }

            @Override
            public void onFailure(Call<List<CurtidaRecebida>> call, Throwable t) {
                estado.erro(CurtidasEnviadasActivity.this::carregarCurtidasEnviadas);
            }
        });
    }

    private void abrirDetalhe(CurtidaRecebida pessoa) {
        Intent intent = new Intent(this, MatchDetalheActivity.class);
        intent.putExtra(MatchDetalheActivity.EXTRA_NOME, pessoa.getNome());
        intent.putExtra(MatchDetalheActivity.EXTRA_FOTO, pessoa.getFotoShow());
        intent.putExtra(MatchDetalheActivity.EXTRA_BIO, pessoa.getBio());
        intent.putStringArrayListExtra(MatchDetalheActivity.EXTRA_GENEROS, new ArrayList<>(pessoa.getGenerosComuns()));
        startActivity(intent);
    }
}
