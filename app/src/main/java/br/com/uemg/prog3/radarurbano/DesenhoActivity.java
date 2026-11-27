package br.com.uemg.prog3.radarurbano;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;

public class DesenhoActivity extends AppCompatActivity {

    private ViewCanvas viewCanvas;
    private String caminhoFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desenho);
        setTitle("Marque o problema");

        viewCanvas = findViewById(R.id.area_desenho);
        caminhoFoto = getIntent().getStringExtra("foto");

        Bitmap bmp = ImagemUtil.carregar(caminhoFoto, 1600);
        if (bmp == null) {
            Toast.makeText(this, "Não foi possível abrir a foto.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        viewCanvas.setFundo(bmp);
    }

    public void limparDesenho(View view) {
        viewCanvas.limparCanvas();
    }

    public void desfazer(View view) {
        viewCanvas.desfazer();
    }

    public void salvarDesenho(View view) {
        Bitmap resultado = viewCanvas.gerarBitmap();
        if (resultado == null) return;
        try (FileOutputStream out = new FileOutputStream(caminhoFoto)) {
            resultado.compress(Bitmap.CompressFormat.JPEG, 90, out);
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
