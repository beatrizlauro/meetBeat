package com.meetbeat.app.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.R;
import com.meetbeat.app.model.MatchSuggestion;
import com.meetbeat.app.util.VisualHelper;
import com.meetbeat.app.util.ImageLoader;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    public interface OnMatchActionListener {
        void onCurtir(MatchSuggestion pessoa);
        void onRejeitar(MatchSuggestion pessoa);
    }

    private final List<MatchSuggestion> lista;
    private final OnMatchActionListener listener;

    public MatchAdapter(List<MatchSuggestion> lista, OnMatchActionListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_card, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchSuggestion pessoa = lista.get(position);

        holder.textNome.setText(pessoa.getNome());

        String generos = String.join(", ", pessoa.getGenerosComuns());
        holder.textScore.setText(generos);

        holder.textAvatarInicial.setText(VisualHelper.getInicial(pessoa.getNome()));
        GradientDrawable bgAvatar = (GradientDrawable) holder.textAvatarInicial.getBackground();
        bgAvatar.setColor(VisualHelper.getCorAvatar(pessoa.getNome()));

        // Carrega a foto real do show pelo nome do arquivo salvo no banco
        Context context = holder.itemView.getContext();
        ImageLoader.carregar(context, pessoa.getFotoShow(), holder.imageCardBackground);

        holder.btnCurtir.setOnClickListener(v -> listener.onCurtir(pessoa));
        holder.btnRejeitar.setOnClickListener(v -> listener.onRejeitar(pessoa));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView textNome, textScore, btnCurtir, btnRejeitar, textAvatarInicial;
        ImageView imageCardBackground;

        MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textNome);
            textScore = itemView.findViewById(R.id.textScore);
            btnCurtir = itemView.findViewById(R.id.btnCurtir);
            btnRejeitar = itemView.findViewById(R.id.btnRejeitar);
            textAvatarInicial = itemView.findViewById(R.id.textAvatarInicial);
            imageCardBackground = itemView.findViewById(R.id.imageCardBackground);
        }
    }
}