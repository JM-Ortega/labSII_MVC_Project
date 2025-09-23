package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.access.ArchivoRepositorySqlite;
import co.unicauca.workflow.degree_project.access.IArchivoRepository;
import co.unicauca.workflow.degree_project.access.IProyectoRepository;
import co.unicauca.workflow.degree_project.access.ProyectoRepositorySqlite;
import co.unicauca.workflow.degree_project.access.SqliteRepository;
import co.unicauca.workflow.degree_project.domain.services.IProyectoService;
import co.unicauca.workflow.degree_project.domain.services.ProyectoService;
import co.unicauca.workflow.degree_project.main;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class CoordinadorController implements Initializable{
    @FXML
    private AnchorPane contentArea;
    
    @FXML
    private Button btnProyectos;

    @FXML
    private Button btnSalir;

    private Button selectedButton = null; // botón actualmente seleccionado
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnProyectos.setOnMouseEntered(e -> btnProyectos.getStyleClass().add("hoverable"));
        btnProyectos.setOnMouseExited(e -> btnProyectos.getStyleClass().remove("hoverable"));
        
        btnSalir.setOnMouseEntered(e -> btnSalir.getStyleClass().add("hoverable"));
        btnSalir.setOnMouseExited(e -> btnSalir.getStyleClass().remove("hoverable"));

        
        // Configurar eventos para cada botón
        btnProyectos.setOnAction(e -> {
            loadUI("/co/unicauca/workflow/degree_project/view/Coordinador_Proyectos");
            selectButton(btnProyectos);
        });
    }
 
    private void selectButton(Button button) {
        // Si ya hay un botón seleccionado, quitarle la clase CSS
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("selected");
        }
        // Agregar clase al nuevo botón seleccionado
        if (!button.getStyleClass().contains("selected")) {
            button.getStyleClass().add("selected");
        }
        selectedButton = button;
    }
    
    
     public void loadUI(String fxml) {
        try {
            String path = fxml+".fxml";
            FXMLLoader loader = main.newInjectedLoader(path);
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof Co_Proyecto_Controller cpc) {
                cpc.setParentController(this);
            } else if (controller instanceof Co_Observaciones_Controller coc) {
                coc.setParentController(this);
            }

            contentArea.getChildren().setAll(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void switchToSignin(ActionEvent event) {
        try {
            selectButton(btnSalir);
            // Usa tu clase main para navegar
            main.navigate("signin", "SignIn");

        } catch (IOException e) {
            System.err.println("No se pudo abrir la vista de Signin");
            e.printStackTrace();
        }
    }
}
