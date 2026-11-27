package br.com.uemg.prog3.radarurbano;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.uemg.prog3.radarurbano.DAO.OcorrenciaDAO;
import br.com.uemg.prog3.radarurbano.Model.OcorrenciaModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MapaActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView tvResumo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recomendações do osmdroid
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_mapa);
        setTitle("Mapa de ocorrências");

        mapView = findViewById(R.id.mapView);
        tvResumo = findViewById(R.id.tvResumo);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
    }

    private void carregarMarcadores() {
        OcorrenciaDAO dao = new OcorrenciaDAO(this);
        List<OcorrenciaModel> todas = dao.listaOcorrencias();
        dao.close();

        mapView.getOverlays().clear();
        List<GeoPoint> pontos = new ArrayList<>();

        for (OcorrenciaModel o : todas) {
            if (!o.temLocalizacao()) continue;
            GeoPoint p = new GeoPoint(o.getLatitude(), o.getLongitude());
            pontos.add(p);

            Marker m = new Marker(mapView);
            m.setPosition(p);
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            m.setTitle((o.isResolvido() ? "[Resolvido] " : "[Pendente] ") + o.getTitulo());
            m.setSnippet(o.getCategoria() + " • " + o.getData());
            m.setSubDescription(o.getDescricao());
            mapView.getOverlays().add(m);
        }

        tvResumo.setText("Ocorrências no mapa: " + pontos.size() + " de " + todas.size());

        if (pontos.isEmpty()) {
            Toast.makeText(this, "Nenhuma ocorrência com localização.", Toast.LENGTH_LONG).show();
            mapView.getController().setZoom(14.0);
            mapView.getController().setCenter(new GeoPoint(-20.7189, -46.6097)); // Passos-MG
        } else if (pontos.size() == 1) {
            mapView.getController().setZoom(17.0);
            mapView.getController().setCenter(pontos.get(0));
        } else {
            final BoundingBox caixa = BoundingBox.fromGeoPoints(pontos);
            mapView.post(() -> mapView.zoomToBoundingBox(caixa, true, 100));
        }
        mapView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        carregarMarcadores();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
