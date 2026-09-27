package com.meetbeat.app.fragment;

import android.content.Intent;
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

import com.meetbeat.app.CurtidasActivity;
import com.meetbeat.app.MatchesActivity;
import com.meetbeat.app.R;
import com.meetbeat.app.adapter.MatchAdapter;
import com.meetbeat.app.model.MatchSuggestion;
import com.meetbeat.app.model.SwipeResponse;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.UserSession;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchingFragment extends Fragment {

    private long meuId;
    private MatchAdapter adapter;
    private final List<MatchSuggestion> sugestoes = new ArrayList<>();
    private ApiService apiService;

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

        view.findViewById(R.id.btnVerMatches).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MatchesActivity.class)));

        view.findViewById(R.id.btnVerCurtidas).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CurtidasActivity.class)));

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

    private void carregarSugestoes() {
        apiService.getSugestoes(meuId).enqueue(new Callback<List<MatchSuggestion>>() {
            @Override
            public void onResponse(Call<List<MatchSuggestion>> call, Response<List<MatchSuggestion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sugestoes.clear();
                    sugestoes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<MatchSuggestion>> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void curtirPessoa(MatchSuggestion pessoa) {
        apiService.curtir(meuId, pessoa.getId()).enqueue(new Callback<SwipeResponse>() {
            @Override
            public void onResponse(Call<SwipeResponse> call, Response<SwipeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), response.body().getMensagem(), Toast.LENGTH_LONG).show();
                    int posicao = sugestoes.indexOf(pessoa);
                    if (posicao != -1) {
                        sugestoes.remove(posicao);
                        adapter.notifyItemRemoved(posicao);
                    }
                }
            }

            @Override
            public void onFailure(Call<SwipeResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha ao curtir: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void rejeitarPessoa(MatchSuggestion pessoa) {
        apiService.rejeitar(meuId, pessoa.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    int posicao = sugestoes.indexOf(pessoa);
                    if (posicao != -1) {
                        sugestoes.remove(posicao);
                        adapter.notifyItemRemoved(posicao);
                    }
                    Toast.makeText(requireContext(), "Sugestão removida", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha ao remover sugestão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}