package com.meetbeat.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.MatchDetalheActivity;
import com.meetbeat.app.R;
import com.meetbeat.app.model.MatchConfirmado;
import com.meetbeat.app.util.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class MatchConfirmadoAdapter extends RecyclerView.Adapter<MatchConfirmadoAdapter.ViewHolder> {

    private final List<MatchConfirmado> lista;

    public MatchConfirmadoAdapter(List<MatchConfirmado> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_confirmado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchConfirmado pessoa = lista.get(position);

        holder.textNome.setText(pessoa.getNome());

        Context context = holder.itemView.getContext();

        // Carrega a foto (local ou via URL do servidor, dependendo do formato salvo)
        ImageLoader.carregar(context, pessoa.getFotoShow(), holder.imageFoto);

        // Clique no card inteiro: abre a tela de detalhe do match
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MatchDetalheActivity.class);
            intent.putExtra(MatchDetalheActivity.EXTRA_NOME, pessoa.getNome());
            intent.putExtra(MatchDetalheActivity.EXTRA_FOTO, pessoa.getFotoShow());
            intent.putStringArrayListExtra(MatchDetalheActivity.EXTRA_GENEROS, new ArrayList<>(pessoa.getGenerosComuns()));
            context.startActivity(intent);
        });

        // Clique no botão de mensagem: mantém o Toast simulando
        holder.btnMensagem.setOnClickListener(v -> {
            Toast.makeText(context, "Função de mensagens em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNome;
        ImageView imageFoto, btnMensagem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textMatchNome);
            imageFoto = itemView.findViewById(R.id.imageMatchFoto);
            btnMensagem = itemView.findViewById(R.id.btnMensagem);
        }
    }
}