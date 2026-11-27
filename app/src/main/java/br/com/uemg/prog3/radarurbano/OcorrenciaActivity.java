package br.com.uemg.prog3.radarurbano;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import br.com.uemg.prog3.radarurbano.DAO.OcorrenciaDAO;
import br.com.uemg.prog3.radarurbano.Model.OcorrenciaModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OcorrenciaActivity extends AppCompatActivity {

    private static final String[] CATEGORIAS = {
            "Buraco", "Iluminação danificada", "Lixo / Entulho",
            "Esgoto / Vazamento", "Sinalização", "Outro"};

    private EditText edtTitulo, edtDescricao;
    private Spinner spnCategoria;
    private ImageView imgFoto;
    private TextView txtLocal;
    private Button btnSalvar;

    private OcorrenciaModel ocorrencia;   // != null => edição
    private String fotoPath, novaFotoPath;
    private double latitude, longitude;

    private LocationManager locationManager;
    private LocationListener listenerLocal;

    // ---- Launchers (substituem startActivityForResult) ----
    private final ActivityResultLauncher<String> permCamera =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), ok -> {
                if (ok) abrirCamera();
                else Toast.makeText(this, "Permissão de câmera negada.", Toast.LENGTH_LONG).show();
            });

    private final ActivityResultLauncher<Uri> camera =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (ok) {
                    fotoPath = novaFotoPath;
                    mostrarFoto();
                } else if (novaFotoPath != null) {
                    new File(novaFotoPath).delete();
                }
                novaFotoPath = null;
            });

    private final ActivityResultLauncher<Intent> desenho =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK) mostrarFoto();
            });

    private final ActivityResultLauncher<String[]> permLocal =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    obterLocalizacao();
                } else {
                    Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocorrencia);

        edtTitulo = findViewById(R.id.edtTitulo);
        edtDescricao = findViewById(R.id.edtDescricao);
        spnCategoria = findViewById(R.id.spnCategoria);
        imgFoto = findViewById(R.id.imgFoto);
        txtLocal = findViewById(R.id.txtLocal);
        btnSalvar = findViewById(R.id.btnSalvar);
        Button btnFoto = findViewById(R.id.btnFoto);
        Button btnDesenhar = findViewById(R.id.btnDesenhar);
        Button btnLocal = findViewById(R.id.btnLocal);

        spnCategoria.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CATEGORIAS));

        ocorrencia = (OcorrenciaModel) getIntent().getSerializableExtra("ocorrencia");
        if (ocorrencia != null) {
            setTitle("Editar ocorrência");
            btnSalvar.setText("Modificar");
            edtTitulo.setText(ocorrencia.getTitulo());
            edtDescricao.setText(ocorrencia.getDescricao());
            for (int i = 0; i < CATEGORIAS.length; i++) {
                if (CATEGORIAS[i].equals(ocorrencia.getCategoria())) spnCategoria.setSelection(i);
            }
            fotoPath = ocorrencia.getFoto();
            if (ocorrencia.temLocalizacao()) {
                definirLocal(ocorrencia.getLatitude(), ocorrencia.getLongitude());
            }
            mostrarFoto();
        } else {
            setTitle("Nova ocorrência");
            btnSalvar.setText("Cadastrar");
        }

        btnFoto.setOnClickListener(v -> tirarFoto());
        btnLocal.setOnClickListener(v -> obterLocalizacao());
        btnDesenhar.setOnClickListener(v -> {
            if (fotoPath == null) {
                Toast.makeText(this, "Tire a foto primeiro.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, DesenhoActivity.class);
            intent.putExtra("foto", fotoPath);
            desenho.launch(intent);
        });
        btnSalvar.setOnClickListener(v -> salvar());
    }

    // ---------------- Câmera ----------------
    private void tirarFoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permCamera.launch(Manifest.permission.CAMERA);
        } else {
            abrirCamera();
        }
    }

    private void abrirCamera() {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File arquivo = File.createTempFile("ocorrencia_", ".jpg", dir);
            novaFotoPath = arquivo.getAbsolutePath();
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);
            camera.launch(uri);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir a câmera: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarFoto() {
        Bitmap b = ImagemUtil.carregar(fotoPath, 1024);
        if (b != null) imgFoto.setImageBitmap(b);
    }

    // ---------------- GPS ----------------
    @SuppressLint("MissingPermission")
    private void obterLocalizacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permLocal.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean rede = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps && !rede) {
            Toast.makeText(this, "Habilite a localização nas configurações.", Toast.LENGTH_LONG).show();
            return;
        }

        pararLocalizacao();
        txtLocal.setText("Obtendo localização...");

        listenerLocal = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                definirLocal(location.getLatitude(), location.getLongitude());
                pararLocalizacao();
                Toast.makeText(OcorrenciaActivity.this, "Localização capturada!",
                        Toast.LENGTH_SHORT).show();
            }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override public void onProviderEnabled(String provider) { }
            @Override public void onProviderDisabled(String provider) { }
        };

        if (gps) locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listenerLocal);
        if (rede) locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listenerLocal);
    }

    @SuppressLint("MissingPermission")
    private void pararLocalizacao() {
        if (locationManager != null && listenerLocal != null) {
            locationManager.removeUpdates(listenerLocal);
            listenerLocal = null;
        }
    }

    private void definirLocal(double lat, double lon) {
        latitude = lat;
        longitude = lon;
        txtLocal.setText(String.format(Locale.US, "Lat: %.5f   Lon: %.5f", lat, lon));
    }

    @Override
    protected void onPause() {
        super.onPause();
        pararLocalizacao();
    }

    // ---------------- Salvar ----------------
    private void salvar() {
        String titulo = edtTitulo.getText().toString().trim();
        if (titulo.isEmpty()) {
            edtTitulo.setError("Informe um título");
            edtTitulo.requestFocus();
            return;
        }

        boolean novo = (ocorrencia == null);
        OcorrenciaModel o = novo ? new OcorrenciaModel() : ocorrencia;
        o.setTitulo(titulo);
        o.setDescricao(edtDescricao.getText().toString().trim());
        o.setCategoria((String) spnCategoria.getSelectedItem());
        o.setFoto(fotoPath);
        o.setLatitude(latitude);
        o.setLongitude(longitude);
        if (novo) {
            o.setData(new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"))
                    .format(new Date()));
            o.setResolvido(false);
        }

        OcorrenciaDAO dao = new OcorrenciaDAO(this);
        boolean ok = novo ? dao.salvarOcorrencia(o) : dao.alterarOcorrencia(o);
        String erro = dao.msmErro;
        dao.close();

        if (!ok) {
            Toast.makeText(this, "Erro ao salvar: " + erro, Toast.LENGTH_LONG).show();
            return;
        }
        if (novo) {
            NotificacaoHelper.enviar(this, 1, "Ocorrência registrada",
                    o.getCategoria() + ": " + o.getTitulo());
        }
        finish();
    }
}
