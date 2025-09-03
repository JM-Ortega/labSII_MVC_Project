package co.unicauca.workflow.degree_ptoject.presentation;

import co.unicauca.workflow.degree_ptoject.domain.services.ISignInService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SigninController {

    @FXML
    private TextField txtCorreo;
    @FXML
    private PasswordField txtConrtaseña;

    private final ISignInService authService = null;
     
    @FXML
    private void ingresar(){
        if(txtCorreo.getText().trim().isEmpty()|| txtConrtaseña.getText().trim().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Campos incompletos");
            alert.setContentText("Por favor, rellene todos los campos.");
            alert.showAndWait();
        }else{
            String usuario = txtCorreo.getText().trim().toLowerCase();
            char[] passwordIngresada = txtConrtaseña.getText().toCharArray();

            try{
                int answer = authService.validacion(usuario, passwordIngresada);

                switch (answer) {
                    case 0 -> {
                        Alert alerta = new Alert(Alert.AlertType.WARNING);
                        alerta.setTitle("Credenciales inválidas");
                        alerta.setHeaderText(null);
                        alerta.setContentText("No fue posible ingresar, usuario o contraseña incorrectos.");
                        alerta.showAndWait();
                    }
                    case 1 -> {
                        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                        alerta.setTitle("Correcto");
                        alerta.setHeaderText(null);
                        alerta.setContentText("Inicio de sesión como estudiante exitoso.");
                        alerta.showAndWait();

                        //main.setRoot("estudiate");
                    }
                    case 2 -> {
                        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                        alerta.setTitle("Correcto");
                        alerta.setHeaderText(null);
                        alerta.setContentText("Inicio de sesión como docente exitoso.");
                        alerta.showAndWait();

                        //main.setRoot("docente");
                    }
                    default -> {
                        Alert alerta = new Alert(Alert.AlertType.ERROR);
                        alerta.setTitle("Error");
                        alerta.setHeaderText(null);
                        alerta.setContentText("El usuario no tiene un rol asociado.");
                        alerta.showAndWait();
                    }
                }
            }finally {
                java.util.Arrays.fill(passwordIngresada, '\0');
            }
        }    
    }
}
