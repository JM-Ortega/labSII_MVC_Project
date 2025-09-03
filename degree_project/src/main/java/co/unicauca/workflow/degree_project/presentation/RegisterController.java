package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.models.Programa;
import co.unicauca.workflow.degree_project.domain.models.Rol;
import co.unicauca.workflow.degree_project.domain.services.IRegistrationService;
import co.unicauca.workflow.degree_project.main;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class RegisterController {

    private IRegistrationService registrationService;

    public void setServices(IRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    // FXML
    @FXML
    private TextField txtNombres, txtApellidos, txtUsuario, txtCelular;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private ComboBox<Programa> cbPrograma;
    @FXML
    private ComboBox<Rol> cbRol;

    // @FXML
    //   private Label lblPwdHint;
    @FXML
    private Label errNombres, errApellidos, errUsuario, errPassword,
            errPrograma, errRol, errCelular, errGeneral;

    @FXML
    private void initialize() {

        cbPrograma.getItems().setAll(Programa.values());
        cbRol.getItems().setAll(Rol.values());
        if (!cbPrograma.getItems().isEmpty()) {
            cbPrograma.getSelectionModel().selectFirst();
        }
        if (!cbRol.getItems().isEmpty()) {
            cbRol.getSelectionModel().selectFirst();
        }

        txtCelular.setTextFormatter(new TextFormatter<>(change -> {
            String next = change.getControlNewText();
            if (next.length() > 10) {
                return null;
            }
            if (!next.matches("\\d*")) {
                return null;
            }
            return change;
        }));

    }

    @FXML
    private void handleRegister() {
        clearAllErrors();

        // Normalizar valores de UI
        String nombres = valueOrEmpty(txtNombres);
        String apellidos = valueOrEmpty(txtApellidos);
        String email = valueOrEmpty(txtUsuario).toLowerCase();
        String password = valueOrEmpty(txtPassword);
        Programa programa = cbPrograma.getValue();
        Rol rol = cbRol.getValue();
        String cel = valueOrEmpty(txtCelular);
        String celular = cel.isEmpty() ? null : cel; // opcional

        var res = registrationService.register(
                nombres, apellidos, email, password, programa, rol, celular
        );

        if (!res.ok()) {
            var fe = res.fieldErrors();
            if (fe != null && !fe.isEmpty()) {
                if (fe.containsKey("nombres")) {
                    showError(errNombres, fe.get("nombres"));
                }
                if (fe.containsKey("apellidos")) {
                    showError(errApellidos, fe.get("apellidos"));
                }
                if (fe.containsKey("email")) {
                    showError(errUsuario, fe.get("email"));
                }
                if (fe.containsKey("password")) {
                    showError(errPassword, fe.get("password"));
                }
                if (fe.containsKey("celular")) {
                    showError(errCelular, fe.get("celular"));
                }
                if (fe.containsKey("programa")) {
                    showError(errPrograma, fe.get("programa"));
                }
                if (fe.containsKey("rol")) {
                    showError(errRol, fe.get("rol"));
                }
            } else {
                showError(errGeneral, res.message());
            }
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("¡Registro exitoso!");
        alerta.setHeaderText(null);
        alerta.setContentText("Su registro se completo con exito.");
        alerta.showAndWait();

        goToLogin();
    }

    @FXML
    private void goToLogin() {
        try {
            main.navigate("signin", "Login");
        } catch (IOException e) {
            /* mostrar alerta */ }
    }

    // ===== Helpers =====
    private String valueOrEmpty(TextInputControl field) {
        String s = field.getText();
        return s == null ? "" : s.trim();
    }

    private void showError(Label lbl, String msg) {
        if (lbl == null) {
            return;
        }
        lbl.getStyleClass().remove("error-hidden");
        lbl.setText((msg == null || msg.isBlank()) ? " " : msg);
    }

    private void clearError(Label lbl) {
        if (lbl == null) {
            return;
        }
        if (!lbl.getStyleClass().contains("error-hidden")) {
            lbl.getStyleClass().add("error-hidden");
        }
        lbl.setText(" ");
    }

    private void clearAllErrors() {
        clearError(errNombres);
        clearError(errApellidos);
        clearError(errUsuario);
        clearError(errPassword);
        clearError(errPrograma);
        clearError(errRol);
        clearError(errCelular);
        clearError(errGeneral);
    }

}
