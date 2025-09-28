package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.models.Archivo;
import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.services.IProyectoService;
import co.unicauca.workflow.degree_project.main;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;

public class Co_Proyecto_Controller implements Initializable{
    @FXML
    private TableView<RowVM> tabla;
    @FXML
    private TableColumn<RowVM, String> colNombreProyecto;
    @FXML
    private TableColumn<RowVM, String> colNombreProfesor;
    @FXML
    private TableColumn<RowVM, String> colTipoA;
    @FXML
    private TableColumn<RowVM, String> colTipoP;
    @FXML
    private TableColumn<RowVM, String> colFecha;
    @FXML
    private TableColumn<RowVM, RowVM> colEstado;
    @FXML
    private TableColumn<RowVM, RowVM> colDescargar;

    private CoordinadorController parent;  
    private IProyectoService proyectoService; // referencia al controlador padre para loadUI
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarTabla();
    }
    
    private Stage estadisticasStage;
    @FXML
    private void onVerEstadisticas(ActionEvent event) {
        if (estadisticasStage == null) { 
            try {
                FXMLLoader loaderEstadisticas = main.newInjectedLoader(
                        "/co/unicauca/workflow/degree_project/view/EstadisticasCoordinador.fxml"
                );
                Parent estadisticasView = loaderEstadisticas.load();

                estadisticasStage = new Stage();
                estadisticasStage.setTitle("Estadísticas - Coordinador");
                estadisticasStage.setScene(new Scene(estadisticasView));

                Stage mainStage = (Stage) tabla.getScene().getWindow();
                estadisticasStage.initOwner(mainStage);

                // 🔑 importante: limpiar la referencia cuando se cierra
                estadisticasStage.setOnHidden(event1 -> estadisticasStage = null);

                estadisticasStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            estadisticasStage.toFront();
        }
    }

    
    public void setService(IProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }
    
    private void configurarTabla() {
        colNombreProyecto.setCellValueFactory(d -> d.getValue().nombreProyectoProperty());
        colNombreProfesor.setCellValueFactory(d -> d.getValue().nombreDocenteProperty());
        colTipoA.setCellValueFactory(d -> d.getValue().tipoAProperty());
        colTipoP.setCellValueFactory(d -> d.getValue().tipoPProperty());
        colFecha.setCellValueFactory(d -> d.getValue().fechaProperty());

        colFecha.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String soloFecha = item.split(" ")[0];
                    setText(soloFecha);
                }
            }
        });

        colEstado.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        configurarColumnaEstado();
        configurarColumnaDescargar();
        tabla.getColumns().forEach(col -> col.setReorderable(false));
        colNombreProyecto.setSortable(false);
        colNombreProfesor.setSortable(false);
        colTipoA.setSortable(false);
        colTipoP.setSortable(false);
        colFecha.setSortable(false);
        colEstado.setSortable(false);
        colDescargar.setSortable(false);
    }

    private void cargarTabla() {
        if (proyectoService == null) {
            System.err.println("Error: proyectoService no fue inicializado.");
            return;
        }
        
        try {
            List<Archivo> archivos = proyectoService.listarTodosArchivos();

            ObservableList<RowVM> rows = FXCollections.observableArrayList();

            for (Archivo a : archivos) {
                Proyecto p = proyectoService.buscarProyectoPorId(a.getProyectoId());

                String nombreDocente = proyectoService.obtenerNombreDocente(p.getDocenteId());
                String correoDocente = proyectoService.obtenerCorreoDocente(p.getDocenteId());
                String correoEstudiante = proyectoService.obtenerCorreoEstudiante(p.getEstudianteId());
                
                rows.add(new RowVM(
                    a.getId(),
                    p.getId(),
                    p.getTitulo(),
                    nombreDocente,
                    a.getTipo().name(),
                    p.getTipo().name(),
                    a.getFechaSubida(),
                    a.getEstado().name(),
                    correoDocente,
                    correoEstudiante
                ));
            }

            rows.sort((r1, r2) -> {
                // "Pendiente" primero, el resto después
                if (r1.estadoProperty().get().equalsIgnoreCase("Pendiente") &&
                    !r2.estadoProperty().get().equalsIgnoreCase("Pendiente")) {
                    return -1;
                } else if (!r1.estadoProperty().get().equalsIgnoreCase("Pendiente") &&
                           r2.estadoProperty().get().equalsIgnoreCase("Pendiente")) {
                    return 1;
                }
                return 0; // mismo estado → mantener orden
            });
            tabla.setItems(rows);
            tabla.refresh();

        } catch (Exception ex) {
            ex.printStackTrace();
            // opcional: mostrar error en un label
        }
    }
    
    public static class RowVM {
        private final long archivoId;
        private final long proyectoId;
        private final StringProperty nombreProyecto = new SimpleStringProperty();
        private final StringProperty nombreDocente = new SimpleStringProperty();
        private final StringProperty tipoA = new SimpleStringProperty();
        private final StringProperty tipoP = new SimpleStringProperty();
        private final StringProperty fecha = new SimpleStringProperty();
        private final StringProperty estado = new SimpleStringProperty();
        private final StringProperty correoProfesor = new SimpleStringProperty();
        private final StringProperty correoEstudiante = new SimpleStringProperty();

        public RowVM(long archivoId, long proyectoId, String nombreProyecto, String nombreDocente, String tipoA, String tipoP, 
                String fecha, String estado, String correoProfesor, String correoEstudiante) {
            this.archivoId = archivoId;
            this.proyectoId = proyectoId;
            this.nombreProyecto.set(nombreProyecto);
            this.nombreDocente.set(nombreDocente);
            this.tipoA.set(tipoA);
            this.tipoP.set(tipoP);
            this.fecha.set(fecha);
            this.estado.set(estado);
            this.correoProfesor.set(correoProfesor);
            this.correoEstudiante.set(correoEstudiante);
        }

        public long archivoId() {return archivoId;}
        public long proyectoId() {return proyectoId;}
        public StringProperty nombreProyectoProperty() { return nombreProyecto; }
        public StringProperty nombreDocenteProperty() { return nombreDocente; }
        public StringProperty tipoAProperty() { return tipoA; }
        public StringProperty tipoPProperty() { return tipoP; }
        public StringProperty fechaProperty() { return fecha; }
        public StringProperty estadoProperty() { return estado; }
        public StringProperty correoProfesor() { return correoProfesor; }
        public StringProperty correoEstudiante() { return correoEstudiante; }
    }

    public void setParentController(CoordinadorController parent) {
        this.parent = parent;
    }
 
    private void configurarColumnaEstado() {
        colEstado.setCellFactory(column -> new TableCell<RowVM, RowVM>() {
            private final Button estadoBtn = new Button();

            {
                estadoBtn.setOnAction(e -> {
                    RowVM row = getItem(); 
                    if (row == null) return;

                    String estadoActual = row.estadoProperty().get();

                    if ("PENDIENTE".equalsIgnoreCase(estadoActual)) {
                        if (parent != null) {
                            parent.loadUI(
                                "/co/unicauca/workflow/degree_project/view/Coordinador_Observaciones",
                                row //pasamos el RowVM
                            );
                        }
                    } else {
                        alerta(Alert.AlertType.WARNING,
                                "Acción no permitida",
                                null,
                                "Este proyecto ya fue evaluado. No se puede volver a evaluar.");
                    }
                });
            }

            @Override
            protected void updateItem(RowVM row, boolean empty) {
                super.updateItem(row, empty);

                if (empty || row == null) {
                    setGraphic(null);
                } else {
                    String estado = row.estadoProperty().get();
                    estadoBtn.setText(estado);

                    Image icon = null;
                    if ("PENDIENTE".equalsIgnoreCase(estado)) {
                        estadoBtn.getStyleClass().add("estado-rojo");
                        icon = new Image(getClass().getResourceAsStream(
                                "/co/unicauca/workflow/degree_project/images/ojo_abierto.png"));
                    } else if ("OBSERVADO".equalsIgnoreCase(estado)) {
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
        colDescargar.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colDescargar.setCellFactory(col -> new TableCell<>() {
            private final Button btnDescargar = new Button();
            private final ImageView imgView;

            {
                btnDescargar.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                imgView = new ImageView(
                    new Image(getClass().getResourceAsStream(
                        "/co/unicauca/workflow/degree_project/images/descargar.png"))
                );
                imgView.setFitWidth(20);
                imgView.setFitHeight(20);
                btnDescargar.setGraphic(imgView);

                btnDescargar.setOnAction(event -> {
                    RowVM r = getItem();
                    if (r != null) descargarObservaciones(r);
                });
            }

            @Override
            protected void updateItem(RowVM row, boolean empty) {
                super.updateItem(row, empty);
                setGraphic(empty || row == null ? null : btnDescargar);
            }
        });
    }
    
    private void descargarObservaciones(RowVM row) {
        try {
            proyectoService.enforceAutoCancelIfNeeded(row.archivoId());
            var arch = proyectoService.obtenerFormatoA(row.archivoId());
            if (arch == null) {
                alerta(Alert.AlertType.WARNING,
                       "Formato A no encontrado",
                       null,
                       "No hay Formato A disponible con observaciones para este proyecto.");
                return;
            }

            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar Formato A con observaciones");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            fc.setInitialFileName(arch.getNombreArchivo());

            File dest = fc.showSaveDialog(tabla.getScene().getWindow());
            if (dest == null) return;

            Files.write(dest.toPath(), arch.getBlob());

            alerta(Alert.AlertType.INFORMATION,
                   "Archivo descargado",
                   null,
                   "El archivo se guardó exitosamente en:\n" + dest.getAbsolutePath());

            cargarTabla();
        } catch (Exception ex) {
            alerta(Alert.AlertType.ERROR,
                   "Error al descargar",
                   null,
                   ex.getMessage() != null ? ex.getMessage() : "Error desconocido al guardar el archivo.");
        }
    }

    private void alerta(Alert.AlertType type, String title, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
