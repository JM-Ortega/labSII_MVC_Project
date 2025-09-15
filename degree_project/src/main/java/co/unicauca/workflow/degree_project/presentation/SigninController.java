package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.models.User;
import co.unicauca.workflow.degree_project.domain.services.ISignInService;
import co.unicauca.workflow.degree_project.domain.services.IUserService;
import co.unicauca.workflow.degree_project.main;
import java.io.IOException;
import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SigninController {

    @FXML
    private TextField txtCorreo;
    @FXML
    private PasswordField txtConrtaseña;

    private ISignInService authService;

    public void setServices(ISignInService signInService) {
        this.authService = signInService;
    }

    @FXML
    private void ingresar() throws IOException {
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
                    User.setEmail(usuario);

                  try {
                        Object controller = main.navigateWithController("Estudiante", "Panel Estudiante");

                        if (controller instanceof EstudianteController dc) {
                            dc.setService((IUserService) authService);
                            dc.setEmail(usuario);
                            dc.cargarDatos();
                        }
                    } catch (IOException e) {
                        new Alert(Alert.AlertType.ERROR, "Error al abrir la vista de estudiante.").showAndWait();
                        e.printStackTrace();
                    }
                }
                case 2 -> {
                    Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                    alerta.setTitle("Correcto");
                    alerta.setHeaderText(null);
                    alerta.setContentText("Inicio de sesión como docente exitoso.");
                    alerta.showAndWait();
                    
                    try {
                        Object controller = main.navigateWithController("Docente", "Panel Docente");

                        if (controller instanceof DocenteController dc) {
                            dc.setService((IUserService) authService);
                            dc.setEmail(usuario);
                            dc.cargarDatos();
                        }
                    } catch (IOException e) {
                        new Alert(Alert.AlertType.ERROR, "Error al abrir la vista de docente.").showAndWait();
                        e.printStackTrace();
                    }
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
            main.navigate("register", "Registro");
        } catch (IOException e) {
            /* mostrar alerta */ }
    }

}
