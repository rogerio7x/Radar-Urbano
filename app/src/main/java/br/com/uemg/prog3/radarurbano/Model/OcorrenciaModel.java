package br.com.uemg.prog3.radarurbano.Model;

import java.io.Serializable;

public class OcorrenciaModel implements Serializable {

    private Long id;
    private String titulo;
    private String descricao;
    private String categoria;
    private String data;
    private double latitude;
    private double longitude;
    private String foto;
    private boolean resolvido;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public boolean isResolvido() { return resolvido; }
    public void setResolvido(boolean resolvido) { this.resolvido = resolvido; }

    public boolean temLocalizacao() {
        return latitude != 0 || longitude != 0;
    }

    // Texto exibido no ListView
    @Override
    public String toString() {
        return (resolvido ? "[Resolvido] " : "[Pendente] ") + titulo
                + "\n" + categoria + " • " + data;
    }
}
