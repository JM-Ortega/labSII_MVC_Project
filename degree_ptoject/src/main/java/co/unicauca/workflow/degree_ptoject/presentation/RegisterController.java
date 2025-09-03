package co.unicauca.workflow.degree_ptoject.presentation;

import co.unicauca.workflow.degree_ptoject.domain.models.Programa;
import co.unicauca.workflow.degree_ptoject.domain.models.Rol;
import co.unicauca.workflow.degree_ptoject.domain.services.IRegistrationService;
import co.unicauca.workflow.degree_ptoject.domain.services.ISignInService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    private IRegistrationService registrationService;
    private ISignInService signInService;

    public void setServices(IRegistrationService registrationService, ISignInService signInService) {
        this.registrationService = registrationService;
        this.signInService = signInService;
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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/unicauca/workflow/degree_ptoject/view/signin.fxml")
            );
            Parent root = loader.load();

            SigninController sc = loader.getController();
            sc.setServices(this.registrationService, this.signInService);

            Stage stage = (Stage) txtNombres.getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception ex) {
            showError(errGeneral, "No fue posible abrir la pantalla de login.");
            ex.printStackTrace();
        }
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
