package com.example.proyecto_final;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jgrapht.graph.SimpleWeightedGraph;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class GrafoTransporte {
    private final Graph<Parada, DefaultWeightedEdge> grafo;
    private final ObservableList<Parada> paradas;
    private final ObservableList<Ruta> rutas;
    private final Map<Ruta, DefaultWeightedEdge> edgeMap;
    private final Map<String, List<Ruta>> rutasPorLinea; // mapa de líneas y sus rutas

    public GrafoTransporte() {
        grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        paradas = FXCollections.observableArrayList();
        rutas = FXCollections.observableArrayList();
        edgeMap = new HashMap<>();
        rutasPorLinea = new HashMap<>();
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

            // Añadir a la colección de líneas
            rutasPorLinea.computeIfAbsent(r.getLinea(), k -> new ArrayList<>()).add(r);
        }
    }

    public void eliminarRuta(Ruta r) {
        DefaultWeightedEdge edge = edgeMap.get(r);
        if (edge != null) {
            grafo.removeEdge(edge);
            edgeMap.remove(r);
            rutas.remove(r);

            // Eliminar de la colección de líneas
            if (rutasPorLinea.containsKey(r.getLinea())) {
                rutasPorLinea.get(r.getLinea()).remove(r);
                if (rutasPorLinea.get(r.getLinea()).isEmpty()) {
                    rutasPorLinea.remove(r.getLinea());
                }
            }
        }
    }

    // Metodo para cambiar pesos según criterio
    private void aplicarCriterio(String criterio) {
        if (!Arrays.asList("tiempo", "distancia", "costo", "transbordos").contains(criterio)) {
            throw new IllegalArgumentException("Criterio inválido");
        }

        if (criterio.equals("transbordos")) {
            // Para el criterio de transbordos, usamos un enfoque diferente
            aplicarCriterioTransbordos();
            return;
        }

        for (Ruta r : rutas) {
            DefaultWeightedEdge edge = edgeMap.get(r);
            if (edge != null) {
                double peso = switch (criterio) {
                    case "tiempo" -> r.getTiempo();
                    case "distancia" -> r.getDistancia();
                    case "costo" -> r.getCosto();
                    default -> r.getTiempo();
                };
                grafo.setEdgeWeight(edge, peso);
            }
        }
    }

    // Metodo especializado para aplicar pesos basados en transbordos
    private void aplicarCriterioTransbordos() {
        /*Creo una copia del grafo original pero con nuevos pesos
         Puse un peso bajo (0.1) para rutas dentro de la misma línea
         y un peso alto (10) para rutas que hacen cambio de línea*/

        String lineaActual = null;

        for (Ruta r : rutas) {
            DefaultWeightedEdge edge = edgeMap.get(r);
            if (edge != null) {
                // Peso base para cualquier segmento
                double peso = 1.0;

                // Si estamos cambiando de línea, aumentamos significativamente el peso
                if (lineaActual != null && !lineaActual.equals(r.getLinea())) {
                    peso = 10.0; // Penalización por transbordo
                }

                grafo.setEdgeWeight(edge, peso);
                lineaActual = r.getLinea();
            }
        }
    }

    // Dijkstra
    public Map<Parada, Double> dijkstra(Parada origen, String criterio) {
        aplicarCriterio(criterio);

        DijkstraShortestPath<Parada, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(grafo);
        Map<Parada, Double> resultados = new HashMap<>();

        grafo.vertexSet().forEach(p -> {
            if (!p.equals(origen)) {
                try {
                    double peso = dijkstra.getPath(origen, p).getWeight();
                    resultados.put(p, peso);
                } catch (Exception ignored) {} // no hay ruta, ignorar
            }
        });

        return resultados;
    }

    // Bellman-Ford - útil para cuando tengamos descuentos (pesos negativos)
    public Map<Parada, Double> bellmanFord(Parada origen, String criterio) {
        aplicarCriterio(criterio);

        BellmanFordShortestPath<Parada, DefaultWeightedEdge> bellmanFord =
                new BellmanFordShortestPath<>(grafo);
        Map<Parada, Double> resultados = new HashMap<>();

        grafo.vertexSet().forEach(p -> {
            if (!p.equals(origen)) {
                try {
                    double peso = bellmanFord.getPath(origen, p).getWeight();
                    resultados.put(p, peso);
                } catch (Exception ignored) {} // sin ruta
            }
        });

        return resultados;
    }

    // Floyd-Warshall - calcula todas las rutas de una vez
    public Map<Parada, Map<Parada, Double>> floydWarshall(String criterio) {
        aplicarCriterio(criterio);

        FloydWarshallShortestPaths<Parada, DefaultWeightedEdge> floydWarshall =
                new FloydWarshallShortestPaths<>(grafo);
        Map<Parada, Map<Parada, Double>> resultados = new HashMap<>();

        // Para todos los pares de paradas
        for (Parada origen : paradas) {
            Map<Parada, Double> distanciasDesdeOrigen = new HashMap<>();

            for (Parada destino : paradas) {
                if (!origen.equals(destino)) {
                    try {
                        double distancia = floydWarshall.getPath(origen, destino).getWeight();
                        distanciasDesdeOrigen.put(destino, distancia);
                    } catch (Exception ignored) {
                        // nada que hacer, no hay camino
                    }
                }
            }

            resultados.put(origen, distanciasDesdeOrigen);
        }

        return resultados;
    }

    // Kruskal - para optimizar la red (minimizar infraestructura)
    public Set<DefaultWeightedEdge> kruskalMST(String criterio) {
        aplicarCriterio(criterio);

        // Para MST aqui se convierte a no dirigido
        AsUndirectedGraph<Parada, DefaultWeightedEdge> grafoNoDir =
                new AsUndirectedGraph<>(grafo);

        KruskalMinimumSpanningTree<Parada, DefaultWeightedEdge> kruskal =
                new KruskalMinimumSpanningTree<>(grafoNoDir);

        return kruskal.getSpanningTree().getEdges();
    }

    // Prim - alternativa a Kruskal, a veces más rápido
    public Set<DefaultWeightedEdge> primMST(String criterio) {
        aplicarCriterio(criterio);

        AsUndirectedGraph<Parada, DefaultWeightedEdge> grafoNoDir =
                new AsUndirectedGraph<>(grafo);

        PrimMinimumSpanningTree<Parada, DefaultWeightedEdge> prim =
                new PrimMinimumSpanningTree<>(grafoNoDir);

        return prim.getSpanningTree().getEdges();
    }

    // Obtener la secuencia de paradas, no solo el costo
    public List<Parada> obtenerRutaCompleta(Parada origen, Parada destino, String criterio) {
        aplicarCriterio(criterio);

        DijkstraShortestPath<Parada, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(grafo);

        try {
            List<Parada> ruta = dijkstra.getPath(origen, destino).getVertexList();
            return ruta;
        } catch (Exception e) {
            return null; // no hay ruta
        }
    }

    // Rutas alternativas - para dar opciones al usuario
    public List<List<Parada>> obtenerRutasAlternativas(Parada origen, Parada destino,
                                                       String criterio, int maxAlternativas) {
        List<List<Parada>> rutasAlternativas = new ArrayList<>();

        // Primero la ruta principal
        List<Parada> rutaPrincipal = obtenerRutaCompleta(origen, destino, criterio);

        if (rutaPrincipal != null) {
            rutasAlternativas.add(rutaPrincipal);

            // Intentamos con otros criterios para dar alternativas
            String[] criteriosAlternativos = {"tiempo", "distancia", "costo", "transbordos"};

            for (String criterioAlt : criteriosAlternativos) {
                if (!criterioAlt.equals(criterio)) {
                    List<Parada> rutaAlternativa = obtenerRutaCompleta(origen, destino, criterioAlt);

                    if (rutaAlternativa != null && !rutasAlternativas.contains(rutaAlternativa)) {
                        rutasAlternativas.add(rutaAlternativa);

                        if (rutasAlternativas.size() >= maxAlternativas + 1) {
                            break; // suficientes alternativas
                        }
                    }
                }
            }
        }

        return rutasAlternativas;
    }

    // Calcular transbordos básico - basado en número de paradas
    public int calcularTransbordos(List<Parada> ruta) {
        if (ruta == null || ruta.size() <= 1) {
            return 0;
        }
        return ruta.size() - 1; // cada parada nueva = un transbordo potencial
    }

    // Calcular transbordos real - considerando cambios de línea
    public int calcularTransbordosReales(List<Parada> ruta) {
        if (ruta == null || ruta.size() <= 2) return 0;

        int transbordos = 0;
        String lineaActual = null;

        for (int i = 0; i < ruta.size() - 1; i++) {
            Parada actual = ruta.get(i);
            Parada siguiente = ruta.get(i + 1);

            // Buscar la ruta entre estas dos paradas
            for (Ruta r : rutas) {
                if (r.getOrigen().equals(actual) && r.getDestino().equals(siguiente)) {
                    // Si es nuestra primera ruta, simplemente establecemos la línea actual
                    if (lineaActual == null) {
                        lineaActual = r.getLinea();
                    }
                    // Si cambiamos de línea, contamos un transbordo
                    else if (!lineaActual.equals(r.getLinea())) {
                        transbordos++;
                        lineaActual = r.getLinea();
                    }
                    break;
                }
            }
        }

        return transbordos;
    }

    // Obtener las líneas que pasan por una parada
    public List<String> getLineasEnParada(Parada parada) {
        Set<String> lineas = new HashSet<>();

        for (Ruta r : rutas) {
            if (r.getOrigen().equals(parada) || r.getDestino().equals(parada)) {
                lineas.add(r.getLinea());
            }
        }

        return new ArrayList<>(lineas);
    }

    // Obtener las líneas utilizadas en una ruta
    public List<String> getLineasEnRuta(List<Parada> ruta) {
        if (ruta == null || ruta.size() <= 1) return new ArrayList<>();

        List<String> lineasUsadas = new ArrayList<>();
        String lineaActual = null;

        for (int i = 0; i < ruta.size() - 1; i++) {
            Parada actual = ruta.get(i);
            Parada siguiente = ruta.get(i + 1);

            for (Ruta r : rutas) {
                if (r.getOrigen().equals(actual) && r.getDestino().equals(siguiente)) {
                    if (lineaActual == null || !lineaActual.equals(r.getLinea())) {
                        lineaActual = r.getLinea();
                        lineasUsadas.add(lineaActual);
                    }
                    break;
                }
            }
        }

        return lineasUsadas;
    }

    // Obtener todas las líneas disponibles
    public List<String> getTodasLasLineas() {
        return new ArrayList<>(rutasPorLinea.keySet());
    }

    // Getters
    public ObservableList<Parada> getParadas() { return paradas; }
    public ObservableList<Ruta> getRutas() { return rutas; }

    // Obtener rutas por línea
    public List<Ruta> getRutasPorLinea(String linea) {
        return rutasPorLinea.getOrDefault(linea, new ArrayList<>());
    }
    public Map<Ruta, DefaultWeightedEdge> getEdgeMap() {
        return edgeMap;
    }

    public void guardarRutasEnArchivo(String archivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(archivo))) {
            for (Ruta r : rutas) {
                writer.printf("%s;%s;%.2f;%.2f;%.2f;%s%n",
                        r.getOrigen().getId(),
                        r.getDestino().getId(),
                        r.getTiempo(),
                        r.getDistancia(),
                        r.getCosto(),
                        r.getLinea());
            }
        }
    }

    public void cargarParadasDesdeArchivo(String archivo) throws IOException {
        for (Parada p : new ArrayList<>(grafo.vertexSet())) {
            grafo.removeVertex(p);
        }
        paradas.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(";");
                if (partes.length >= 4) {
                    Parada p = new Parada(partes[0], partes[1],
                            Double.parseDouble(partes[2]),
                            Double.parseDouble(partes[3]));
                    agregarParada(p);
                }
            }
        }
    }

    public void cargarRutasDesdeArchivo(String archivo) throws IOException {
        for (DefaultWeightedEdge edge : new ArrayList<>(grafo.edgeSet())) {
            grafo.removeEdge(edge);
        }
        rutas.clear();
        edgeMap.clear();
        rutasPorLinea.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(";");
                if (partes.length >= 6) {
                    Parada origen = buscarParadaPorId(partes[0]);
                    Parada destino = buscarParadaPorId(partes[1]);
                    if (origen != null && destino != null) {
                        Ruta r = new Ruta(origen, destino,
                                Double.parseDouble(partes[2]),
                                Double.parseDouble(partes[3]),
                                Double.parseDouble(partes[4]),
                                partes[5]);
                        agregarRuta(r);
                    }
                }
            }
        }
    }
    private Parada buscarParadaPorId(String id) {
        for (Parada p : paradas) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }
}