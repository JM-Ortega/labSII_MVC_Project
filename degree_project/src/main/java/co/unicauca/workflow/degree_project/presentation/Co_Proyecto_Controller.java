package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.models.Archivo;
import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.services.IProyectoService;
import co.unicauca.workflow.degree_project.presentation.FormatoADocenteController.RowVM;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private TableView<RowVM> tabla;
    @FXML
    private TableColumn<RowVM, String> colNombreProyecto;
    @FXML
    private TableColumn<RowVM, String> colNombreProfesor;
    @FXML
    private TableColumn<RowVM, String> colTipo;
    @FXML
    private TableColumn<RowVM, String> colFecha;
    @FXML
    private TableColumn<RowVM, String> colEstado;

    private CoordinadorController parent;  
    private IProyectoService proyectoService; // referencia al controlador padre para loadUI
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarTabla();
 
        /*
        // Columna Descargar
        configurarColumnaDescargar();*/
    }
    
    public void setService(IProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }
    
    private void configurarTabla() {
        colNombreProyecto.setCellValueFactory(d -> d.getValue().nombreProyectoProperty());
        colNombreProfesor.setCellValueFactory(d -> d.getValue().nombreDocenteProperty());
        colTipo.setCellValueFactory(d -> d.getValue().tipoProperty());
        colFecha.setCellValueFactory(d -> d.getValue().fechaProperty());
        colEstado.setCellValueFactory(d -> d.getValue().estadoProperty());
        //configurarColumnaEstado();
    }
    
    private void cargarTabla() {
        if (proyectoService == null) {
            System.err.println("⚠️ Error: proyectoService no fue inicializado.");
            return;
        }
        
        try {
            // Aquí deberías tener un método que devuelva TODOS los archivos
            List<Archivo> archivos = proyectoService.listarTodosArchivos();

            ObservableList<RowVM> rows = FXCollections.observableArrayList();

            for (Archivo a : archivos) {
                Proyecto p = proyectoService.buscarProyectoPorId(a.getProyectoId());

                // Aquí asumo que puedes resolver el nombre del docente con su id
                String nombreDocente = proyectoService.obtenerNombreDocente(p.getDocenteId());

                rows.add(new RowVM(
                    p.getTitulo(),
                    nombreDocente,
                    a.getTipo().name(),
                    a.getFechaSubida(),
                    a.getEstado().name()
                ));
            }

            tabla.setItems(rows);
            tabla.refresh();

        } catch (Exception ex) {
            ex.printStackTrace();
            // opcional: mostrar error en un label
        }
    }
    
    public static class RowVM {
    private final StringProperty nombreProyecto = new SimpleStringProperty();
    private final StringProperty nombreDocente = new SimpleStringProperty();
    private final StringProperty tipo = new SimpleStringProperty();
    private final StringProperty fecha = new SimpleStringProperty();
    private final StringProperty estado = new SimpleStringProperty();

    public RowVM(String nombreProyecto, String nombreDocente, String tipo, String fecha, String estado) {
        this.nombreProyecto.set(nombreProyecto);
        this.nombreDocente.set(nombreDocente);
        this.tipo.set(tipo);
        this.fecha.set(fecha);
        this.estado.set(estado);
    }

    public StringProperty nombreProyectoProperty() { return nombreProyecto; }
    public StringProperty nombreDocenteProperty() { return nombreDocente; }
    public StringProperty tipoProperty() { return tipo; }
    public StringProperty fechaProperty() { return fecha; }
    public StringProperty estadoProperty() { return estado; }
}

    public void setParentController(CoordinadorController parent) {
        this.parent = parent;
    }
    
/*
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
*/
}
