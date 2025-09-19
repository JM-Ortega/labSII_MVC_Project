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
public class DocenteController implements Initializable {

    @FXML private Button btnPrincipal;
    @FXML private Button btnFormatoA;
    @FXML private Button btnSalir;
    @FXML private Button btnEstadisticas;
    @FXML private Label nombreDocente;
    @FXML private BorderPane bp;
    @FXML private AnchorPane ap;
    
    private IUserService service;
    private String email;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        activarBoton(btnPrincipal, btnFormatoA, btnSalir, btnEstadisticas);
        cargarDatos();
    }

    @FXML
    void switchToLogin(ActionEvent event) {
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
        activarBoton(btnPrincipal, btnFormatoA, btnSalir, btnEstadisticas);
        bp.setCenter(ap);
    }

    @FXML
    private void showInfoFormatoA(ActionEvent event) {
        activarBoton(btnFormatoA, btnPrincipal, btnSalir, btnEstadisticas);
        loadModule("/co/unicauca/workflow/degree_project/view/FormatoADocente");
    }
    
    @FXML
    private void showEstadisticas(ActionEvent event) {
        activarBoton(btnEstadisticas, btnPrincipal, btnSalir, btnFormatoA);
        loadModule("/co/unicauca/workflow/degree_project/view/EstadisticasDocente");
    }
    
    private void loadModule(String modulo) {
        try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(modulo + ".fxml"));
        Parent moduleRoot = loader.load();

        Object controller = loader.getController();
        if (controller instanceof FormatoADocenteController fa) {
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
            nombreDocente.setText(nombre);
            } else {
            System.err.println("Service o email no seteados");
        }
    }
    
    private void activarBoton(Button botonActivo, Button... otros) {
        botonActivo.getStyleClass().remove("btn-default");
        if (!botonActivo.getStyleClass().contains("btn-pressed")) {
            botonActivo.getStyleClass().add("btn-pressed");
        }

        for (Button b : otros) {
            b.getStyleClass().remove("btn-pressed");
            if (!b.getStyleClass().contains("btn-default")) {
                b.getStyleClass().add("btn-default");
            }
        }
    }
}

