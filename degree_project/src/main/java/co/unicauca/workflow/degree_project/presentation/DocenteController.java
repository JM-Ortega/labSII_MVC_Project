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
import javafx.scene.Scene;
import javafx.stage.Stage;

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
        activarBoton(btnPrincipal, btnFormatoA, btnSalir);
        cargarDatos();
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
        activarBoton(btnPrincipal, btnFormatoA, btnSalir);
        bp.setCenter(ap);
    }
    
    @FXML
    private void showInfoFormatoA(ActionEvent event) {
        activarBoton(btnFormatoA, btnPrincipal, btnSalir);

        try {
            FXMLLoader loaderFormatoA = main.newInjectedLoader(
                "/co/unicauca/workflow/degree_project/view/FormatoADocente.fxml"
            );
            Parent formatoAView = loaderFormatoA.load();
            FormatoADocenteController formatoAController = loaderFormatoA.getController();
            formatoAController.cargarDatos(); 

            bp.setCenter(formatoAView);

            FXMLLoader loaderEstadisticas = main.newInjectedLoader(
                "/co/unicauca/workflow/degree_project/view/EstadisticasDocente.fxml"
            );
            Parent estadisticasView = loaderEstadisticas.load();

            Stage estadisticasStage = new Stage();
            estadisticasStage.setTitle("Estadísticas - Docente");
            estadisticasStage.setScene(new Scene(estadisticasView));
            estadisticasStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
    
    private void loadModule(String modulo){
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
}
