package com.meetbeat.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.meetbeat.app.fragment.CarpoolFragment;
import com.meetbeat.app.fragment.HomeFragment;
import com.meetbeat.app.fragment.MatchingFragment;
import com.meetbeat.app.fragment.ProfileFragment;
import com.meetbeat.app.fragment.TicketFragment;

public class MainContainerActivity extends AppCompatActivity {

    private View underline;
    private ImageView[] icones;
    private View[] navItems;
    private int abaAtualIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);

        underline = findViewById(R.id.underlineIndicator);

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

        navItems[0].setOnClickListener(v -> selecionarAba(0, new HomeFragment()));
        navItems[1].setOnClickListener(v -> selecionarAba(1, new TicketFragment()));
        navItems[2].setOnClickListener(v -> selecionarAba(2, new MatchingFragment()));
        navItems[3].setOnClickListener(v -> selecionarAba(3, new CarpoolFragment()));
        navItems[4].setOnClickListener(v -> selecionarAba(4, new ProfileFragment()));

        // Abre direto na Home, sem animação (primeira carga)
        trocarFragment(new HomeFragment());
        atualizarCores(0);

        // Espera o layout terminar de desenhar para posicionar a linha corretamente na primeira vez
        underline.post(() -> moverLinhaPara(0, false));
    }

    private void selecionarAba(int index, Fragment fragment) {
        if (index == abaAtualIndex) return;

        trocarFragment(fragment);
        moverLinhaPara(index, true);
        atualizarCores(index);
        abaAtualIndex = index;
    }

    private void trocarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void moverLinhaPara(int index, boolean animado) {
        View itemAlvo = navItems[index];

        float destinoX = itemAlvo.getX() + (itemAlvo.getWidth() - underline.getWidth()) / 2f;

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
            int cor = (i == indexAtivo) ? corAtivo : corInativo;
            icones[i].setColorFilter(cor);
        }
    }
}