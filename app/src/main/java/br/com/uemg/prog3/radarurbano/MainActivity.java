package br.com.uemg.prog3.radarurbano;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import br.com.uemg.prog3.radarurbano.DAO.OcorrenciaDAO;
import br.com.uemg.prog3.radarurbano.Model.OcorrenciaModel;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ListView lstOcorrencias;
    private final ArrayList<OcorrenciaModel> lista = new ArrayList<>();
    private ArrayAdapter<OcorrenciaModel> adapter;

    private SensorManager sensorManager;
    private Sensor acelerometro;
    private long ultimoShake = 0;

    private final ActivityResultLauncher<String> permNotificacao =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), ok -> {
                if (!ok) Toast.makeText(this,
                        "Sem permissão, os avisos não serão exibidos.", Toast.LENGTH_LONG).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificacaoHelper.criarCanal(this);
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        lstOcorrencias = findViewById(R.id.listaOcorrencias);
        lstOcorrencias.setEmptyView(findViewById(R.id.txtVazio));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
        lstOcorrencias.setAdapter(adapter);
        registerForContextMenu(lstOcorrencias);

        // Clique curto: editar
        lstOcorrencias.setOnItemClickListener((parent, view, position, id) ->
                abrirFormulario(lista.get(position)));

        findViewById(R.id.btnNova).setOnClickListener(v -> abrirFormulario(null));
        findViewById(R.id.btnMapa).setOnClickListener(v ->
                startActivity(new Intent(this, MapaActivity.class)));
        findViewById(R.id.btnLembrar).setOnClickListener(v -> lembrarPendentes());

        // Sensor (acelerômetro) para detectar o "balançar"
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void abrirFormulario(OcorrenciaModel o) {
        Intent intent = new Intent(this, OcorrenciaActivity.class);
        if (o != null) intent.putExtra("ocorrencia", o);
        startActivity(intent);
    }

    private void carregarLista() {
        OcorrenciaDAO dao = new OcorrenciaDAO(this);
        lista.clear();
        lista.addAll(dao.listaOcorrencias());
        dao.close();
        adapter.notifyDataSetChanged();
    }

    private void lembrarPendentes() {
        OcorrenciaDAO dao = new OcorrenciaDAO(this);
        int pendentes = dao.contarPendentes();
        dao.close();
        if (pendentes == 0) {
            Toast.makeText(this, "Nenhuma ocorrência pendente.", Toast.LENGTH_SHORT).show();
        } else {
            NotificacaoHelper.enviar(this, 2, "Ocorrências pendentes",
                    "Você tem " + pendentes + " ocorrência(s) aguardando solução.");
            Toast.makeText(this, "Confira as notificações!", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- Menu de contexto (clique longo) ----------
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        OcorrenciaModel o = lista.get(info.position);
        menu.setHeaderTitle(o.getTitulo());
        menu.add(0, 1, 0, o.isResolvido() ? "Marcar como pendente" : "Marcar como resolvida");
        menu.add(0, 2, 1, "Deletar ocorrência");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        OcorrenciaModel o = lista.get(info.position);

        OcorrenciaDAO dao = new OcorrenciaDAO(this);
        if (item.getItemId() == 1) {
            o.setResolvido(!o.isResolvido());
            dao.alterarOcorrencia(o);
        } else if (item.getItemId() == 2) {
            dao.deletarOcorrencia(o);
            if (o.getFoto() != null) new File(o.getFoto()).delete();
        }
        dao.close();
        carregarLista();
        return true;
    }

    // ---------- Ciclo de vida + sensor ----------
    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
        if (acelerometro != null) {
            sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0], y = event.values[1], z = event.values[2];
        float g = (float) Math.sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH;
        long agora = System.currentTimeMillis();
        if (g > 2.7f && agora - ultimoShake > 1500) {
            ultimoShake = agora;
            Toast.makeText(this, "Nova ocorrência!", Toast.LENGTH_SHORT).show();
            abrirFormulario(null);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
