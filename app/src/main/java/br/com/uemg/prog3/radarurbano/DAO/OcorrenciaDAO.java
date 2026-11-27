package br.com.uemg.prog3.radarurbano.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import br.com.uemg.prog3.radarurbano.Model.OcorrenciaModel;

import java.util.ArrayList;

public class OcorrenciaDAO extends SQLiteOpenHelper {

    private static final String DATABASE = "radarurbano";
    private static final int VERSION = 1;

    public String msmErro = "";

    public OcorrenciaDAO(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE ocorrencias(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "titulo TEXT NOT NULL, " +
                "descricao TEXT, " +
                "categoria TEXT NOT NULL, " +
                "data TEXT NOT NULL, " +
                "latitude REAL NOT NULL DEFAULT 0, " +
                "longitude REAL NOT NULL DEFAULT 0, " +
                "foto TEXT, " +
                "resolvido INTEGER NOT NULL DEFAULT 0);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS ocorrencias");
            onCreate(db);
        }
    }

    private ContentValues valores(OcorrenciaModel o) {
        ContentValues v = new ContentValues();
        v.put("titulo", o.getTitulo());
        v.put("descricao", o.getDescricao());
        v.put("categoria", o.getCategoria());
        v.put("data", o.getData());
        v.put("latitude", o.getLatitude());
        v.put("longitude", o.getLongitude());
        v.put("foto", o.getFoto());
        v.put("resolvido", o.isResolvido() ? 1 : 0);
        return v;
    }

    // CREATE
    public boolean salvarOcorrencia(OcorrenciaModel o) {
        try {
            long id = getWritableDatabase().insert("ocorrencias", null, valores(o));
            return id != -1;
        } catch (Exception ex) {
            msmErro = ex.getMessage();
            return false;
        }
    }

    // UPDATE
    public boolean alterarOcorrencia(OcorrenciaModel o) {
        try {
            String[] args = {String.valueOf(o.getId())};
            getWritableDatabase().update("ocorrencias", valores(o), "id=?", args);
            return true;
        } catch (Exception ex) {
            msmErro = ex.getMessage();
            return false;
        }
    }

    // DELETE
    public boolean deletarOcorrencia(OcorrenciaModel o) {
        try {
            String[] args = {String.valueOf(o.getId())};
            getWritableDatabase().delete("ocorrencias", "id=?", args);
            return true;
        } catch (Exception ex) {
            msmErro = ex.getMessage();
            return false;
        }
    }

    // READ
    public ArrayList<OcorrenciaModel> listaOcorrencias() {
        String[] colunas = {"id", "titulo", "descricao", "categoria", "data",
                "latitude", "longitude", "foto", "resolvido"};
        ArrayList<OcorrenciaModel> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().query("ocorrencias", colunas,
                null, null, null, null, "id DESC");
        while (c.moveToNext()) {
            OcorrenciaModel o = new OcorrenciaModel();
            o.setId(c.getLong(0));
            o.setTitulo(c.getString(1));
            o.setDescricao(c.getString(2));
            o.setCategoria(c.getString(3));
            o.setData(c.getString(4));
            o.setLatitude(c.getDouble(5));
            o.setLongitude(c.getDouble(6));
            o.setFoto(c.getString(7));
            o.setResolvido(c.getInt(8) == 1);
            lista.add(o);
        }
        c.close();
        return lista;
    }

    public int contarPendentes() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM ocorrencias WHERE resolvido = 0", null);
        int total = 0;
        if (c.moveToFirst()) total = c.getInt(0);
        c.close();
        return total;
    }
}
