package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Co_Proyecto_Controller implements Initializable{
    @FXML
    private TableView<Proyecto> tabla;

    @FXML
    private TableColumn<Proyecto, String> colNombreProyecto;
    @FXML
    private TableColumn<Proyecto, String> colNombreProfesor;
    @FXML
    private TableColumn<Proyecto, String> colTipo;
    @FXML
    private TableColumn<Proyecto, String> colFecha;
    @FXML
    private TableColumn<Proyecto, String> colEstado;
    @FXML
    private TableColumn<Proyecto, Proyecto> colDescargar;

    private CoordinadorController parent; // referencia al controlador padre para loadUI
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Columnas normales
        colNombreProyecto.setCellValueFactory(new PropertyValueFactory<>("nombreProyecto"));
        colNombreProfesor.setCellValueFactory(new PropertyValueFactory<>("nombreProfesor"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        // Columna Estado
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        configurarColumnaEstado();


        // Columna Descargar
        configurarColumnaDescargar();

        // Cargar datos de prueba
        cargarDatosPrueba();
    }
    
    public void setParentController(CoordinadorController parent) {
        this.parent = parent;
    }
    
    private void configurarColumnaEstado() {
        colEstado.setCellFactory(column -> new TableCell<Proyecto, String>() {
            private final Button estadoBtn = new Button();

            {
                // Evento al hacer clic en el botón
                estadoBtn.setOnAction(e -> {
                    String estadoActual = estadoBtn.getText();

                    if ("A evaluar".equalsIgnoreCase(estadoActual)) {
                        if (parent != null) {
                            parent.loadUI("Coordinador_Observaciones");
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Acción no permitida");
                        alert.setHeaderText(null);
                        alert.setContentText("Este proyecto ya fue evaluado. No se puede volver a evaluar.");
                        alert.showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                } else {
                    estadoBtn.setText(estado);
                    estadoBtn.getStyleClass().removeAll("estado-rojo", "estado-verde");

                    Image icon = null;
                    if ("A evaluar".equalsIgnoreCase(estado)) {
                        estadoBtn.getStyleClass().add("estado-rojo");
                        icon = new Image(getClass().getResourceAsStream(
                                "/co/unicauca/workflow/degree_project/images/ojo_abierto.png"));
                    } else if ("Evaluado".equalsIgnoreCase(estado)) {
                        estadoBtn.getStyleClass().add("estado-verde");
                        icon = new Image(getClass().getResourceAsStream(
                                "/co/unicauca/workflow/degree_project/images/ojo_cerrado.png"));
                    }

                    if (icon != null) {
                        ImageView iv = new ImageView(icon);
                        iv.setFitWidth(24);
                        iv.setFitHeight(22);
                        estadoBtn.setGraphic(iv);
                    } else {
                        estadoBtn.setGraphic(null);
                    }

                    setGraphic(estadoBtn);
                }
            }
        });
    }

    private void configurarColumnaDescargar() {
        // El valor de la celda será el objeto Proyecto completo
        colDescargar.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));

        colDescargar.setCellFactory(column -> new TableCell<Proyecto, Proyecto>() {
            @Override
            protected void updateItem(Proyecto proyecto, boolean empty) {
                super.updateItem(proyecto, empty);

                if (empty || proyecto == null) {
                    setGraphic(null);
                } else {
                    // Crear botón con imagen
                    Button btnDescargar = new Button();
                    btnDescargar.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                    ImageView imgView = new ImageView(
                        new Image(getClass().getResourceAsStream(
                            "/co/unicauca/workflow/degree_project/images/descargar.png"))
                    );
                    imgView.setFitWidth(20);
                    imgView.setFitHeight(20);
                    btnDescargar.setGraphic(imgView);

                    // Acción de clic
                    btnDescargar.setOnAction(event -> {
                        byte[] pdf = proyecto.getArchivoPdf();
                        if (pdf != null) {
                            try (FileOutputStream fos = new FileOutputStream(proyecto.getNombreProyecto() + ".pdf")) {
                                fos.write(pdf);

                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Descarga exitosa");
                                alert.setHeaderText(null);
                                alert.setContentText("El archivo '" + proyecto.getNombreProyecto() + ".pdf' fue descargado correctamente.");
                                alert.showAndWait();

                            } catch (IOException e) {
                                e.printStackTrace();
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error al descargar");
                                alert.setHeaderText("No se pudo guardar el archivo");
                                alert.setContentText("Ocurrió un error al intentar guardar el PDF.");
                                alert.showAndWait();
                            }
                        } else {
                            // ⚠ Alerta cuando no hay PDF
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Archivo no disponible");
                            alert.setHeaderText(null);
                            alert.setContentText("El proyecto '" + proyecto.getNombreProyecto() + "' no tiene un PDF asociado.");
                            alert.showAndWait();
                        }
                    });

                    setGraphic(btnDescargar);
                }
            }
        });
    }



    private void cargarDatosPrueba() {
        tabla.getItems().addAll(
                new Proyecto("Proyecto A", "Profesor 1", "Tesis", "2025-09-18", "A evaluar", null),
                new Proyecto("Proyecto B", "Profesor 2", "Investigación", "2025-09-17", "Evaluado", null)
        );
    }
}
