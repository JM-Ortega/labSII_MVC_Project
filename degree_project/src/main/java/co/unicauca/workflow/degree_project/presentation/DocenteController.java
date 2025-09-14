package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.main;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    @FXML private Label nombreDocente;
    @FXML private BorderPane bp;
    @FXML private AnchorPane ap;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnPrincipal.getStyleClass().add("btn-pressed");
        btnFormatoA.getStyleClass().add("btn-default");
        btnSalir.getStyleClass().add("btn-default");
    }

    @FXML
    void switchToLogin(ActionEvent event) {
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
        bp.setCenter(ap);
    }

    @FXML
    private void showInfoFormatoA(ActionEvent event) {
        btnFormatoA.getStyleClass().add("btn-pressed");
        btnPrincipal.getStyleClass().remove("btn-pressed");
        btnPrincipal.getStyleClass().add("btn-default");
        loadModule("/co/unicauca/workflow/degree_project/view/FormatoA");
    }
    
    private void loadModule(String modulo) {
     Parent root = null;
     
     try{
        root = FXMLLoader.load(getClass().getResource(modulo+".fxml"));

     }catch(IOException ex){
         Logger.getLogger(DocenteController.class.getName()).log(Level.SEVERE, null, ex);
     }
     bp.setCenter(root);
    }
}

