package com.meetbeat.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.meetbeat.app.R;

public class TicketFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_placeholder, container, false);
        TextView titulo = view.findViewById(R.id.textTituloPlaceholder);
        titulo.setText("Ingressos");

        TextView descricao = view.findViewById(R.id.textDescricaoPlaceholder);
        descricao.setText("Em breve: eventos perto de você, busca de shows e revenda segura de ingressos");
        return view;
    }
}