package com.meetbeat.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.adapter.MatchAdapter;
import com.meetbeat.app.model.MatchSuggestion;
import com.meetbeat.app.model.SwipeResponse;
import com.meetbeat.app.model.UsuarioSimples;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private long meuId;
    private RecyclerView recyclerView;
    private MatchAdapter adapter;
    private final List<MatchSuggestion> sugestoes = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        recyclerView = findViewById(R.id.recyclerSugestoes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MatchAdapter(sugestoes, new MatchAdapter.OnMatchActionListener() {
            @Override
            public void onCurtir(MatchSuggestion pessoa) {
                curtirPessoa(pessoa);
            }

            @Override
            public void onRejeitar(MatchSuggestion pessoa) {
                sugestoes.remove(pessoa);
                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Sugestão removida", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnVerMatches).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, MatchesActivity.class)));

        findViewById(R.id.btnCriarPerfil).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, CadastroActivity.class)));

        findViewById(R.id.btnTrocarUsuario).setOnClickListener(v -> abrirSeletorDeUsuario());

        carregarSugestoes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        long idAtual = UserSession.getUsuarioLogado(this);
        if (idAtual != meuId) {
            meuId = idAtual;
            carregarSugestoes();
        }
    }

    private void abrirSeletorDeUsuario() {
        apiService.listarUsuarios().enqueue(new Callback<List<UsuarioSimples>>() {
            @Override
            public void onResponse(Call<List<UsuarioSimples>> call, Response<List<UsuarioSimples>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UsuarioSimples> usuarios = response.body();

                    String[] nomes = new String[usuarios.size()];
                    for (int i = 0; i < usuarios.size(); i++) {
                        nomes[i] = usuarios.get(i).getNome();
                    }

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Trocar usuário logado")
                            .setItems(nomes, (dialog, which) -> {
                                UsuarioSimples selecionado = usuarios.get(which);
                                meuId = selecionado.getId();
                                UserSession.salvarUsuarioLogado(MainActivity.this, meuId);
                                Toast.makeText(MainActivity.this, "Logado como " + selecionado.getNome(), Toast.LENGTH_SHORT).show();
                                carregarSugestoes();
                            })
                            .show();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao carregar usuários", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioSimples>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void carregarSugestoes() {
        apiService.getSugestoes(meuId).enqueue(new Callback<List<MatchSuggestion>>() {
            @Override
            public void onResponse(Call<List<MatchSuggestion>> call, Response<List<MatchSuggestion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sugestoes.clear();
                    sugestoes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao carregar sugestões", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MatchSuggestion>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void curtirPessoa(MatchSuggestion pessoa) {
        apiService.curtir(meuId, pessoa.getId()).enqueue(new Callback<SwipeResponse>() {
            @Override
            public void onResponse(Call<SwipeResponse> call, Response<SwipeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, response.body().getMensagem(), Toast.LENGTH_LONG).show();
                    sugestoes.remove(pessoa);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<SwipeResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Falha ao curtir: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}