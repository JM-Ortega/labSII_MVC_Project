package co.unicauca.workflow.degree_ptoject.presentation;

import co.unicauca.workflow.degree_ptoject.main;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Maryuri
 */
public class EstudianteController implements Initializable {

    @FXML
    private Button btnPrincipal;
    @FXML
    private Button btnFormatoA;
    @FXML
    private Button btnSalir;
    @FXML
    private Label nombreDocente;
    @FXML
    private StackPane stackPane;
    @FXML
    private Pane pnPrincipal;
    @FXML
    private Pane pnFormatoA;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnPrincipal.getStyleClass().add("btn-pressed");
        btnFormatoA.getStyleClass().add("btn-default");
        btnSalir.getStyleClass().add("btn-default");
        pnPrincipal.getStyleClass().add("Principal_container");
        stackPane.getStyleClass().add("stack_container");
        showPane(pnPrincipal);
    }

    @FXML
    private void switchToLogin(ActionEvent event) {
        try {
            main.navigate("signin", "Login");
            btnSalir.getStyleClass().add("btn-pressed");
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
        showPane(pnPrincipal);
    }

    @FXML
    private void showInfoFormatA(ActionEvent event) {
        btnFormatoA.getStyleClass().add("btn-pressed");
        btnPrincipal.getStyleClass().remove("btn-pressed");
        btnPrincipal.getStyleClass().add("btn-default");
        showPane(pnFormatoA);
    }

    private void showPane(Pane pane) {
        stackPane.getChildren().forEach(node -> node.setVisible(false));
        pane.setVisible(true);
    }
}
