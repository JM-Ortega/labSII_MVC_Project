package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.domain.services.IProyectoService;
import co.unicauca.workflow.degree_project.infra.security.Sesion;
import co.unicauca.workflow.degree_project.main;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.image.Image;

public class FormatoAEstudianteController implements Initializable {  

    @FXML private Label nombreEstudiante; 
    @FXML private TableView<Proyecto> tabla;
    @FXML private TableColumn<Proyecto, String> colTipo;
    @FXML private TableColumn<Proyecto, String> colTitulo;
    @FXML private TableColumn<Proyecto, String> colFechaEmision;
    @FXML private TableColumn<Proyecto, String> colEstado;
    @FXML private TableColumn<Proyecto, Integer> colVersion;
    @FXML private TableColumn<Proyecto, Void> colAcciones;
    @FXML private Label LabelInfo;
    
    private IProyectoService proyectoService;
    private Map<Long, Proyecto> proyectosCache = new HashMap<>();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        configurarColumnas();
        configurarColumnaEstado();
        cargarDatos();
    } 
    
    public void setService(IProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }
    
    public void cargarDatos() {
        AuthResult auth = Sesion.getInstancia().getUsuarioActual();
        if (auth != null) {
            nombreEstudiante.setText(auth.nombre());
            
            List<Proyecto> proyectos = proyectoService.listarFormatosAPorEstudiante(auth.userId());
            
            for (Proyecto proyecto : proyectos) {
                if (!proyectosCache.containsKey(proyecto.getId())) {
                    Proyecto encontrado = proyectoService.buscarProyectoPorId(proyecto.getId());
                    if (encontrado != null) {
                        proyectosCache.put(encontrado.getId(), encontrado);
                    }
                }
            }

            tabla.setItems(FXCollections.observableArrayList(proyectos));


        } else {
            System.err.println("No hay sesión activa; redirigiendo a login.");
            try {
                main.navigate("signin", "Login");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void configurarColumnas() {
        colTipo.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTipo().toString())
        );

        colTitulo.setCellValueFactory(cellData -> {
            Proyecto proyecto = proyectosCache.get(cellData.getValue().getId());
            return new SimpleStringProperty(proyecto != null ? proyecto.getTitulo() : "N/A");
        });

        colFechaEmision.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getArchivo().getFechaSubida())
        );

        colEstado.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getArchivo().getEstado().toString())
        );

        colVersion.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getArchivo().getNroVersion()).asObject()
        );

        configurarColumnaAcciones();
    }
    
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnDescargar = new Button("Descargar");

            {
                btnDescargar.setOnAction(event -> {
                    Proyecto archivo = getTableView().getItems().get(getIndex());
                    if (archivo != null) {
                        manejarDescarga(archivo);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDescargar);
                }
            }
        });
    }

    private void manejarDescarga(Proyecto proyecto) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(proyecto.getArchivo().getNombreArchivo() 
                                           + "_v" + proyecto.getArchivo().getNroVersion() + ".pdf");
            File file = fileChooser.showSaveDialog(tabla.getScene().getWindow());

            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(proyecto.getArchivo().getBlob());
                }
                LabelInfo.setText("Archivo descargado en: " + file.getAbsolutePath());
                LabelInfo.setVisible(true);
                
                PauseTransition pause = new PauseTransition(Duration.seconds(4));
                pause.setOnFinished(event -> LabelInfo.setVisible(false));
                pause.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void configurarColumnaEstado() {
        colEstado.setCellFactory(col -> new TableCell<Proyecto, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Image img = null;
                String e = estado.trim().toUpperCase();
                switch (e) {
                    case "APROBADO" -> img = loadImage("/co/unicauca/workflow/degree_project/images/aprobado.png");
                    case "OBSERVADO" -> img = loadImage("/co/unicauca/workflow/degree_project/images/observado.png");
                    case "PENDIENTE" -> img = loadImage("/co/unicauca/workflow/degree_project/images/pendiente.png");
                    default -> img = null;
                }

                if (img != null) {
                    imageView.setImage(img);
                    setGraphic(imageView);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(estado);
                }
            }
        });
    }

    private Image loadImage(String resourcePath) {
        var is = getClass().getResourceAsStream(resourcePath);
        return (is == null) ? null : new Image(is);
    }
}
