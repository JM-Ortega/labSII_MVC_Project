package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.domain.services.IUserService;
import co.unicauca.workflow.degree_project.infra.security.Sesion;
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
    
    private IUserService userService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        activarBoton(btnPrincipal, btnFormatoA, btnSalir);
        cargarDatos();
    }

    @FXML
    private void switchToLogin(ActionEvent event) {
        try {
            Sesion.getInstancia().limpiar();
            btnSalir.getStyleClass().add("btn-pressed");
            main.navigate("signin", "Login");
        } catch (IOException e) {
            System.err.println("No se pudo abrir la vista de Login");
            e.printStackTrace();
        }
    }

    @FXML
    private void showInfoPrincipal(ActionEvent event) {
        activarBoton(btnPrincipal, btnFormatoA, btnSalir);
        bp.setCenter(ap);
    }

    @FXML
    private void showInfoFormatoA(ActionEvent event) {
        activarBoton(btnFormatoA, btnPrincipal, btnSalir);
        loadModule("/co/unicauca/workflow/degree_project/view/FormatoAEstudiante");
    }

    private void loadModule(String modulo){
        try {
            String path = modulo + ".fxml";
            FXMLLoader loader = main.newInjectedLoader(path);
            Parent moduleRoot = loader.load();
            Object controller = loader.getController();
            if (controller instanceof FormatoAEstudianteController fa) {
                fa.cargarDatos();
            }
            bp.setCenter(moduleRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    void cargarDatos() {
        AuthResult auth = Sesion.getInstancia().getUsuarioActual();
        if (auth != null) {
            nombreEstudiante.setText(auth.nombre());
        } else {
            System.err.println("No hay sesión activa; redirigiendo a login.");
            try {
                main.navigate("signin", "Login");
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }

}