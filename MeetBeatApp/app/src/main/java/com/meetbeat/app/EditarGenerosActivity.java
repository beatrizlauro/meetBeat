package com.meetbeat.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.adapter.GeneroCardAdapter;
import com.meetbeat.app.model.AdicionarInteresses;
import com.meetbeat.app.model.Genero;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.BuscaGeneroHelper;
import com.meetbeat.app.util.MensagensErro;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarGenerosActivity extends AppCompatActivity {

    private ApiService apiService;
    private long meuId;
    private RecyclerView recyclerView;
    private GeneroCardAdapter adapter;
    private View btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_generos);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        recyclerView = findViewById(R.id.recyclerEditarGeneros);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new GeneroCardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        BuscaGeneroHelper.configurar(this, adapter, recyclerView, findViewById(R.id.textSemResultadoEditarGeneros));

        btnSalvar = findViewById(R.id.btnSalvarGeneros);
        btnSalvar.setOnClickListener(v -> salvar());
        findViewById(R.id.btnVoltarEditarGeneros).setOnClickListener(v -> finish());

        carregarGeneros();
    }

    private void carregarGeneros() {
        apiService.getGeneros().enqueue(new Callback<List<Genero>>() {
            @Override
            public void onResponse(Call<List<Genero>> call, Response<List<Genero>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.definirGeneros(response.body());
                    carregarSelecaoAtual();
                } else {
                    Toast.makeText(EditarGenerosActivity.this, "Erro ao carregar gêneros", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Genero>> call, Throwable t) {
                Toast.makeText(EditarGenerosActivity.this, MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void carregarSelecaoAtual() {
        apiService.getInteresses(meuId).enqueue(new Callback<List<Genero>>() {
            @Override
            public void onResponse(Call<List<Genero>> call, Response<List<Genero>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Set<Long> ids = new HashSet<>();
                    for (Genero genero : response.body()) {
                        ids.add(genero.getId());
                    }
                    adapter.definirSelecionados(ids);
                } else {
                    Toast.makeText(EditarGenerosActivity.this, "Erro ao carregar seus gêneros", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Genero>> call, Throwable t) {
                Toast.makeText(EditarGenerosActivity.this, MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void salvar() {
        List<Long> selecionados = new ArrayList<>(adapter.getSelecionados());

        if (selecionados.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um gênero musical", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSalvar.setEnabled(false);

        apiService.substituirInteresses(meuId, new AdicionarInteresses(selecionados)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditarGenerosActivity.this, "Gêneros atualizados", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnSalvar.setEnabled(true);
                    Toast.makeText(EditarGenerosActivity.this, "Erro ao salvar. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSalvar.setEnabled(true);
                Toast.makeText(EditarGenerosActivity.this, "Falha de conexão. Tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
