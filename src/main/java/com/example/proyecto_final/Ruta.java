package com.example.proyecto_final;

import java.util.Arrays;
import java.util.Objects;

public class Ruta {
    private final Parada origen;
    private final Parada destino;
    private double tiempo;
    private double distancia;
    private double costo;
    private String criterio;

    public Ruta(Parada origen, Parada destino, double tiempo, double distancia, double costo) {
        if (origen == null || destino == null) throw new IllegalArgumentException("Paradas inválidas");
        if (origen.equals(destino)) throw new IllegalArgumentException("Misma parada");
        if (tiempo <= 0) throw new IllegalArgumentException("Tiempo inválido");
        if (distancia <= 0) throw new IllegalArgumentException("Distancia inválida");
        if (costo < 0) throw new IllegalArgumentException("Costo inválido");

        this.origen = origen;
        this.destino = destino;
        this.tiempo = tiempo;
        this.distancia = distancia;
        this.costo = costo;
        this.criterio = "tiempo"; // un Default criterio
    }

    // Getters
    public Parada getOrigen() { return origen; }
    public Parada getDestino() { return destino; }
    public double getTiempo() { return tiempo; }
    public double getDistancia() { return distancia; }
    public double getCosto() { return costo; }
    public String getCriterio() { return criterio; }

    // Setters
    public void setTiempo(double tiempo) {
        if (tiempo <= 0) throw new IllegalArgumentException("Tiempo inválido");
        this.tiempo = tiempo;
    }

    public void setDistancia(double distancia) {
        if (distancia <= 0) throw new IllegalArgumentException("Distancia inválida");
        this.distancia = distancia;
    }

    public void setCriterio(String criterio) {
        if (!Arrays.asList("tiempo", "distancia", "costo").contains(criterio))
            throw new IllegalArgumentException("Criterio inválido");
        this.criterio = criterio;
    }

    public void setCosto(double costo) {
        if (costo < 0) throw new IllegalArgumentException("Costo no puede ser negativo");
        this.costo = costo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ruta ruta)) return false;
        return origen.equals(ruta.origen) && destino.equals(ruta.destino);
    }

    @Override
    public int hashCode() { return Objects.hash(origen, destino); }

    @Override
    public String toString() {
        return String.format("%s → %s", origen, destino);
    }
}