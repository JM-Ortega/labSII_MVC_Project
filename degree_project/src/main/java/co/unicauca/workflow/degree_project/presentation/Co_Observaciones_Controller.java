package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.models.Archivo;
import co.unicauca.workflow.degree_project.domain.models.EstadoArchivo;
import co.unicauca.workflow.degree_project.domain.models.TipoArchivo;
import co.unicauca.workflow.degree_project.domain.services.IProyectoService;
import co.unicauca.workflow.degree_project.presentation.Co_Proyecto_Controller.RowVM;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.FileChooser;

public class Co_Observaciones_Controller implements Initializable{
    @FXML
    private Label volver;
    
    @FXML
    private ComboBox<String> cbxValoracion;

    @FXML
    private Label lblFechaEntrega;

    @FXML
    private Label lblNombreProyecto;

    @FXML
    private Label lblTipoArchivo;

    @FXML
    private Label lblTipoProyecto;
    
    @FXML
    private Button btnArchivo;

    @FXML
    private Label lblArchivo;
    
    @FXML
    private Button btnEnviar;

    private IProyectoService proyectoService;
    private CoordinadorController parent;
    private Archivo archivoSeleccionado; // Guardamos el archivo subido
    private RowVM rowActual;             // Guardamos el row que nos pasa el padre
    
    public void setParentController(CoordinadorController parent) {
        this.parent = parent;
    }
    
    public void setService(IProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    public void setRowVM(RowVM row) {
        this.rowActual = row;
        lblNombreProyecto.setText(row.nombreProyectoProperty().get());
        lblTipoProyecto.setText(row.tipoPProperty().get());
        lblFechaEntrega.setText(row.fechaProperty().get());
        lblTipoArchivo.setText(row.tipoAProperty().get());
        lblArchivo.setText("");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Acción al hacer clic en el Label volver
        volver.setOnMouseClicked(e -> {
            if (parent != null) {
                parent.loadUI("/co/unicauca/workflow/degree_project/view/Coordinador_Proyectos");
            }
        });
        
        // Agregar opciones
        cbxValoracion.getItems().addAll("ACEPTADO", "RECHAZADO");

        // Personalizar las celdas de la lista desplegable
        cbxValoracion.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("aceptado", "rechazado");
                } else {
                    setText(item);
                    getStyleClass().removeAll("aceptado", "rechazado");
                    if (item.equals("ACEPTADO")) {
                        getStyleClass().add("aceptado");
                    } else if (item.equals("RECHAZADO")) {
                        getStyleClass().add("rechazado");
                    }
                }
            }
        });

        // Personalizar el valor seleccionado en el botón del ComboBox
        cbxValoracion.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("aceptado", "rechazado");
                } else {
                    setText(item);
                    getStyleClass().removeAll("aceptado", "rechazado");
                    if (item.equals("ACEPTADO")) {
                        getStyleClass().add("aceptado");
                    } else if (item.equals("RECHAZADO")) {
                        getStyleClass().add("rechazado");
                    }
                }
            }
        });
        
        btnEnviar.setDisable(true);

        // Listener: habilitar si hay selección y archivo cargado
        cbxValoracion.valueProperty().addListener((obs, oldVal, newVal) -> {
            validarEnviar();
        });
    }
    
    private void validarEnviar() {
        boolean habilitar = cbxValoracion.getValue() != null && archivoSeleccionado != null;
        btnEnviar.setDisable(!habilitar);
    }

    @FXML
    private void seleccionarArchivo(ActionEvent event) {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar archivo PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

            File f = fc.showOpenDialog(btnArchivo.getScene().getWindow());
            if (f == null) return;

            byte[] bytes = Files.readAllBytes(f.toPath());
            
            archivoSeleccionado = new Archivo();
            archivoSeleccionado.setTipo(TipoArchivo.FORMATO_A);
            archivoSeleccionado.setNombreArchivo(f.getName());
            archivoSeleccionado.setBlob(bytes);

            lblArchivo.setText("Archivo seleccionado: " + archivoSeleccionado.getNombreArchivo());

            validarEnviar(); // Habilitar si ya hay selección y archivo
        } catch (Exception ex) {
            System.err.println("Error al seleccionar archivo: " + ex.getMessage());
        }
    }
    
    private void alerta(Alert.AlertType type, String title, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
    
    @FXML
    private void enviar(ActionEvent event) {
        if (archivoSeleccionado == null || cbxValoracion.getValue() == null) {
            alerta(Alert.AlertType.WARNING, "Campos incompletos", null,
                   "Debes seleccionar un archivo y una valoración.");
            return;
        }

        archivoSeleccionado.setProyectoId(rowActual.proyectoId());

        // Asignar estado al archivo según el valor del ComboBox
        String valoracion = cbxValoracion.getValue();
        if ("ACEPTADO".equalsIgnoreCase(valoracion)) {
            archivoSeleccionado.setEstado(EstadoArchivo.APROBADO);
        } else if ("RECHAZADO".equalsIgnoreCase(valoracion)) {
            archivoSeleccionado.setEstado(EstadoArchivo.OBSERVADO);
        }

        int resultado = proyectoService.subirObservacion(rowActual.proyectoId(), archivoSeleccionado, 
                rowActual.correoProfesor().get(), rowActual.correoEstudiante().get());

        if (resultado == 1) {
            alerta(Alert.AlertType.INFORMATION, "Correcto", null,
               "El proyecto fue aprobado y se marcó como TERMINADO.");
            parent.loadUI("/co/unicauca/workflow/degree_project/view/Coordinador_Proyectos");
        } else if (resultado == 2) {
            alerta(Alert.AlertType.INFORMATION, "Correcto", null, 
               "El archivo fue rechazado y marcado como OBSERVADO.");
            parent.loadUI("/co/unicauca/workflow/degree_project/view/Coordinador_Proyectos");
        } else {
            alerta(Alert.AlertType.ERROR, "Error", null, 
               "El estado registrado para el archivo no fue ni observado ni aprobado.");
        }    
    }
}
