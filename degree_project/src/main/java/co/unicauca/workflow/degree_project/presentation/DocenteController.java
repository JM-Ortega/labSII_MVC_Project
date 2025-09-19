package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.domain.services.IUserService;
import co.unicauca.workflow.degree_project.infra.security.Sesion;
import co.unicauca.workflow.degree_project.main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DocenteController implements Initializable {

    @FXML private Button btnPrincipal;
    @FXML private Button btnFormatoA;
    @FXML private Button btnSalir;
    @FXML private Label nombreDocente;
    @FXML private BorderPane bp;
    @FXML private AnchorPane ap;

    private IUserService userService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnPrincipal.getStyleClass().add("btn-pressed");
        btnFormatoA.getStyleClass().add("btn-default");
        btnSalir.getStyleClass().add("btn-default");
    }

    @FXML
    void switchToLogin(ActionEvent event) {
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
        loadModule("/co/unicauca/workflow/degree_project/view/FormatoADocente");
    }

    private void loadModule(String modulo) {
        try {
            String path = modulo + ".fxml";
            FXMLLoader loader = main.newInjectedLoader(path);
            Parent moduleRoot = loader.load();

            Object controller = loader.getController();
            if (controller instanceof FormatoADocenteController fa) {
                fa.cargarDatos();
            }

            bp.setCenter(moduleRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUserService(IUserService userService) {
        this.userService = userService;
    }

    public void cargarDatos() {
        AuthResult auth = Sesion.getInstancia().getUsuarioActual();
        if (auth != null) {
            nombreDocente.setText(auth.nombre());
        } else {
            System.err.println("No hay sesión activa; redirigiendo a login.");
            try {
                main.navigate("signin", "Login");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
