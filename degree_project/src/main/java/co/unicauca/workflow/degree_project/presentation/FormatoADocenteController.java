package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.models.Archivo;
import co.unicauca.workflow.degree_project.domain.models.EstadoProyecto;
import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.models.TipoArchivo;
import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.domain.services.IProyectoService;
import co.unicauca.workflow.degree_project.infra.security.Sesion;
import co.unicauca.workflow.degree_project.main;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;

public class FormatoADocenteController implements Initializable {

    // Header / acciones
    @FXML
    private Label nombreDocente;
    @FXML
    private Button btnIniciarNuevoProyecto;
    @FXML
    private Label lblCupoDocente;
    @FXML
    private TextField txtBuscar;
    @FXML
    private Button btnRefrescar;

    // Panel NUEVO PROYECTO (RF-2)
    @FXML
    private TitledPane pnNuevoProyecto;
    @FXML
    private TextField txtEstudianteId;
    @FXML
    private Button btnBuscarEstudiante;
    @FXML
    private Label lblEstudianteNombre;
    @FXML
    private TextField txtTitulo;

    // Tipo trabajo + archivos
    @FXML
    private ComboBox<TipoTrabajoGrado> cbTipoTrabajo;
    @FXML
    private Button btnSeleccionarPdf;
    @FXML
    private Label lblPdfNombre;
    @FXML
    private HBox rowCarta;
    @FXML
    private Button btnSeleccionarCarta;
    @FXML
    private Label lblCartaNombre;

    @FXML
    private Button btnCrearProyecto;
    @FXML
    private Button btnCancelarNuevo;
    @FXML
    private Label lblNuevoProyectoMsg;

    // Tabla
    @FXML
    private TableView<RowVM> tblProyectos;
    @FXML
    private TableColumn<RowVM, String> colTitulo;
    @FXML
    private TableColumn<RowVM, String> colEstudiante;
    @FXML
    private TableColumn<RowVM, Number> colVersion;
    @FXML
    private TableColumn<RowVM, String> colEstadoProyecto;
    @FXML
    private TableColumn<RowVM, RowVM> colAccion;
    @FXML
    private Label lblTablaMsg;

    
    // Estado de archivos para nuevo proyecto
    private byte[] formatoABytes;
    private String formatoANombre;
    private byte[] cartaBytes;
    private String cartaNombre;
    
    private IProyectoService proyectoService;

    // Inyección por constructor
    public FormatoADocenteController(IProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();

        if (cbTipoTrabajo != null) {
            cbTipoTrabajo.getItems().setAll(TipoTrabajoGrado.TESIS, TipoTrabajoGrado.PRACTICA_PROFESIONAL);
            cbTipoTrabajo.valueProperty().addListener((obs, old, val) -> {
                boolean requiereCarta = (val == TipoTrabajoGrado.PRACTICA_PROFESIONAL);
                if (rowCarta != null) {
                    rowCarta.setVisible(requiereCarta);
                    rowCarta.setManaged(requiereCarta);
                }
                if (!requiereCarta) {
                    cartaBytes = null;
                    cartaNombre = null;
                    if (lblCartaNombre != null) lblCartaNombre.setText("Ningún archivo seleccionado");
                }
            });
        }
    }

    public void setService(IProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    // =================== Utiles UI ===================
    private static String safeText(TextField tf) {
        return tf.getText() == null ? "" : tf.getText().trim();
    }

    private static void setError(Label lbl, String msg) {
        lbl.setStyle("-fx-text-fill:#D84315;");
        lbl.setText(msg);
    }

    private static void setOk(Label lbl, String msg) {
        lbl.setStyle("-fx-text-fill:#2E7D32;");
        lbl.setText(msg);
    }

    // =================== Carga inicial ===================
    public void cargarDatos() {
        AuthResult auth = Sesion.getInstancia().getUsuarioActual();
        if (auth == null) {
            try {
                main.navigate("signin", "Login");
            } catch (Exception ignored) {
            }
            return;
        }
        nombreDocente.setText(auth.nombre());
        ocultarPanelNuevo();
        if (lblPdfNombre != null) lblPdfNombre.setText("Ningún archivo seleccionado");
        if (lblCartaNombre != null) lblCartaNombre.setText("Ningún archivo seleccionado");
        actualizarCupo();
        cargarTabla();
    }

    // =================== Eventos ===================
    @FXML
    private void onIniciarNuevoProyecto() {
        lblNuevoProyectoMsg.setText("");
        mostrarPanelNuevo();
        limpiarNuevoProyecto();
    }

    @FXML
    private void onCancelarNuevo() {
        ocultarPanelNuevo();
        limpiarNuevoProyecto();
    }

    @FXML
    private void onBuscarEstudiante() {
        lblEstudianteNombre.setStyle("");
        lblNuevoProyectoMsg.setText("");

        String estId = safeText(txtEstudianteId);
        if (estId.isEmpty()) {
            setError(lblEstudianteNombre, "Ingrese el ID del estudiante");
            return;
        }
        try {
            boolean libre = proyectoService.estudianteLibre(estId);
            if (libre) setOk(lblEstudianteNombre, "Estudiante disponible");
            else setError(lblEstudianteNombre, "Estudiante con proyecto en curso");
        } catch (IllegalArgumentException ex) {
            setError(lblEstudianteNombre, ex.getMessage());
        } catch (Exception ex) {
            setError(lblEstudianteNombre, "Error al validar estudiante");
        }
    }

    @FXML
    private void onSeleccionarPdf() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar Formato A (PDF)");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File f = fc.showOpenDialog(btnSeleccionarPdf.getScene().getWindow());
        if (f != null) {
            try {
                formatoABytes = Files.readAllBytes(f.toPath());
                formatoANombre = f.getName();
                lblPdfNombre.setText(formatoANombre);
            } catch (Exception ex) {
                lblPdfNombre.setText("Error leyendo el archivo");
            }
        }
    }

    @FXML
    private void onSeleccionarCarta() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar Carta de aceptación (PDF)");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File f = fc.showOpenDialog(btnSeleccionarCarta.getScene().getWindow());
        if (f != null) {
            try {
                cartaBytes = Files.readAllBytes(f.toPath());
                cartaNombre = f.getName();
                lblCartaNombre.setText(cartaNombre);
            } catch (Exception ex) {
                lblCartaNombre.setText("Error leyendo el archivo");
            }
        }
    }

    @FXML
    private void onCrearProyecto() {
        lblNuevoProyectoMsg.setText("");

        AuthResult auth = Sesion.getInstancia().getUsuarioActual();
        if (auth == null) {
            setError(lblNuevoProyectoMsg, "Sesión no válida");
            return;
        }

        String estId = safeText(txtEstudianteId);
        String titulo = safeText(txtTitulo);
        TipoTrabajoGrado tipoTrabajo = cbTipoTrabajo != null ? cbTipoTrabajo.getValue() : TipoTrabajoGrado.TESIS;

        if (tipoTrabajo == null) {
            setError(lblNuevoProyectoMsg, "Seleccione el tipo de trabajo.");
            return;
        }
        if (estId.isEmpty()) {
            setError(lblNuevoProyectoMsg, "Ingrese el ID del estudiante.");
            return;
        }
        if (titulo.isEmpty()) {
            setError(lblNuevoProyectoMsg, "Ingrese el título del proyecto.");
            return;
        }
        if (formatoABytes == null || formatoANombre == null) {
            setError(lblNuevoProyectoMsg, "Adjunte el Formato A (PDF).");
            return;
        }
        if (tipoTrabajo == TipoTrabajoGrado.PRACTICA_PROFESIONAL && (cartaBytes == null || cartaNombre == null)) {
            setError(lblNuevoProyectoMsg, "Adjunte la Carta de aceptación (PDF).");
            return;
        }

        try {
            Proyecto p = new Proyecto();
            p.setTipo(tipoTrabajo.name());
            p.setTitulo(titulo);
            p.setDocenteId(auth.userId());
            p.setEstudianteId(estId);

            Archivo formatoA = new Archivo();
            formatoA.setTipo(TipoArchivo.FORMATO_A);
            formatoA.setNombreArchivo(formatoANombre);
            formatoA.setBlob(formatoABytes);

            if (tipoTrabajo == TipoTrabajoGrado.PRACTICA_PROFESIONAL) {
                Archivo carta = new Archivo();
                carta.setTipo(TipoArchivo.CARTA_ACEPTACION);
                carta.setNombreArchivo(cartaNombre);
                carta.setBlob(cartaBytes);
                proyectoService.crearProyectoConArchivos(p, List.of(formatoA, carta));
            } else {
                proyectoService.crearProyectoConArchivos(p, List.of(formatoA));
            }

            setOk(lblNuevoProyectoMsg, "Proyecto creado correctamente.");
            ocultarPanelNuevo();
            limpiarNuevoProyecto();
            actualizarCupo();
            cargarTabla();
        } catch (Exception ex) {
            setError(lblNuevoProyectoMsg, ex.getMessage() != null ? ex.getMessage() : "Error al crear el proyecto");
        }
    }

    @FXML
    private void onRefrescar() {
        cargarTabla();
        actualizarCupo();
    }

    // =================== Tabla ===================
    private void configurarTabla() {
        colTitulo.setCellValueFactory(d -> d.getValue().tituloProperty());
        colEstudiante.setCellValueFactory(d -> d.getValue().estudianteProperty());
        colVersion.setCellValueFactory(d -> d.getValue().versionProperty());
        colEstadoProyecto.setCellValueFactory(d -> d.getValue().estadoProperty());

        colAccion.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnSubir = new Button("Subir nueva versión");
            private final Button btnObs = new Button("Descargar obs.");
            private final VBox box = new VBox(6, btnSubir, btnObs);

            {
                btnSubir.setMaxWidth(Double.MAX_VALUE);
                btnObs.setMaxWidth(Double.MAX_VALUE);
                box.setFillWidth(true);

                btnSubir.setOnAction(e -> {
                    RowVM r = getItem();
                    if (r != null) subirNuevaVersion(r);
                });
                btnObs.setOnAction(e -> {
                    RowVM r = getItem();
                    if (r != null) descargarObservaciones(r);
                });
            }

            @Override
            protected void updateItem(RowVM row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }

                boolean puedeSubir = proyectoService.canResubmit(row.proyectoId());
                btnSubir.setVisible(puedeSubir);
                btnSubir.setManaged(puedeSubir);

                boolean hayObs = proyectoService.tieneObservacionesFormatoA(row.proyectoId());
                btnObs.setVisible(hayObs);
                btnObs.setManaged(hayObs);

                setGraphic(box);
            }
        });
    }

    private void cargarTabla() {
        lblTablaMsg.setText("");

        AuthResult auth = Sesion.getInstancia().getUsuarioActual();
        if (auth == null) return;

        String filtro = safeText(txtBuscar);
        List<Proyecto> proyectos = proyectoService.listarProyectosDocente(auth.userId(), filtro);

        ObservableList<RowVM> rows = FXCollections.observableArrayList();
        for (Proyecto p : proyectos) {
            long id = p.getId();
            String titulo = p.getTitulo();

            EstadoProyecto estadoFinal = proyectoService.enforceAutoCancelIfNeeded(id);

            int version = proyectoService.maxVersionFormatoA(id);
            String estudianteId = p.getEstudianteId();

            rows.add(new RowVM(id, titulo, estudianteId, version, estadoFinal.name()));
        }

        tblProyectos.setItems(rows);
        tblProyectos.refresh();
    }


    private void subirNuevaVersion(RowVM row) {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar nueva versión (PDF)");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File f = fc.showOpenDialog(tblProyectos.getScene().getWindow());
            if (f == null) return;

            byte[] bytes = Files.readAllBytes(f.toPath());

            Archivo a = new Archivo();
            a.setTipo(TipoArchivo.FORMATO_A);
            a.setNombreArchivo(f.getName());
            a.setBlob(bytes);

            var res = proyectoService.subirNuevaVersionFormatoA(row.proyectoId(), a);

            cargarTabla();
            setOk(lblTablaMsg, "Se subió la versión " + res.getNroVersion());
        } catch (Exception ex) {
            setError(lblTablaMsg, ex.getMessage() != null ? ex.getMessage() : "Error al subir la versión");
        }
    }

    // =================== Helpers ===================
    private void descargarObservaciones(RowVM row) {
        try {
            proyectoService.enforceAutoCancelIfNeeded(row.proyectoId());
            var arch = proyectoService.obtenerUltimoFormatoAConObservaciones(row.proyectoId());
            if (arch == null) {
                setError(lblTablaMsg, "No hay Formato A con observaciones.");
                return;
            }

            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar Formato A con observaciones");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            fc.setInitialFileName(arch.getNombreArchivo());
            File dest = fc.showSaveDialog(tblProyectos.getScene().getWindow());
            if (dest == null) return;

            Files.write(dest.toPath(), arch.getBlob());
            setOk(lblTablaMsg, "Observaciones descargadas.");
            cargarTabla();
        } catch (Exception ex) {
            setError(lblTablaMsg, ex.getMessage() != null ? ex.getMessage() : "Error al descargar observaciones");
        }
    }

    private void actualizarCupo() {
        AuthResult auth = Sesion.getInstancia().getUsuarioActual();
        if (auth == null) {
            btnIniciarNuevoProyecto.setDisable(true);
            return;
        }
        boolean cupo = proyectoService.docenteTieneCupo(auth.userId());
        btnIniciarNuevoProyecto.setDisable(!cupo);
        lblCupoDocente.setText(cupo ? "" : "Límite de 7 proyectos en curso alcanzado");
    }

    private void mostrarPanelNuevo() {
        pnNuevoProyecto.setVisible(true);
        pnNuevoProyecto.setManaged(true);
        pnNuevoProyecto.setExpanded(true);
    }

    private void ocultarPanelNuevo() {
        pnNuevoProyecto.setVisible(false);
        pnNuevoProyecto.setManaged(false);
        pnNuevoProyecto.setExpanded(false);
    }

    private void limpiarNuevoProyecto() {
        txtEstudianteId.clear();
        lblEstudianteNombre.setText("");
        txtTitulo.clear();
        if (cbTipoTrabajo != null) cbTipoTrabajo.getSelectionModel().clearSelection();
        lblPdfNombre.setText("Ningún archivo seleccionado");
        if (lblCartaNombre != null) lblCartaNombre.setText("Ningún archivo seleccionado");
        lblNuevoProyectoMsg.setText("");
        formatoABytes = null;
        formatoANombre = null;
        cartaBytes = null;
        cartaNombre = null;
        if (rowCarta != null) {
            rowCarta.setVisible(false);
            rowCarta.setManaged(false);
        }
    }

    public enum TipoTrabajoGrado {TESIS, PRACTICA_PROFESIONAL}

    // =================== ViewModel ===================
    public static class RowVM {
        private final long proyectoId;
        private final StringProperty titulo = new SimpleStringProperty();
        private final StringProperty estudiante = new SimpleStringProperty();
        private final IntegerProperty version = new SimpleIntegerProperty();
        private final StringProperty estado = new SimpleStringProperty();

        public RowVM(long proyectoId, String titulo, String estudiante, int version, String estado) {
            this.proyectoId = proyectoId;
            this.titulo.set(titulo);
            this.estudiante.set(estudiante);
            this.version.set(version);
            this.estado.set(estado);
        }

        public long proyectoId() {
            return proyectoId;
        }

        public StringProperty tituloProperty() {
            return titulo;
        }

        public StringProperty estudianteProperty() {
            return estudiante;
        }

        public IntegerProperty versionProperty() {
            return version;
        }

        public StringProperty estadoProperty() {
            return estado;
        }
    }
}
