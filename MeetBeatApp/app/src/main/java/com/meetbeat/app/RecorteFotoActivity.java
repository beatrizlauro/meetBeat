package com.meetbeat.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;

import com.meetbeat.app.view.MolduraRecorteView;
import com.meetbeat.app.view.RecorteImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Permite ajustar o zoom e a posição de uma foto antes do envio. A moldura tem o mesmo formato
 * do lugar onde a foto será exibida (círculo do avatar, card do feed ou grade de fotos do perfil),
 * então o que fica dentro dela é exatamente o que os outros usuários vão ver.
 *
 * Devolve, no {@code getData()} do resultado, a Uri de um JPEG já recortado.
 */
public class RecorteFotoActivity extends AppCompatActivity {

    public enum Formato { PERFIL, FEED, POST }

    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_FORMATO = "formato";
    private static final int LADO_MAXIMO_LEITURA = 2048;
    private static final int LADO_MAXIMO_SAIDA = 1080;

    private RecorteImageView imageRecorte;
    private MolduraRecorteView moldura;
    private View areaRecorte;
    private Formato formato;
    private boolean imagemCarregada;
    private boolean molduraAplicada;
    private int ultimaLargura, ultimaAltura;

    public static Intent criarIntent(Context context, Uri uri, Formato formato) {
        return new Intent(context, RecorteFotoActivity.class)
                .putExtra(EXTRA_URI, uri)
                .putExtra(EXTRA_FORMATO, formato.name());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorte_foto);

        Uri uri = IntentCompat.getParcelableExtra(getIntent(), EXTRA_URI, Uri.class);
        formato = Formato.valueOf(getIntent().getStringExtra(EXTRA_FORMATO));

        imageRecorte = findViewById(R.id.imageRecorte);
        moldura = findViewById(R.id.molduraRecorte);
        areaRecorte = findViewById(R.id.areaRecorte);

        TextView titulo = findViewById(R.id.textTituloRecorte);
        TextView dica = findViewById(R.id.textDicaRecorte);
        switch (formato) {
            case PERFIL:
                titulo.setText("Foto de perfil");
                dica.setText("Arraste para posicionar e use dois dedos para dar zoom.");
                break;
            case FEED:
                titulo.setText("Foto do feed");
                dica.setText("A área destacada é exatamente o que aparece no seu card no feed de sugestões.");
                break;
            default:
                titulo.setText("Foto de show");
                dica.setText("A área destacada é como a foto aparece na grade do seu perfil.");
                break;
        }

        findViewById(R.id.btnCancelarRecorte).setOnClickListener(v -> finish());
        findViewById(R.id.btnUsarFoto).setOnClickListener(v -> concluir());

        // A moldura depende do tamanho real da área, que só é conhecido depois do layout
        areaRecorte.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> posicionarMoldura());

        carregarImagem(uri);
    }

    private void carregarImagem(Uri uri) {
        new Thread(() -> {
            Bitmap bitmap = null;
            try {
                bitmap = decodificarComOrientacao(uri);
            } catch (Exception ignored) {
            }
            Bitmap resultado = bitmap;
            runOnUiThread(() -> {
                findViewById(R.id.progressRecorte).setVisibility(View.GONE);
                if (resultado == null) {
                    Toast.makeText(this, "Não foi possível abrir essa foto", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                imageRecorte.definirImagem(resultado);
                imagemCarregada = true;
                posicionarMoldura();
            });
        }).start();
    }

    /** Proporção largura/altura do lugar onde a foto será exibida, medida nesta tela. */
    private float proporcao() {
        DisplayMetrics metricas = getResources().getDisplayMetrics();
        float larguraTelaDp = metricas.widthPixels / metricas.density;
        switch (formato) {
            case FEED:
                // Card do feed (item_match_card): margem de 16dp de cada lado e imagem com 320dp de altura
                return (larguraTelaDp - 32f) / 320f;
            case POST:
                // Grade do perfil (item_post_grid): 2 colunas, margem de 2dp e 180dp de altura
                return (larguraTelaDp / 2f - 4f) / 180f;
            default:
                return 1f;
        }
    }

    private void posicionarMoldura() {
        int largura = areaRecorte.getWidth();
        int altura = areaRecorte.getHeight();
        if (largura == 0 || altura == 0) return;
        // Só reposiciona se o tamanho mudou, para não desfazer o zoom que a pessoa já ajustou
        if (molduraAplicada && largura == ultimaLargura && altura == ultimaAltura) return;
        ultimaLargura = largura;
        ultimaAltura = altura;

        float margem = 24 * getResources().getDisplayMetrics().density;
        float disponivelL = largura - 2 * margem;
        float disponivelA = altura - 2 * margem;
        float p = proporcao();

        float molduraL = disponivelL;
        float molduraA = molduraL / p;
        if (molduraA > disponivelA) {
            molduraA = disponivelA;
            molduraL = molduraA * p;
        }

        RectF retangulo = new RectF(
                (largura - molduraL) / 2f, (altura - molduraA) / 2f,
                (largura + molduraL) / 2f, (altura + molduraA) / 2f);
        moldura.definirMoldura(retangulo, formato == Formato.PERFIL);
        if (imagemCarregada) {
            imageRecorte.definirMoldura(retangulo);
            molduraAplicada = true;
        }
    }

    private void concluir() {
        if (!imagemCarregada) return;
        try {
            Bitmap recorte = imageRecorte.recortar(LADO_MAXIMO_SAIDA);
            File arquivo = File.createTempFile("recorte_", ".jpg", getCacheDir());
            try (FileOutputStream saida = new FileOutputStream(arquivo)) {
                recorte.compress(Bitmap.CompressFormat.JPEG, 90, saida);
            }
            recorte.recycle();
            setResult(RESULT_OK, new Intent().setData(Uri.fromFile(arquivo)));
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao recortar a foto", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Lê a foto já reduzida (para não estourar a memória com fotos de câmera de 12MP ou mais)
     * e aplica a rotação gravada no EXIF, já que muitas câmeras salvam a foto "deitada".
     */
    private Bitmap decodificarComOrientacao(Uri uri) throws Exception {
        BitmapFactory.Options medidas = new BitmapFactory.Options();
        medidas.inJustDecodeBounds = true;
        try (InputStream entrada = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(entrada, null, medidas);
        }

        BitmapFactory.Options opcoes = new BitmapFactory.Options();
        opcoes.inSampleSize = 1;
        while (medidas.outWidth / (opcoes.inSampleSize * 2) >= LADO_MAXIMO_LEITURA
                || medidas.outHeight / (opcoes.inSampleSize * 2) >= LADO_MAXIMO_LEITURA) {
            opcoes.inSampleSize *= 2;
        }

        Bitmap bitmap;
        try (InputStream entrada = getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(entrada, null, opcoes);
        }
        if (bitmap == null) return null;

        int orientacao;
        try (InputStream entrada = getContentResolver().openInputStream(uri)) {
            orientacao = new ExifInterface(entrada).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception e) {
            orientacao = ExifInterface.ORIENTATION_NORMAL;
        }

        Matrix rotacao = new Matrix();
        switch (orientacao) {
            case ExifInterface.ORIENTATION_ROTATE_90: rotacao.postRotate(90); break;
            case ExifInterface.ORIENTATION_ROTATE_180: rotacao.postRotate(180); break;
            case ExifInterface.ORIENTATION_ROTATE_270: rotacao.postRotate(270); break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: rotacao.postScale(-1, 1); break;
            default: return bitmap;
        }
        Bitmap girado = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotacao, true);
        if (girado != bitmap) bitmap.recycle();
        return girado;
    }
}
