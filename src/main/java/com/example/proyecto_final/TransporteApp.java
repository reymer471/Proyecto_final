package com.example.proyecto_final;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TransporteApp extends Application {
    private final GrafoTransporte grafo = new GrafoTransporte();
    private final Pane mapa = new Pane();

    // Guarda las líneas y círculos de la ruta actual para poder eliminarlos después
    private List<Line> lineasRutaActual = new java.util.ArrayList<>();
    private List<Circle> circulosRutaActual = new java.util.ArrayList<>();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setLeft(crearControles());
        root.setCenter(mapa);

        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Sistema de Transporte Inteligente");
        stage.show();
    }

    private VBox crearControles() {
        VBox vbox = new VBox(10);
        vbox.setPrefWidth(300);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(1000);


        // --- AGREGAR PARADA ---
        TextField txtNombre = new TextField();
        TextField txtX = new TextField();
        TextField txtY = new TextField();
        Button btnAgregarParada = new Button("Agregar Parada");
        btnAgregarParada.setOnAction(e -> agregarParada(txtNombre, txtX, txtY));

        // --- MODIFICAR PARADA ---
        ComboBox<Parada> cbModificarParada = new ComboBox<>();
        TextField txtNuevoNombre = new TextField();
        Button btnModificarParada = new Button("Modificar Parada");
        btnModificarParada.setOnAction(e -> modificarParada(cbModificarParada, txtNuevoNombre));

        // --- ELIMINAR PARADA ---
        ComboBox<Parada> cbEliminarParada = new ComboBox<>();
        Button btnEliminarParada = new Button("Eliminar Parada");
        btnEliminarParada.setOnAction(e -> eliminarParada(cbEliminarParada));

        // --- AGREGAR RUTA ---
        ComboBox<Parada> cbOrigen = new ComboBox<>();
        ComboBox<Parada> cbDestino = new ComboBox<>();
        TextField txtTiempo = new TextField();
        TextField txtDistancia = new TextField(); // Campo para introducir la distancia manualmente
        TextField txtCosto = new TextField();
        TextField txtLinea = new TextField(); // Campo para introducir la línea

        // Botón para calcular distancia automáticamente
        Button btnCalcularDistancia = new Button("Calcular Distancia");
        btnCalcularDistancia.setOnAction(e -> {
            try {
                if (cbOrigen.getValue() != null && cbDestino.getValue() != null) {
                    double distancia = cbOrigen.getValue().calcularDistancia(cbDestino.getValue());
                    txtDistancia.setText(String.format("%.2f", distancia));
                } else {
                    mostrarError("Seleccione origen y destino para calcular distancia");
                }
            } catch (Exception ex) {
                mostrarError("Error al calcular distancia: " + ex.getMessage());
            }
        });

        Button btnAgregarRuta = new Button("Agregar Ruta");
        btnAgregarRuta.setOnAction(e -> agregarRuta(cbOrigen, cbDestino, txtTiempo, txtDistancia, txtCosto, txtLinea));

        // --- MODIFICAR RUTA ---
        ComboBox<Ruta> cbModificarRuta = new ComboBox<>();
        TextField txtNuevoTiempo = new TextField();
        TextField txtNuevaDistancia = new TextField(); // Campo para modificar distancia
        TextField txtNuevoCosto = new TextField();
        TextField txtNuevaLinea = new TextField(); // Campo para modificar línea
        Button btnModificarRuta = new Button("Modificar Ruta");
        btnModificarRuta.setOnAction(e -> modificarRuta(cbModificarRuta, txtNuevoTiempo, txtNuevaDistancia, txtNuevoCosto, txtNuevaLinea));

        // --- ELIMINAR RUTA ---
        ComboBox<Ruta> cbEliminarRuta = new ComboBox<>();
        Button btnEliminarRuta = new Button("Eliminar Ruta");
        btnEliminarRuta.setOnAction(e -> eliminarRuta(cbEliminarRuta));

        // --- CALCULAR RUTA ---
        ComboBox<Parada> cbRutaOrigen = new ComboBox<>();
        ComboBox<Parada> cbRutaDestino = new ComboBox<>();
        ComboBox<String> cbRutaCriterio = new ComboBox<>(FXCollections.observableArrayList("tiempo", "distancia", "costo", "transbordos"));
        cbRutaCriterio.setValue("tiempo");
        Button btnCalcularRuta = new Button("Calcular Ruta Óptima");
        btnCalcularRuta.setOnAction(e -> calcularRutaOptima(cbRutaOrigen, cbRutaDestino, cbRutaCriterio));

        // --- MOSTRAR RUTAS ALTERNATIVAS ---
        Button btnRutasAlternativas = new Button("Mostrar Rutas Alternativas");
        btnRutasAlternativas.setOnAction(e -> mostrarRutasAlternativas(cbRutaOrigen, cbRutaDestino, cbRutaCriterio));

        // Listeners
        grafo.getParadas().addListener((ListChangeListener<Parada>) c -> {
            //c.getList()
            cbOrigen.getItems().setAll(grafo.getParadas());
            cbDestino.getItems().setAll(grafo.getParadas());
            cbEliminarParada.getItems().setAll(grafo.getParadas());
            cbModificarParada.getItems().setAll(grafo.getParadas());
            cbRutaOrigen.getItems().setAll(grafo.getParadas());
            cbRutaDestino.getItems().setAll(grafo.getParadas());
        });

        grafo.getRutas().addListener((ListChangeListener<Ruta>) c -> {
            cbModificarRuta.getItems().setAll(grafo.getRutas());
            cbEliminarRuta.getItems().setAll(grafo.getRutas());
        });


        // Ensamblar UI
        vbox.getChildren().addAll(
                new Label("--- AGREGAR PARADA ---"),
                new Label("Nombre:"), txtNombre,
                new Label("Coordenada X:"), txtX,
                new Label("Coordenada Y:"), txtY,
                btnAgregarParada,
                new Separator(),
                new Label("--- MODIFICAR PARADA ---"),
                cbModificarParada,
                new Label("Nuevo Nombre:"), txtNuevoNombre,
                btnModificarParada,
                new Separator(),
                new Label("--- ELIMINAR PARADA ---"),
                cbEliminarParada, btnEliminarParada,
                new Separator(),
                new Label("--- AGREGAR RUTA ---"),
                new Label("Origen:"), cbOrigen,
                new Label("Destino:"), cbDestino,
                new Label("Tiempo (min):"), txtTiempo,
                new Label("Distancia (km):"), txtDistancia, // Etiqueta para el campo de distancia
                btnCalcularDistancia, // Botón para calcular distancia automáticamente
                new Label("Costo:"), txtCosto,
                new Label("Línea:"), txtLinea, // Campo para la línea
                btnAgregarRuta,
                new Separator(),
                new Label("--- MODIFICAR RUTA ---"),
                cbModificarRuta,
                new Label("Nuevo Tiempo:"), txtNuevoTiempo,
                new Label("Nueva Distancia:"), txtNuevaDistancia, // Campo para modificar distancia
                new Label("Nuevo Costo:"), txtNuevoCosto,
                new Label("Nueva Línea:"), txtNuevaLinea, // Campo para modificar línea
                btnModificarRuta,
                new Separator(),
                new Label("--- ELIMINAR RUTA ---"),
                cbEliminarRuta, btnEliminarRuta,
                new Separator(),
                new Label("--- CALCULAR RUTA ÓPTIMA ---"),
                new Label("Origen:"), cbRutaOrigen,
                new Label("Destino:"), cbRutaDestino,
                new Label("Criterio:"), cbRutaCriterio,
                btnCalcularRuta,
                btnRutasAlternativas
        );

        return new VBox(scrollPane);
    }

    // Métodos de Acción
    private void agregarParada(TextField... campos) {
        try {
            Parada p = new Parada(
                    UUID.randomUUID().toString(),
                    campos[0].getText(),
                    Double.parseDouble(campos[1].getText()),
                    Double.parseDouble(campos[2].getText())
            );
            grafo.agregarParada(p);

            actualizarMapa();
            limpiarCampos(campos);
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void modificarParada(ComboBox<Parada> cb, TextField txtNuevoNombre) {
        try {
            Parada p = cb.getValue();
            if (p == null) throw new IllegalArgumentException("Seleccione una parada");
            p.setNombre(txtNuevoNombre.getText());
            actualizarMapa();
            txtNuevoNombre.clear();
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void eliminarParada(ComboBox<Parada> cb) {
        Parada p = cb.getValue();
        if (p != null) {
            grafo.eliminarParada(p);
            actualizarMapa();
        } else {
            mostrarError("Seleccione una parada");
        }
    }

    private void agregarRuta(ComboBox<Parada> origen, ComboBox<Parada> destino,
                             TextField tiempo, TextField distancia, TextField costo, TextField linea) {
        try {
            if (origen.getValue() == null || destino.getValue() == null)
                throw new IllegalArgumentException("Seleccione origen y destino");
            if (origen.getValue().equals(destino.getValue()))
                throw new IllegalArgumentException("Origen y destino deben ser diferentes");

            Ruta r = new Ruta(
                    origen.getValue(),
                    destino.getValue(),
                    Double.parseDouble(tiempo.getText()),
                    Double.parseDouble(distancia.getText()),
                    Double.parseDouble(costo.getText()),
                    linea.getText() // Usar la línea especificada
            );
            grafo.agregarRuta(r);
            actualizarMapa();
            tiempo.clear();
            distancia.clear();
            costo.clear();
            linea.clear();
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void modificarRuta(ComboBox<Ruta> cb, TextField txtNuevoTiempo,
                               TextField txtNuevaDistancia, TextField txtNuevoCosto, TextField txtNuevaLinea) {
        try {
            Ruta r = cb.getValue();
            if (r == null) throw new IllegalArgumentException("Seleccione una ruta");
            r.setTiempo(Double.parseDouble(txtNuevoTiempo.getText()));
            r.setDistancia(Double.parseDouble(txtNuevaDistancia.getText()));
            r.setCosto(Double.parseDouble(txtNuevoCosto.getText()));

            if (txtNuevaLinea.getText() != null && !txtNuevaLinea.getText().isEmpty()) {
                r.setLinea(txtNuevaLinea.getText()); // Actualizar línea
            }

            actualizarMapa();
            txtNuevoTiempo.clear();
            txtNuevaDistancia.clear();
            txtNuevoCosto.clear();
            txtNuevaLinea.clear();
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void eliminarRuta(ComboBox<Ruta> cb) {
        Ruta r = cb.getValue();
        if (r != null) {
            grafo.eliminarRuta(r);
            actualizarMapa();
        } else {
            mostrarError("Seleccione una ruta");
        }
    }

    private void calcularRutaOptima(ComboBox<Parada> origen, ComboBox<Parada> destino, ComboBox<String> criterio) {
        try {
            Parada o = origen.getValue();
            Parada d = destino.getValue();
            String c = criterio.getValue();

            if (o == null || d == null || c == null)
                throw new IllegalArgumentException("Seleccione origen, destino y criterio");

            if (o.equals(d))
                throw new IllegalArgumentException("El origen y destino no pueden ser iguales");

            // Obtener todas las rutas óptimas desde el origen
            Map<Parada, Double> resultados = grafo.dijkstra(o, c);

            // Verificar si hay ruta al destino seleccionado
            if (!resultados.containsKey(d)) {
                mostrarError("No hay ruta disponible desde " + o.getNombre() + " hasta " + d.getNombre());
                return;
            }

            // Mostrar resultado específico para el destino seleccionado
            double costo = resultados.get(d);
            String unidad = switch (c) {
                case "tiempo" -> "minutos";
                case "distancia" -> "km";
                case "costo" -> "$";
                case "transbordos" -> "transbordos";
                default -> "";
            };

            // Obtener la ruta completa (secuencia de paradas)
            List<Parada> rutaCompleta = grafo.obtenerRutaCompleta(o, d, c);

            // Calcular transbordos reales
            int transbordosReales = grafo.calcularTransbordosReales(rutaCompleta);

            // Crear el mensaje para mostrar al usuario
            StringBuilder mensaje = new StringBuilder();
            mensaje.append(String.format("Criterio: %s\nValor: %.2f %s\n",
                    c.toUpperCase(), costo, unidad));

            // Añadir información de transbordos
            if (!c.equals("transbordos")) {
                mensaje.append(String.format("Transbordos: %d\n\n", transbordosReales));
            }

            // Añadir información de líneas
            List<String> lineasUtilizadas = grafo.getLineasEnRuta(rutaCompleta);
            if (!lineasUtilizadas.isEmpty()) {
                mensaje.append("Líneas utilizadas: ");
                for (int i = 0; i < lineasUtilizadas.size(); i++) {
                    mensaje.append(lineasUtilizadas.get(i));
                    if (i < lineasUtilizadas.size() - 1) {
                        mensaje.append(", ");
                    }
                }
                mensaje.append("\n\n");
            }

            mensaje.append("Ruta completa:\n");
            if (rutaCompleta != null) {
                for (int i = 0; i < rutaCompleta.size(); i++) {
                    mensaje.append(rutaCompleta.get(i).getNombre());
                    if (i < rutaCompleta.size() - 1) {
                        mensaje.append(" → ");
                    }
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ruta Óptima");
            alert.setHeaderText("Ruta de " + o.getNombre() + " a " + d.getNombre());
            alert.setContentText(mensaje.toString());
            alert.show();

            // Resaltar visualmente la ruta en el mapa
            resaltarRuta(rutaCompleta);

        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void mostrarRutasAlternativas(ComboBox<Parada> origen, ComboBox<Parada> destino, ComboBox<String> criterio) {
        try {
            Parada o = origen.getValue();
            Parada d = destino.getValue();
            String c = criterio.getValue();

            if (o == null || d == null || c == null)
                throw new IllegalArgumentException("Seleccione origen, destino y criterio");

            // Obtener rutas alternativas (máximo 3)
            List<List<Parada>> alternativas = grafo.obtenerRutasAlternativas(o, d, c, 3);

            if (alternativas.isEmpty()) {
                mostrarError("No se encontraron rutas entre " + o.getNombre() + " y " + d.getNombre());
                return;
            }

            // Crear el mensaje para mostrar al usuario
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Se encontraron ").append(alternativas.size())
                    .append(" ruta(s) posibles:\n\n");

            for (int i = 0; i < alternativas.size(); i++) {
                List<Parada> ruta = alternativas.get(i);

                // Calcular valores para esta ruta
                double tiempoTotal = calcularValorRuta(ruta, "tiempo");
                double distanciaTotal = calcularValorRuta(ruta, "distancia");
                double costoTotal = calcularValorRuta(ruta, "costo");
                int transbordos = grafo.calcularTransbordosReales(ruta);

                mensaje.append("Ruta ").append(i + 1).append(":\n");
                mensaje.append(String.format("• Tiempo: %.2f min\n", tiempoTotal));
                mensaje.append(String.format("• Distancia: %.2f km\n", distanciaTotal));
                mensaje.append(String.format("• Costo: $%.2f\n", costoTotal));
                mensaje.append(String.format("• Transbordos: %d\n", transbordos));

                // Añadir información de líneas
                List<String> lineasUtilizadas = grafo.getLineasEnRuta(ruta);
                if (!lineasUtilizadas.isEmpty()) {
                    mensaje.append("• Líneas: ");
                    for (int j = 0; j < lineasUtilizadas.size(); j++) {
                        mensaje.append(lineasUtilizadas.get(j));
                        if (j < lineasUtilizadas.size() - 1) {
                            mensaje.append(", ");
                        }
                    }
                    mensaje.append("\n");
                }

                mensaje.append("• Secuencia: ");
                for (int j = 0; j < ruta.size(); j++) {
                    mensaje.append(ruta.get(j).getNombre());
                    if (j < ruta.size() - 1) {
                        mensaje.append(" → ");
                    }
                }

                if (i < alternativas.size() - 1) {
                    mensaje.append("\n\n");
                }
            }

            // Mostrar diálogo con las rutas alternativas
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rutas Alternativas");
            alert.setHeaderText("De " + o.getNombre() + " a " + d.getNombre());

            // Para textos largos, usamos TextArea en lugar de simple texto
            TextArea textArea = new TextArea(mensaje.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefHeight(300);

            alert.getDialogPane().setContent(textArea);
            alert.show();

            // Resaltar la primera ruta alternativa
            if (!alternativas.isEmpty()) {
                resaltarRuta(alternativas.get(0));
            }

        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    // Método auxiliar para calcular el valor total de una ruta según un criterio
    private double calcularValorRuta(List<Parada> ruta, String criterio) {
        if (ruta == null || ruta.size() < 2) return 0;

        double total = 0;
        for (int i = 0; i < ruta.size() - 1; i++) {
            Parada actual = ruta.get(i);
            Parada siguiente = ruta.get(i + 1);

            // Buscar la ruta entre estas paradas
            for (Ruta r : grafo.getRutas()) {
                if (r.getOrigen().equals(actual) && r.getDestino().equals(siguiente)) {
                    switch (criterio) {
                        case "tiempo" -> total += r.getTiempo();
                        case "distancia" -> total += r.getDistancia();
                        case "costo" -> total += r.getCosto();
                    }
                    break;
                }
            }
        }

        return total;
    }

    // Método mejorado para resaltar visualmente la ruta en el mapa
    private void resaltarRuta(List<Parada> ruta) {
        // Primero actualizamos el mapa normal para tener todas las rutas
        actualizarMapa();

        // Si no hay ruta válida, no hacemos nada más
        if (ruta == null || ruta.size() < 2) return;

        // Limpiamos cualquier ruta resaltada anterior
        for (Line l : lineasRutaActual) {
            mapa.getChildren().remove(l);
        }
        for (Circle c : circulosRutaActual) {
            mapa.getChildren().remove(c);
        }

        lineasRutaActual.clear();
        circulosRutaActual.clear();

        // Dibujamos la nueva ruta resaltada
        for (int i = 0; i < ruta.size() - 1; i++) {
            Parada actual = ruta.get(i);
            Parada siguiente = ruta.get(i + 1);

            // Buscar la línea de esta ruta para colorear según la línea
            String lineaActual = null;
            for (Ruta r : grafo.getRutas()) {
                if (r.getOrigen().equals(actual) && r.getDestino().equals(siguiente)) {
                    lineaActual = r.getLinea();
                    break;
                }
            }

            // Seleccionar color según la línea (colores distintivos para diferentes líneas)
            Color colorLinea = Color.RED; // color por defecto
            if (lineaActual != null) {
                // Seleccionar color según la línea
                switch (lineaActual) {
                    case "L1" -> colorLinea = Color.RED;
                    case "L2" -> colorLinea = Color.BLUE;
                    case "L3" -> colorLinea = Color.GREEN;
                    case "L4" -> colorLinea = Color.PURPLE;
                    case "L5" -> colorLinea = Color.ORANGE;
                    default -> colorLinea = Color.GRAY;
                }
            }

            // Dibujar línea resaltada con el color de la línea
            Line linea = new Line(
                    actual.getX(), actual.getY(),
                    siguiente.getX(), siguiente.getY()
            );
            linea.setStroke(colorLinea);
            linea.setStrokeWidth(3); // línea más gruesa
            linea.getStrokeDashArray().addAll(10.0, 5.0); // línea punteada
            mapa.getChildren().add(linea);
            lineasRutaActual.add(linea);

            // Destacar las paradas en la ruta
            if (i == 0) { // Origen
                Circle circulo = new Circle(actual.getX(), actual.getY(), 10, Color.ORANGE);
                mapa.getChildren().add(circulo);
                circulosRutaActual.add(circulo);
            }

            // Última parada (destino)
            if (i == ruta.size() - 2) {
                Circle circulo = new Circle(siguiente.getX(), siguiente.getY(), 10, Color.RED);
                mapa.getChildren().add(circulo);
                circulosRutaActual.add(circulo);
            }
            // Paradas intermedias
            else if (i > 0) {
                Circle circulo = new Circle(actual.getX(), actual.getY(), 10, Color.YELLOW);
                mapa.getChildren().add(circulo);
                circulosRutaActual.add(circulo);
            }
        }

        // Mostrar la información de la ruta completa en la parte inferior del mapa
        StringBuilder info = new StringBuilder("Ruta: ");
        for (int i = 0; i < ruta.size(); i++) {
            info.append(ruta.get(i).getNombre());
            if (i < ruta.size() - 1) {
                info.append(" → ");
            }
        }

        Text textoRuta = new Text(20, mapa.getHeight() - 20, info.toString());
        textoRuta.setFill(Color.RED);
        mapa.getChildren().add(textoRuta);
    }

    // Herramientas
    private void actualizarMapa() {
        mapa.getChildren().clear();
        lineasRutaActual.clear();
        circulosRutaActual.clear();

        // Dibujar rutas
        grafo.getRutas().forEach(r -> {
            Line linea = new Line(
                    r.getOrigen().getX(), r.getOrigen().getY(),
                    r.getDestino().getX(), r.getDestino().getY()
            );

            // Usar diferentes colores según la línea
            switch (r.getLinea()) {
                case "L1" -> linea.setStroke(Color.RED);
                case "L2" -> linea.setStroke(Color.BLUE);
                case "L3" -> linea.setStroke(Color.GREEN);
                case "L4" -> linea.setStroke(Color.PURPLE);
                case "L5" -> linea.setStroke(Color.ORANGE);
                default -> linea.setStroke(Color.GRAY);
            }

            mapa.getChildren().add(linea);

            // Mostrar información de la ruta
            Text texto = new Text(
                    (r.getOrigen().getX() + r.getDestino().getX()) / 2,
                    (r.getOrigen().getY() + r.getDestino().getY()) / 2,
                    String.format("T: %.1f min\nD: %.1f km\nC: $%.1f\nL: %s",
                            r.getTiempo(), r.getDistancia(), r.getCosto(), r.getLinea())
            );
            texto.setFill(Color.BLACK);
            mapa.getChildren().add(texto);
        });

        // Dibujar paradas
        grafo.getParadas().forEach(p -> {
            Circle punto = new Circle(p.getX(), p.getY(), 8, Color.GREEN);
            Label etiqueta = new Label(p.getNombre());
            etiqueta.setLayoutX(p.getX() + 10);
            etiqueta.setLayoutY(p.getY() - 15);
            etiqueta.setTextFill(Color.DARKBLUE);
            mapa.getChildren().addAll(punto, etiqueta);
        });
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).show();
    }

    private void limpiarCampos(TextField... campos) {
        for (TextField campo : campos) campo.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}