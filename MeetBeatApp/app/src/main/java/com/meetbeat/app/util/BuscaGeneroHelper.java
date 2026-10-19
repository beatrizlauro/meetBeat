package com.meetbeat.app.util;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.R;
import com.meetbeat.app.adapter.GeneroCardAdapter;

/**
 * Liga a barra de busca (layout view_busca_genero) ao GeneroCardAdapter, filtrando os
 * cards a cada letra digitada. Usado tanto no onboarding quanto na edição de gêneros.
 */
public class BuscaGeneroHelper {

    private BuscaGeneroHelper() {
    }

    public static void configurar(Activity activity, GeneroCardAdapter adapter,
                                  RecyclerView recyclerView, TextView textSemResultado) {
        EditText editBusca = activity.findViewById(R.id.editBuscaGenero);
        View btnLimpar = activity.findViewById(R.id.btnLimparBuscaGenero);

        editBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String termo = s.toString();
                adapter.filtrar(termo);
                btnLimpar.setVisibility(termo.isEmpty() ? View.GONE : View.VISIBLE);
                recyclerView.scrollToPosition(0);

                boolean vazio = adapter.getItemCount() == 0 && !termo.trim().isEmpty();
                textSemResultado.setText("Nenhum gênero encontrado para \"" + termo.trim() + "\"");
                textSemResultado.setVisibility(vazio ? View.VISIBLE : View.GONE);
            }
        });

        btnLimpar.setOnClickListener(v -> editBusca.setText(""));

        // A tecla de busca do teclado só fecha o teclado, já que a filtragem acontece enquanto se digita
        editBusca.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                esconderTeclado(activity, editBusca);
                return true;
            }
            return false;
        });

        // Ao começar a rolar a lista, o teclado sai da frente para liberar espaço para os cards
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    esconderTeclado(activity, editBusca);
                }
            }
        });
    }

    private static void esconderTeclado(Activity activity, EditText editBusca) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editBusca.getWindowToken(), 0);
        }
        editBusca.clearFocus();
    }
}
