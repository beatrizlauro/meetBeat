package com.meetbeat.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.R;
import com.meetbeat.app.model.CurtidaRecebida;
import com.meetbeat.app.util.ImageLoader;

import java.util.List;

public class CurtidaEnviadaAdapter extends RecyclerView.Adapter<CurtidaEnviadaAdapter.ViewHolder> {

    public interface OnPessoaClickListener {
        void onClick(CurtidaRecebida pessoa);
    }

    private final List<CurtidaRecebida> lista;
    private final OnPessoaClickListener listener;

    public CurtidaEnviadaAdapter(List<CurtidaRecebida> lista, OnPessoaClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_curtida_enviada, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CurtidaRecebida pessoa = lista.get(position);

        holder.textNome.setText(pessoa.getNome());
        holder.textGeneros.setText(String.join(", ", pessoa.getGenerosComuns()));

        Context context = holder.itemView.getContext();
        ImageLoader.carregar(context, pessoa.getFotoShow(), holder.imageFoto);

        holder.itemView.setOnClickListener(v -> listener.onClick(pessoa));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNome, textGeneros;
        ImageView imageFoto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textCurtidaEnviadaNome);
            textGeneros = itemView.findViewById(R.id.textCurtidaEnviadaGeneros);
            imageFoto = itemView.findViewById(R.id.imageCurtidaEnviadaFoto);
        }
    }
}
