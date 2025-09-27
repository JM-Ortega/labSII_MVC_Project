package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.domain.services.ISignInService;
import co.unicauca.workflow.degree_project.domain.services.IUserService;
import co.unicauca.workflow.degree_project.infra.security.Sesion;
import co.unicauca.workflow.degree_project.main;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;

public class SigninController {

    @FXML
    private TextField txtCorreo;
    @FXML
    private PasswordField txtConrtaseña;

    private ISignInService authService;
    private IUserService userService;

    public void setServices(ISignInService signInService) {
        this.authService = signInService;
    }

    @FXML
    private void ingresar() throws IOException{
        if (txtCorreo.getText().trim().isEmpty() || txtConrtaseña.getText().trim().isEmpty()) {
            alerta(Alert.AlertType.ERROR, "Error", "Campos incompletos", "Por favor, rellene todos los campos.");
            return;
        }
        if (authService == null) {
            alerta(Alert.AlertType.ERROR, "Error", null, "El servicio de autenticación no está disponible.");
            return;
        }
        String usuario = txtCorreo.getText().trim().toLowerCase();
        char[] passwordIngresada = txtConrtaseña.getText().toCharArray();

        try {
          Optional<AuthResult> maybeAuth = authService.validarSesion(usuario, passwordIngresada);

            if (maybeAuth.isEmpty()) {
                alerta(Alert.AlertType.WARNING, "Credenciales inválidas", null,
                        "No fue posible ingresar, usuario o contraseña incorrectos.");
                return;
            }

            AuthResult auth = maybeAuth.get();
            Sesion.getInstancia().setUsuarioActual(auth);

            switch (auth.rol()) {
                case "Estudiante" -> {
                    alerta(Alert.AlertType.INFORMATION, "Correcto", null,
                            "Inicio de sesión como estudiante exitoso.");
                      try {
                        Object controller = main.navigateWithController("Estudiante", "Panel Estudiante");
                         if (controller instanceof EstudianteController ec) {
                             // 1) Pasa los servicios que Docente va a usar
                            if (userService != null) {
                                ec.setUserService(userService);
                            } else if (authService instanceof IUserService us) {
                                ec.setUserService(us);
                            }
                            ec.cargarDatos();
                        }
                    } catch (IOException e) {
                        alerta(Alert.AlertType.ERROR, "Error", null, "Error al abrir la vista de docente.");
                        e.printStackTrace();
                    }
                }
                case "Docente" -> {
                    alerta(Alert.AlertType.INFORMATION, "Correcto", null,
                            "Inicio de sesión como docente exitoso.");
                    try {
                        Object controller = main.navigateWithController("Docente", "Panel Docente");
                         if (controller instanceof DocenteController dc) {
                             // 1) Pasa los servicios que Docente va a usar
                            if (userService != null) {
                                dc.setUserService(userService);
                            } else if (authService instanceof IUserService us) {
                                dc.setUserService(us);
                            }
                            dc.cargarDatos();
                        }
                    } catch (IOException e) {
                        alerta(Alert.AlertType.ERROR, "Error", null, "Error al abrir la vista de docente.");
                        e.printStackTrace();
                    }
                }

                case "Coordinador" -> {
                    alerta(Alert.AlertType.INFORMATION, "Correcto", null, "Inicio de sesión como coordinador exitoso.");
                    try {
                        Object controller = main.navigateWithController("Coordinador", "Panel Coordinador");
                        if (controller instanceof DocenteController dc) {
                            // 1) Pasa los servicios que Docente va a usar
                            if (userService != null) {
                                dc.setUserService(userService);
                            } else if (authService instanceof IUserService us) {
                                dc.setUserService(us);
                            }
                            dc.cargarDatos();
                        }
                    } catch (IOException e) {
                        alerta(Alert.AlertType.ERROR, "Error", null, "Error al abrir la vista del coordinador.");
                        e.printStackTrace();
                    }
                }
                default -> {
                    alerta(Alert.AlertType.ERROR, "Error", null, "El usuario no tiene un rol asociado.");
                }
            }
        } finally {
            java.util.Arrays.fill(passwordIngresada, '\0'); // higiene de memoria
        }
    }

    @FXML
    private void goToRegister() {
        try {
            main.navigate("register", "Registro");
        } catch (IOException e) {
            alerta(Alert.AlertType.ERROR, "Error", null, "No se pudo abrir la vista de registro.");
        }
    }

    private void alerta(Alert.AlertType type, String title, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
