package com.meetbeat.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.meetbeat.app.CurtidasActivity;
import com.meetbeat.app.CurtidasEnviadasActivity;
import com.meetbeat.app.EditarGenerosActivity;
import com.meetbeat.app.MatchesActivity;
import com.meetbeat.app.R;
import com.meetbeat.app.adapter.MatchAdapter;
import com.meetbeat.app.model.MatchSuggestion;
import com.meetbeat.app.model.SwipeResponse;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.Ajuda;
import com.meetbeat.app.util.EstadoLista;
import com.meetbeat.app.util.MensagensErro;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchingFragment extends Fragment {

    private static final String PREFS_DICAS = "meetbeat_dicas";
    private static final String CHAVE_DICA_FEED = "dica_feed_vista";

    private long meuId;
    private MatchAdapter adapter;
    private final List<MatchSuggestion> sugestoes = new ArrayList<>();
    private ApiService apiService;
    private EstadoLista estado;

    // Perfis com curtida ou rejeição em andamento, para ignorar toques repetidos no mesmo card
    private final Set<Long> acoesEmAndamento = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matching, container, false);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(requireContext());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerSugestoes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MatchAdapter(sugestoes, new MatchAdapter.OnMatchActionListener() {
            @Override
            public void onCurtir(MatchSuggestion pessoa) {
                curtirPessoa(pessoa);
            }

            @Override
            public void onRejeitar(MatchSuggestion pessoa) {
                rejeitarPessoa(pessoa);
            }
        });

        recyclerView.setAdapter(adapter);
        estado = new EstadoLista(view, recyclerView);

        view.findViewById(R.id.btnVerMatches).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MatchesActivity.class)));

        view.findViewById(R.id.btnVerCurtidas).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CurtidasActivity.class)));

        view.findViewById(R.id.btnVerCurtidasEnviadas).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CurtidasEnviadasActivity.class)));

        configurarDica(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Sempre recarrega ao voltar pra aba: com a navegação por deslize os fragments continuam
        // vivos entre trocas de aba, então sem isso a lista ficava presa na busca antiga mesmo
        // depois de outra pessoa se cadastrar ou completar o onboarding.
        meuId = UserSession.getUsuarioLogado(requireContext());
        carregarSugestoes();
    }

    // Dica de primeiro acesso: aparece até a pessoa tocar em "Entendi"
    private void configurarDica(View view) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_DICAS, Context.MODE_PRIVATE);
        View card = view.findViewById(R.id.cardDicaFeed);
        card.setVisibility(prefs.getBoolean(CHAVE_DICA_FEED, false) ? View.GONE : View.VISIBLE);

        view.findViewById(R.id.btnSaibaMaisDica).setOnClickListener(v -> Ajuda.mostrarComoFunciona(requireContext()));
        view.findViewById(R.id.btnFecharDica).setOnClickListener(v -> {
            prefs.edit().putBoolean(CHAVE_DICA_FEED, true).apply();
            card.setVisibility(View.GONE);
        });
    }

    private void carregarSugestoes() {
        // A rodinha só aparece na primeira carga; nas recargas ao voltar para a aba a lista atual
        // continua visível até a nova chegar
        if (sugestoes.isEmpty()) {
            estado.carregando();
        }

        apiService.getSugestoes(meuId).enqueue(new Callback<List<MatchSuggestion>>() {
            @Override
            public void onResponse(Call<List<MatchSuggestion>> call, Response<List<MatchSuggestion>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    sugestoes.clear();
                    sugestoes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    atualizarEstadoVazio();
                } else {
                    estado.erro(MatchingFragment.this::carregarSugestoes);
                }
            }

            @Override
            public void onFailure(Call<List<MatchSuggestion>> call, Throwable t) {
                if (!isAdded()) return;
                if (sugestoes.isEmpty()) {
                    estado.erro(MatchingFragment.this::carregarSugestoes);
                } else {
                    Toast.makeText(requireContext(), MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void atualizarEstadoVazio() {
        if (sugestoes.isEmpty()) {
            estado.mostrar(R.drawable.ic_genero_nota, "Você já viu todas as sugestões",
                    "Novas pessoas aparecem aqui conforme se cadastram. Adicionar mais gêneros "
                            + "ao seu perfil também traz mais sugestões.",
                    "Adicionar gêneros",
                    () -> startActivity(new Intent(requireContext(), EditarGenerosActivity.class)));
        } else {
            estado.esconder();
        }
    }

    private void curtirPessoa(MatchSuggestion pessoa) {
        if (!acoesEmAndamento.add(pessoa.getId())) return;

        apiService.curtir(meuId, pessoa.getId()).enqueue(new Callback<SwipeResponse>() {
            @Override
            public void onResponse(Call<SwipeResponse> call, Response<SwipeResponse> response) {
                acoesEmAndamento.remove(pessoa.getId());
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), response.body().getMensagem(), Toast.LENGTH_LONG).show();
                    removerDaLista(pessoa);
                } else {
                    Toast.makeText(requireContext(), "Não foi possível curtir agora. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SwipeResponse> call, Throwable t) {
                acoesEmAndamento.remove(pessoa.getId());
                if (isAdded()) {
                    Toast.makeText(requireContext(), MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void rejeitarPessoa(MatchSuggestion pessoa) {
        if (!acoesEmAndamento.add(pessoa.getId())) return;

        apiService.rejeitar(meuId, pessoa.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                acoesEmAndamento.remove(pessoa.getId());
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    int posicao = removerDaLista(pessoa);
                    oferecerDesfazer(pessoa, posicao);
                } else {
                    Toast.makeText(requireContext(), "Não foi possível remover agora. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                acoesEmAndamento.remove(pessoa.getId());
                if (isAdded()) {
                    Toast.makeText(requireContext(), MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /** Remove o card da lista e devolve a posição que ele ocupava (ou -1). */
    private int removerDaLista(MatchSuggestion pessoa) {
        int posicao = sugestoes.indexOf(pessoa);
        if (posicao != -1) {
            sugestoes.remove(posicao);
            adapter.notifyItemRemoved(posicao);
        }
        atualizarEstadoVazio();
        return posicao;
    }

    // "Não curti" deixa de ser irreversível: por alguns segundos, dá para desfazer o toque errado
    private void oferecerDesfazer(MatchSuggestion pessoa, int posicao) {
        View raiz = getView();
        if (raiz == null) return;

        Snackbar aviso = Snackbar.make(raiz, pessoa.getNome() + " foi removido(a) das sugestões", Snackbar.LENGTH_LONG)
                .setAction("Desfazer", v -> desfazerRejeicao(pessoa, posicao));
        // Mostra o aviso acima da barra de navegação flutuante, e não por trás dela
        View barra = requireActivity().findViewById(R.id.bottomNavContainer);
        if (barra != null) {
            aviso.setAnchorView(barra);
        }
        aviso.show();
    }

    private void desfazerRejeicao(MatchSuggestion pessoa, int posicao) {
        apiService.desfazerRejeicao(meuId, pessoa.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    int destino = (posicao < 0 || posicao > sugestoes.size()) ? sugestoes.size() : posicao;
                    sugestoes.add(destino, pessoa);
                    adapter.notifyItemInserted(destino);
                    atualizarEstadoVazio();
                } else {
                    Toast.makeText(requireContext(), "Não foi possível desfazer. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
