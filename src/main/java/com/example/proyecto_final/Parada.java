package com.example.proyecto_final;

import java.util.Objects;

public class Parada {
    private final String id;
    private String nombre;
    private final double x;
    private final double y;

    public Parada(String id, String nombre, double x, double y) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID inválido");
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Nombre inválido");
        if (x < 0 || y < 0) throw new IllegalArgumentException("Coordenadas negativas");

        this.id = id;
        this.nombre = nombre.trim();
        this.x = x;
        this.y = y;
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public double getX() { return x; }
    public double getY() { return y; }

    // Setters
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Nombre inválido");
        this.nombre = nombre.trim();
    }

    public double calcularDistancia(Parada otra) {
        return Math.sqrt(Math.pow(otra.x - this.x, 2) + Math.pow(otra.y - this.y, 2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parada parada)) return false;
        return id.equals(parada.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return nombre; }
}