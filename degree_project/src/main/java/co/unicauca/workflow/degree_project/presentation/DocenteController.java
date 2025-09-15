package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.IUserService;
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
    
    private IUserService service;
    private String email;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnPrincipal.getStyleClass().add("btn-pressed");
        btnFormatoA.getStyleClass().add("btn-default");
        btnSalir.getStyleClass().add("btn-default");
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
     Parent root = null;
     
     try{
        root = FXMLLoader.load(getClass().getResource(modulo+".fxml"));

     }catch(IOException ex){
         Logger.getLogger(DocenteController.class.getName()).log(Level.SEVERE, null, ex);
     }
     bp.setCenter(root);
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
}

