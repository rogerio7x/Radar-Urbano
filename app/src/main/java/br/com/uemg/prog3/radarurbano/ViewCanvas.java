package br.com.uemg.prog3.radarurbano;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/** View de desenho livre (aula Simple Paint) com a foto como fundo. */
public class ViewCanvas extends View {

    private static final int TOLERANCIA_MOVIMENTO = 4;

    private final List<Path> caminhos = new ArrayList<>();
    private Path path;
    private Paint paint;
    private Bitmap fundo;
    private final RectF destino = new RectF();
    private float eixoX, eixoY;

    public ViewCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        inicializaObjetos();
    }

    private void inicializaObjetos() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setFundo(Bitmap bitmap) {
        this.fundo = bitmap;
        calcularDestino();
        invalidate();
    }

    private void calcularDestino() {
        int w = getWidth(), h = getHeight();
        if (fundo == null || w == 0 || h == 0) {
            destino.set(0, 0, w, h);
            return;
        }
        float escala = Math.min((float) w / fundo.getWidth(), (float) h / fundo.getHeight());
        float dw = fundo.getWidth() * escala;
        float dh = fundo.getHeight() * escala;
        float esq = (w - dw) / 2f;
        float topo = (h - dh) / 2f;
        destino.set(esq, topo, esq + dw, topo + dh);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calcularDestino();
    }

    private void desenhar(Canvas canvas) {
        if (fundo != null) canvas.drawBitmap(fundo, null, destino, null);
        for (Path p : caminhos) canvas.drawPath(p, paint);
        if (path != null) canvas.drawPath(path, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        desenhar(canvas);
    }

    private void inicioToque(float x, float y) {
        path = new Path();
        path.moveTo(x, y);
        eixoX = x;
        eixoY = y;
    }

    private void moveuDedoTela(float x, float y) {
        float distanciaX = Math.abs(x - eixoX);
        float distanciaY = Math.abs(y - eixoY);
        if (distanciaX >= TOLERANCIA_MOVIMENTO || distanciaY >= TOLERANCIA_MOVIMENTO) {
            path.quadTo(eixoX, eixoY, (x + eixoX) / 2, (y + eixoY) / 2);
            eixoX = x;
            eixoY = y;
        }
    }

    private void tirouDedoTela() {
        path.lineTo(eixoX, eixoY);
        caminhos.add(path);
        path = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                inicioToque(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (path != null) { moveuDedoTela(x, y); invalidate(); }
                break;
            case MotionEvent.ACTION_UP:
                if (path != null) { tirouDedoTela(); invalidate(); }
                break;
        }
        return true;
    }

    public void limparCanvas() {
        caminhos.clear();
        path = null;
        invalidate();
    }

    public void desfazer() {
        if (!caminhos.isEmpty()) {
            caminhos.remove(caminhos.size() - 1);
            invalidate();
        }
    }

    /** Gera a foto + desenho na resolução do bitmap original. */
    public Bitmap gerarBitmap() {
        if (fundo == null) return null;
        Bitmap saida = Bitmap.createBitmap(fundo.getWidth(), fundo.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(saida);
        float escala = fundo.getWidth() / destino.width();
        c.scale(escala, escala);
        c.translate(-destino.left, -destino.top);
        desenhar(c);
        return saida;
    }
}
