package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.access.Factory;
import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.domain.services.IUserService;
import co.unicauca.workflow.degree_project.domain.services.UserService;
import co.unicauca.workflow.degree_project.infra.security.Argon2PasswordHasher;
import co.unicauca.workflow.degree_project.infra.security.Sesion;
import co.unicauca.workflow.degree_project.presentation.dto.ProjectArchivoDTO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.image.Image;

/**
 * FXML Controller class
 *
 * @author Maryuri
 */
public class FormatoAEstudianteController implements Initializable {  
    @FXML private Label nombreEstudiante; 
    @FXML private TableView<ProjectArchivoDTO> tabla;
    @FXML private TableColumn<ProjectArchivoDTO, String> colTipo;
    @FXML private TableColumn<ProjectArchivoDTO, String> colTitulo;
    @FXML private TableColumn<ProjectArchivoDTO, String> colFechaEmision;
    @FXML private TableColumn<ProjectArchivoDTO, String> colEstado;
    @FXML private TableColumn<ProjectArchivoDTO, Integer> colVersion;
    @FXML private TableColumn<ProjectArchivoDTO, Void> colAcciones;
    @FXML private Label LabelInfo;
 
    
    /** * Initializes the controller class. */ 
    @Override public void initialize(URL url, ResourceBundle rb) { 
        configurarColumnas();
        configurarColumnaEstado();
        cargarDatos(); 
    } 
    
    public void cargarDatos() {
        AuthResult usuario = Sesion.getInstancia().getUsuarioActual();
        if(usuario != null){ 
            nombreEstudiante.setText(usuario.nombre()); 
            IUserService service = new UserService( 
                    Factory.getInstance().getRepository("default"),
                    new Argon2PasswordHasher() ); 
            
            List<ProjectArchivoDTO> docs = service.getDatosEstudiante(usuario.userId()); 
            tabla.setItems(FXCollections.observableArrayList(docs)); 
        }else{ 
            System.err.println("No hay usuario en sesion"); 
        } 
    } 
    
    private void configurarColumnas() {
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo")); 
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo")); 
        colFechaEmision.setCellValueFactory(new PropertyValueFactory<>("fechaEmision")); 
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado")); 
        colVersion.setCellValueFactory(new PropertyValueFactory<>("version")); 

        configurarColumnaAcciones();
    }
    
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnDescargar = new Button("Descargar");

            {
                btnDescargar.setOnAction(event -> {
                    ProjectArchivoDTO dto = getTableView().getItems().get(getIndex());
                    if (dto != null) {
                        manejarDescarga(dto);
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

    private void manejarDescarga(ProjectArchivoDTO dto) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(dto.getTipo()+ "_v" + dto.getVersion() + ".pdf");
            File file = fileChooser.showSaveDialog(tabla.getScene().getWindow());

            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(dto.getContenido());
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
        colEstado.setCellFactory(col -> new TableCell<ProjectArchivoDTO, String>() {
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
                    case "ACEPTADO" -> img = loadImage("/co/unicauca/workflow/degree_project/images/aceptado.png");
                    case "RECHAZADO" -> img = loadImage("/co/unicauca/workflow/degree_project/images/rechazado.png");
                    case "A_EVALUAR" -> img = loadImage("/co/unicauca/workflow/degree_project/images/a_evaluar.png");
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

    // helper mínimo para cargar la imagen desde resources
    private Image loadImage(String resourcePath) {
        var is = getClass().getResourceAsStream(resourcePath);
        return (is == null) ? null : new Image(is);
    }

}