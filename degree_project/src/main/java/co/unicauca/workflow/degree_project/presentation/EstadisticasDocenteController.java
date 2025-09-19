package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.infra.security.Sesion;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author Maryuri
 */
public class EstadisticasDocenteController implements Initializable {
    
    @FXML private Label nombreDocente; 
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarDatos();
    }    
    
    public void cargarDatos() {
        AuthResult usuario = Sesion.getUsuarioActual();
        if(usuario != null){
            nombreDocente.setText(usuario.nombre());
        }else{
            System.err.println("No hay usuario en sesion");
        }
    }
}
