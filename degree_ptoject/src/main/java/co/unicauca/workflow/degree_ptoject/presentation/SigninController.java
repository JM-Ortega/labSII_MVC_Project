package co.unicauca.workflow.degree_ptoject.presentation;

import co.unicauca.workflow.degree_ptoject.domain.services.IRegistrationService;
import co.unicauca.workflow.degree_ptoject.domain.services.ISignInService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SigninController {

    @FXML
    private TextField txtCorreo;
    @FXML
    private PasswordField txtConrtaseña;

    private ISignInService authService;
    private IRegistrationService registrationService;


    public void setServices(IRegistrationService registrationService, ISignInService signInService) {
        this.registrationService = registrationService;
        this.authService = signInService;
    }

    @FXML
    private void ingresar() {
        if (txtCorreo.getText().trim().isEmpty() || txtConrtaseña.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Campos incompletos");
            alert.setContentText("Por favor, rellene todos los campos.");
            alert.showAndWait();
            return;
        }

        if (authService == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("El servicio de autenticación no está disponible.");
            alert.showAndWait();
            return;
        }

        String usuario = txtCorreo.getText().trim().toLowerCase();
        char[] passwordIngresada = txtConrtaseña.getText().toCharArray();

        try {
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
                    // TODO: navegar a la vista de estudiante
                }
                case 2 -> {
                    Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                    alerta.setTitle("Correcto");
                    alerta.setHeaderText(null);
                    alerta.setContentText("Inicio de sesión como docente exitoso.");
                    alerta.showAndWait();
                    // TODO: navegar a la vista de docente
                }
                default -> {
                    Alert alerta = new Alert(Alert.AlertType.ERROR);
                    alerta.setTitle("Error");
                    alerta.setHeaderText(null);
                    alerta.setContentText("El usuario no tiene un rol asociado.");
                    alerta.showAndWait();
                }
            }
        } finally {
            java.util.Arrays.fill(passwordIngresada, '\0');
        }
    }


    @FXML
    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/unicauca/workflow/degree_ptoject/view/register.fxml")
            );
            Parent root = loader.load();

            RegisterController regCtrl = loader.getController();
            regCtrl.setServices(this.registrationService, this.authService);

            Stage stage = (Stage) txtCorreo.getScene().getWindow();
            stage.setTitle("Registro");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No fue posible abrir la pantalla de registro.");
            alert.showAndWait();
            ex.printStackTrace();
        }
    }
}
