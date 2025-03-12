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
import java.util.Map;
import java.util.UUID;

public class TransporteApp extends Application {
    private final GrafoTransporte grafo = new GrafoTransporte();
    private final Pane mapa = new Pane();

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
        btnAgregarRuta.setOnAction(e -> agregarRuta(cbOrigen, cbDestino, txtTiempo, txtDistancia, txtCosto));

        // --- MODIFICAR RUTA ---
        ComboBox<Ruta> cbModificarRuta = new ComboBox<>();
        TextField txtNuevoTiempo = new TextField();
        TextField txtNuevaDistancia = new TextField(); // Campo para modificar distancia
        TextField txtNuevoCosto = new TextField();
        Button btnModificarRuta = new Button("Modificar Ruta");
        btnModificarRuta.setOnAction(e -> modificarRuta(cbModificarRuta, txtNuevoTiempo, txtNuevaDistancia, txtNuevoCosto));

        // --- ELIMINAR RUTA ---
        ComboBox<Ruta> cbEliminarRuta = new ComboBox<>();
        Button btnEliminarRuta = new Button("Eliminar Ruta");
        btnEliminarRuta.setOnAction(e -> eliminarRuta(cbEliminarRuta));

        // --- CALCULAR RUTA ---
        ComboBox<Parada> cbRutaOrigen = new ComboBox<>();
        ComboBox<Parada> cbRutaDestino = new ComboBox<>();
        ComboBox<String> cbRutaCriterio = new ComboBox<>(FXCollections.observableArrayList("tiempo", "distancia", "costo"));
        cbRutaCriterio.setValue("tiempo");
        Button btnCalcularRuta = new Button("Calcular Ruta Óptima");
        btnCalcularRuta.setOnAction(e -> calcularRutaOptima(cbRutaOrigen, cbRutaDestino, cbRutaCriterio));

        // Listeners
        grafo.getParadas().addListener((ListChangeListener<Parada>) c -> {
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
                btnAgregarRuta,
                new Separator(),
                new Label("--- MODIFICAR RUTA ---"),
                cbModificarRuta,
                new Label("Nuevo Tiempo:"), txtNuevoTiempo,
                new Label("Nueva Distancia:"), txtNuevaDistancia, // Campo para modificar distancia
                new Label("Nuevo Costo:"), txtNuevoCosto,
                btnModificarRuta,
                new Separator(),
                new Label("--- ELIMINAR RUTA ---"),
                cbEliminarRuta, btnEliminarRuta,
                new Separator(),
                new Label("--- CALCULAR RUTA ÓPTIMA ---"),
                new Label("Origen:"), cbRutaOrigen,
                new Label("Destino:"), cbRutaDestino,
                new Label("Criterio:"), cbRutaCriterio,
                btnCalcularRuta
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

    private void agregarRuta(ComboBox<Parada> origen, ComboBox<Parada> destino, TextField tiempo, TextField distancia, TextField costo) {
        try {
            if (origen.getValue() == null || destino.getValue() == null)
                throw new IllegalArgumentException("Seleccione origen y destino");
            if (origen.getValue().equals(destino.getValue()))
                throw new IllegalArgumentException("Origen y destino deben ser diferentes");

            Ruta r = new Ruta(
                    origen.getValue(),
                    destino.getValue(),
                    Double.parseDouble(tiempo.getText()),
                    Double.parseDouble(distancia.getText()), // Usar el valor introducido por el usuario
                    Double.parseDouble(costo.getText())
            );
            grafo.agregarRuta(r);
            actualizarMapa();
            tiempo.clear();
            distancia.clear();
            costo.clear();
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void modificarRuta(ComboBox<Ruta> cb, TextField txtNuevoTiempo, TextField txtNuevaDistancia, TextField txtNuevoCosto) {
        try {
            Ruta r = cb.getValue();
            if (r == null) throw new IllegalArgumentException("Seleccione una ruta");
            r.setTiempo(Double.parseDouble(txtNuevoTiempo.getText()));
            r.setDistancia(Double.parseDouble(txtNuevaDistancia.getText())); // Actualizar distancia
            r.setCosto(Double.parseDouble(txtNuevoCosto.getText()));
            actualizarMapa();
            txtNuevoTiempo.clear();
            txtNuevaDistancia.clear();
            txtNuevoCosto.clear();
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
                default -> "";
            };

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ruta Óptima");
            alert.setHeaderText("Ruta de " + o.getNombre() + " a " + d.getNombre());
            String mensaje = String.format("Criterio: %s\nValor: %.2f %s",
                    c.toUpperCase(), costo, unidad);
            alert.setContentText(mensaje);
            alert.show();

            // Opcional: Destacar visualmente la ruta en el mapa
            resaltarRuta(o, d);

        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void resaltarRuta(Parada origen, Parada destino) {

        actualizarMapa();
    }

    // Herramientas
    private void actualizarMapa() {
        mapa.getChildren().clear();

        // Dibujar rutas
        grafo.getRutas().forEach(r -> {
            Line linea = new Line(
                    r.getOrigen().getX(), r.getOrigen().getY(),
                    r.getDestino().getX(), r.getDestino().getY()
            );
            linea.setStroke(Color.BLUE); // Color predeterminado para todas las rutas
            mapa.getChildren().add(linea);

            // Mostrar información de la ruta
            Text texto = new Text(
                    (r.getOrigen().getX() + r.getDestino().getX()) / 2,
                    (r.getOrigen().getY() + r.getDestino().getY()) / 2,
                    String.format("T: %.1f min\nD: %.1f km\nC: $%.1f",
                            r.getTiempo(), r.getDistancia(), r.getCosto())
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