package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.IUserService;
import co.unicauca.workflow.degree_project.main;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

/**
 * FXML Controller class
 *
 * @author Maryuri
 */
public class EstudianteController implements Initializable {

    @FXML private Button btnPrincipal;
    @FXML private Button btnFormatoA;
    @FXML private Button btnSalir;
    @FXML private Label nombreEstudiante;
    @FXML private BorderPane bp;
    @FXML private AnchorPane ap;
    
    private IUserService service;
    private String email;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnPrincipal.getStyleClass().add("btn-pressed");
        btnFormatoA.getStyleClass().add("btn-default");
        btnSalir.getStyleClass().add("btn-default");
    }

    @FXML
    private void switchToLogin(ActionEvent event) {
        try {
            btnSalir.getStyleClass().add("btn-pressed");
            main.navigate("signin", "Login");
        } catch (IOException e) {
            System.err.println("No se pudo abrir la vista de Login");
            e.printStackTrace();
        }
    }

    @FXML
    private void showInfoPrincipal(ActionEvent event) {
        btnPrincipal.getStyleClass().add("btn-pressed");
        btnFormatoA.getStyleClass().remove("btn-pressed");
        btnFormatoA.getStyleClass().add("btn-default");
        bp.setCenter(ap);
    }

    @FXML
    private void showInfoFormatoA(ActionEvent event) {
        btnFormatoA.getStyleClass().add("btn-pressed");
        btnPrincipal.getStyleClass().remove("btn-pressed");
        btnPrincipal.getStyleClass().add("btn-default");
        loadModule("/co/unicauca/workflow/degree_project/view/FormatoAEstudiante");
    }

    private void loadModule(String modulo) {
        try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(modulo + ".fxml"));
        Parent moduleRoot = loader.load();

        Object controller = loader.getController();
        if (controller instanceof FormatoAEstudianteController fa) {
            fa.setService(service);
            fa.setEmail(email);
            fa.cargarDatos();
        }

        bp.setCenter(moduleRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    void setService(IUserService service) {
        this.service = service;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void cargarDatos() {
        if (service != null && email != null) {
            String nombre = service.getName(email);
            nombreEstudiante.setText(nombre);
            } else {
            System.err.println("Service o email no seteados");
        }
    }
}