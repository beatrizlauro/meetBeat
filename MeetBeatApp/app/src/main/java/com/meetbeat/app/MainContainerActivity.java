package com.meetbeat.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.meetbeat.app.adapter.MainPagerAdapter;
import com.meetbeat.app.model.UsuarioCompleto;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.AvisoDesenvolvimento;
import com.meetbeat.app.util.ImageLoader;
import com.meetbeat.app.util.UserSession;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainContainerActivity extends AppCompatActivity {

    private static final int INDICE_PERFIL = 4;
    private static final String KEY_ABA_ATUAL = "aba_atual";

    private View underline;
    private ImageView[] icones;
    private View[] navItems;
    private ViewPager2 viewPager;
    private int abaAtualIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);

        underline = findViewById(R.id.underlineIndicator);
        viewPager = findViewById(R.id.viewPager);

        icones = new ImageView[]{
                findViewById(R.id.iconHome),
                findViewById(R.id.iconTicket),
                findViewById(R.id.iconMic),
                findViewById(R.id.iconCart),
                findViewById(R.id.iconProfile)
        };

        navItems = new View[]{
                findViewById(R.id.navHome),
                findViewById(R.id.navTicket),
                findViewById(R.id.navMic),
                findViewById(R.id.navCart),
                findViewById(R.id.navProfile)
        };

        viewPager.setAdapter(new MainPagerAdapter(this));

        // Evita recriar todos os fragments vizinhos de uma vez ao arrastar; mantém 1 de cada lado pronto
        viewPager.setOffscreenPageLimit(1);

        for (int i = 0; i < navItems.length; i++) {
            int index = i;
            navItems[i].setOnClickListener(v -> viewPager.setCurrentItem(index, true));
        }

        // Cabeçalho compartilhado por todas as abas
        findViewById(R.id.iconSearch).setOnClickListener(v -> AvisoDesenvolvimento.mostrar(this, "A busca"));
        findViewById(R.id.iconNotifications).setOnClickListener(v -> abrirNotificacoes());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                moverLinhaPara(position, true);
                atualizarCores(position);
                abaAtualIndex = position;
            }
        });

        // Se a Activity está sendo recriada (ex.: troca de tema), reabre na mesma aba de antes
        int abaInicial = savedInstanceState != null ? savedInstanceState.getInt(KEY_ABA_ATUAL, 0) : 0;

        viewPager.setCurrentItem(abaInicial, false);
        atualizarCores(abaInicial);
        abaAtualIndex = abaInicial;
        carregarFotoDePerfilNaNav();

        // Reposiciona a linha sempre que a barra de navegação terminar de desenhar/redesenhar.
        // Um único post() não é suficiente: no modo escuro os recursos trocam (values-night) e a
        // foto de perfil carrega de forma assíncrona, então o layout ainda pode se ajustar depois
        // do primeiro frame, deixando a linha presa na posição antiga (aba inicial/Home).
        underline.getViewTreeObserver().addOnGlobalLayoutListener(() -> moverLinhaPara(abaAtualIndex, false));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_ABA_ATUAL, abaAtualIndex);
    }

    private void carregarFotoDePerfilNaNav() {
        long meuId = UserSession.getUsuarioLogado(this);
        ApiService apiService = RetrofitClient.getApiService();

        apiService.buscarUsuario(meuId).enqueue(new Callback<UsuarioCompleto>() {
            @Override
            public void onResponse(Call<UsuarioCompleto> call, Response<UsuarioCompleto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ImageLoader.carregarCircular(MainContainerActivity.this,
                            response.body().getFotoPerfil(), icones[INDICE_PERFIL]);
                }
            }

            @Override
            public void onFailure(Call<UsuarioCompleto> call, Throwable t) {
                // Mantém o placeholder padrão já definido no layout
            }
        });
    }

    private void moverLinhaPara(int index, boolean animado) {
        View itemAlvo = navItems[index];

        // Ainda não foi medido/desenhado nesta passada de layout — uma próxima chamada
        // (o listener é permanente) vai corrigir assim que o tamanho real estiver disponível
        if (itemAlvo.getWidth() == 0) return;

        // Usa posição absoluta na tela em vez de getX() (relativo ao pai direto), já que o
        // ícone fica aninhado dentro de outro container (navItemsContainer) e a linha em um
        // FrameLayout separado — getX() sozinho não reflete a posição real de um em relação ao outro
        int[] posicaoItem = new int[2];
        itemAlvo.getLocationOnScreen(posicaoItem);

        int[] posicaoPai = new int[2];
        ((View) underline.getParent()).getLocationOnScreen(posicaoPai);

        float destinoX = (posicaoItem[0] - posicaoPai[0]) + (itemAlvo.getWidth() - underline.getWidth()) / 2f;

        if (animado) {
            underline.animate().x(destinoX).setDuration(250).start();
        } else {
            underline.setX(destinoX);
        }
    }

    private void atualizarCores(int indexAtivo) {
        int corAtivo = ContextCompat.getColor(this, R.color.purple_primary);
        int corInativo = ContextCompat.getColor(this, R.color.text_gray);

        for (int i = 0; i < icones.length; i++) {
            if (i == INDICE_PERFIL) {
                // A foto de perfil não recebe tinta de cor: em vez disso, ganha um anel quando ativa
                icones[i].setBackgroundResource(i == indexAtivo ? R.drawable.bg_avatar_ring_active : 0);
                continue;
            }
            int cor = (i == indexAtivo) ? corAtivo : corInativo;
            icones[i].setColorFilter(cor);
        }
    }

    // Por enquanto não há notificações no sistema, então o painel mostra sempre o estado vazio
    private void abrirNotificacoes() {
        BottomSheetDialog painel = new BottomSheetDialog(this);
        painel.setContentView(R.layout.sheet_notificacoes);
        painel.show();
    }
}
