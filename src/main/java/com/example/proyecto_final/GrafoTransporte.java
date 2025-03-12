package com.example.proyecto_final;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;

public class GrafoTransporte {
    private final Graph<Parada, DefaultWeightedEdge> grafo;
    private final ObservableList<Parada> paradas;
    private final ObservableList<Ruta> rutas;
    private final Map<Ruta, DefaultWeightedEdge> edgeMap;

    public GrafoTransporte() {
        grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        paradas = FXCollections.observableArrayList();
        rutas = FXCollections.observableArrayList();
        edgeMap = new HashMap<>();
    }

    // CRUD Paradas
    public void agregarParada(Parada p) {
        if (!grafo.containsVertex(p)) {
            grafo.addVertex(p);
            paradas.add(p);
        }
    }

    public void eliminarParada(Parada p) {
        if (grafo.containsVertex(p)) {
            new ArrayList<>(rutas).forEach(r -> {
                if (r.getOrigen().equals(p) || r.getDestino().equals(p)) eliminarRuta(r);
            });
            grafo.removeVertex(p);
            paradas.remove(p);
        }
    }

    // CRUD Rutas
    public void agregarRuta(Ruta r) {
        DefaultWeightedEdge edge = grafo.addEdge(r.getOrigen(), r.getDestino());
        if (edge != null) {
            grafo.setEdgeWeight(edge, r.getTiempo()); // Usar tiempo como peso por defecto
            edgeMap.put(r, edge);
            rutas.add(r);
        }
    }

    public void eliminarRuta(Ruta r) {
        DefaultWeightedEdge edge = edgeMap.get(r);
        if (edge != null) {
            grafo.removeEdge(edge);
            edgeMap.remove(r);
            rutas.remove(r);
        }
    }

    // Dijkstra
    public Map<Parada, Double> dijkstra(Parada origen, String criterio) {
        // Validar criterio
        if (!Arrays.asList("tiempo", "distancia", "costo").contains(criterio)) {
            throw new IllegalArgumentException("Criterio inválido");
        }

        // Aplicar el criterio a todas las rutas temporalmente para la búsqueda
        for (Ruta r : rutas) {
            DefaultWeightedEdge edge = edgeMap.get(r);
            if (edge != null) {
                double peso = switch (criterio) {
                    case "tiempo" -> r.getTiempo();
                    case "distancia" -> r.getDistancia();
                    case "costo" -> r.getCosto();
                    default -> r.getTiempo(); // Default
                };
                grafo.setEdgeWeight(edge, peso);
            }
        }

        DijkstraShortestPath<Parada, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(grafo);
        Map<Parada, Double> resultados = new HashMap<>();

        grafo.vertexSet().forEach(p -> {
            if (!p.equals(origen)) {
                try {
                    double peso = dijkstra.getPath(origen, p).getWeight();
                    resultados.put(p, peso);
                } catch (Exception ignored) {}
            }
        });

        return resultados;
    }

    //Mis Getters
    public ObservableList<Parada> getParadas() { return paradas; }
    public ObservableList<Ruta> getRutas() { return rutas; }
}