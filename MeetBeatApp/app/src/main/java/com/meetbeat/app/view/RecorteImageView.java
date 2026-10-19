package com.meetbeat.app.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Imagem que pode ser arrastada e ampliada (pinça ou toque duplo) por baixo de uma moldura de
 * recorte. A imagem sempre cobre a moldura por inteiro, então nunca sobra área vazia no recorte.
 */
public class RecorteImageView extends AppCompatImageView {

    private static final float ZOOM_MAXIMO = 6f;   // em relação ao zoom mínimo
    private static final float ZOOM_TOQUE_DUPLO = 2.5f;

    private final Matrix matriz = new Matrix();
    private final float[] valores = new float[9];
    private final ScaleGestureDetector detectorPinca;
    private final GestureDetector detectorGestos;

    private Bitmap bitmap;
    private RectF moldura;
    private float escalaMinima = 1f;
    private ValueAnimator animacao;

    public RecorteImageView(Context context) {
        this(context, null);
    }

    public RecorteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);

        detectorPinca = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                aplicarZoom(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                return true;
            }
        });

        detectorGestos = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                if (animacao != null) animacao.cancel();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanciaX, float distanciaY) {
                matriz.postTranslate(-distanciaX, -distanciaY);
                manterDentroDaMoldura();
                return true;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                float alvo = escalaAtual() < escalaMinima * 1.5f ? escalaMinima * ZOOM_TOQUE_DUPLO : escalaMinima;
                animarZoomPara(alvo, e.getX(), e.getY());
                return true;
            }
        });
    }

    public void definirImagem(Bitmap bitmap) {
        this.bitmap = bitmap;
        setImageBitmap(bitmap);
        enquadrar();
    }

    /** Área da tela (em pixels da view) que será recortada. */
    public void definirMoldura(RectF moldura) {
        this.moldura = new RectF(moldura);
        enquadrar();
    }

    /** Posiciona a imagem com o menor zoom que ainda cobre a moldura, centralizada nela. */
    private void enquadrar() {
        if (bitmap == null || moldura == null) return;
        escalaMinima = Math.max(moldura.width() / bitmap.getWidth(), moldura.height() / bitmap.getHeight());
        matriz.setScale(escalaMinima, escalaMinima);
        matriz.postTranslate(moldura.centerX() - bitmap.getWidth() * escalaMinima / 2f,
                moldura.centerY() - bitmap.getHeight() * escalaMinima / 2f);
        setImageMatrix(matriz);
    }

    private float escalaAtual() {
        matriz.getValues(valores);
        return valores[Matrix.MSCALE_X];
    }

    private void aplicarZoom(float fator, float focoX, float focoY) {
        float atual = escalaAtual();
        float nova = Math.max(escalaMinima, Math.min(atual * fator, escalaMinima * ZOOM_MAXIMO));
        matriz.postScale(nova / atual, nova / atual, focoX, focoY);
        manterDentroDaMoldura();
    }

    private void animarZoomPara(float alvo, float focoX, float focoY) {
        if (animacao != null) animacao.cancel();
        animacao = ValueAnimator.ofFloat(escalaAtual(), alvo);
        animacao.setDuration(220);
        animacao.addUpdateListener(a -> aplicarZoom((float) a.getAnimatedValue() / escalaAtual(), focoX, focoY));
        animacao.start();
    }

    /** Desloca a imagem o mínimo necessário para que ela continue cobrindo toda a moldura. */
    private void manterDentroDaMoldura() {
        if (bitmap == null || moldura == null) return;
        RectF limites = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        matriz.mapRect(limites);

        float dx = 0, dy = 0;
        if (limites.left > moldura.left) dx = moldura.left - limites.left;
        else if (limites.right < moldura.right) dx = moldura.right - limites.right;
        if (limites.top > moldura.top) dy = moldura.top - limites.top;
        else if (limites.bottom < moldura.bottom) dy = moldura.bottom - limites.bottom;

        matriz.postTranslate(dx, dy);
        setImageMatrix(matriz);
    }

    /** Gera o bitmap da área dentro da moldura, com o lado maior limitado a {@code ladoMaximo}. */
    public Bitmap recortar(int ladoMaximo) {
        if (bitmap == null || moldura == null) return null;

        Matrix inversa = new Matrix();
        matriz.invert(inversa);
        RectF area = new RectF(moldura);
        inversa.mapRect(area);

        Rect origem = new Rect(
                Math.max(0, Math.round(area.left)),
                Math.max(0, Math.round(area.top)),
                Math.min(bitmap.getWidth(), Math.round(area.right)),
                Math.min(bitmap.getHeight(), Math.round(area.bottom)));

        float reducao = Math.min(1f, (float) ladoMaximo / Math.max(origem.width(), origem.height()));
        int largura = Math.max(1, Math.round(origem.width() * reducao));
        int altura = Math.max(1, Math.round(origem.height() * reducao));

        Bitmap resultado = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888);
        new Canvas(resultado).drawBitmap(bitmap, origem, new Rect(0, 0, largura, altura),
                new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG));
        return resultado;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        detectorPinca.onTouchEvent(event);
        if (!detectorPinca.isInProgress()) {
            detectorGestos.onTouchEvent(event);
        }
        return true;
    }
}
