package com.meetbeat.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Camada desenhada por cima da imagem: escurece tudo o que fica fora do recorte, contorna a área
 * recortada (círculo para foto de perfil, retângulo para fotos de show) e mostra uma grade de terços
 * para ajudar a posicionar o assunto da foto.
 */
public class MolduraRecorteView extends View {

    private final Paint pinturaFora = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pinturaBorda = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pinturaGrade = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path caminho = new Path();

    private RectF moldura;
    private boolean circular;

    public MolduraRecorteView(Context context) {
        this(context, null);
    }

    public MolduraRecorteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float densidade = getResources().getDisplayMetrics().density;

        pinturaFora.setColor(Color.argb(170, 0, 0, 0));
        pinturaFora.setStyle(Paint.Style.FILL);

        pinturaBorda.setColor(Color.WHITE);
        pinturaBorda.setStyle(Paint.Style.STROKE);
        pinturaBorda.setStrokeWidth(2 * densidade);

        pinturaGrade.setColor(Color.argb(90, 255, 255, 255));
        pinturaGrade.setStyle(Paint.Style.STROKE);
        pinturaGrade.setStrokeWidth(densidade);
    }

    public void definirMoldura(RectF moldura, boolean circular) {
        this.moldura = new RectF(moldura);
        this.circular = circular;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (moldura == null) return;

        caminho.reset();
        caminho.setFillType(Path.FillType.EVEN_ODD);
        caminho.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        if (circular) {
            caminho.addOval(moldura, Path.Direction.CW);
        } else {
            caminho.addRect(moldura, Path.Direction.CW);
        }
        canvas.drawPath(caminho, pinturaFora);

        float tercoX = moldura.width() / 3f;
        float tercoY = moldura.height() / 3f;
        canvas.save();
        if (circular) {
            Path recorte = new Path();
            recorte.addOval(moldura, Path.Direction.CW);
            canvas.clipPath(recorte);
        }
        for (int i = 1; i <= 2; i++) {
            canvas.drawLine(moldura.left + tercoX * i, moldura.top, moldura.left + tercoX * i, moldura.bottom, pinturaGrade);
            canvas.drawLine(moldura.left, moldura.top + tercoY * i, moldura.right, moldura.top + tercoY * i, pinturaGrade);
        }
        canvas.restore();

        if (circular) {
            canvas.drawOval(moldura, pinturaBorda);
        } else {
            canvas.drawRect(moldura, pinturaBorda);
        }
    }
}
