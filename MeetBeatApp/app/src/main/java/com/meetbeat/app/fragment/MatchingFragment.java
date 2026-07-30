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
                sugestoes.remove(pessoa);
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Sugestão removida", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnVerMatches).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MatchesActivity.class)));

        carregarSugestoes();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        long idAtual = UserSession.getUsuarioLogado(requireContext());
        if (idAtual != meuId) {
            meuId = idAtual;
            carregarSugestoes();
        }
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
                    sugestoes.remove(pessoa);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<SwipeResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha ao curtir: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}