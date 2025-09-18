package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.IUserService;
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
public class FormatoADocenteController implements Initializable {
    
    @FXML private Label nombreDocente; 

    private IUserService service;
    private String email;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void setEmail(String email) {
        this.email = email;
    }

    public void setService(IUserService service) {
        this.service = service;
    }
    
    public void cargarDatos() {
        if (service != null && email != null) {
            String nombreCompleto = service.getName(email);
            nombreDocente.setText(nombreCompleto);
        }
    }
    
}
