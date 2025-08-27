package co.unicauca.workflow.degree_ptoject.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

  @FXML private TextField txtUsuario;
  @FXML private PasswordField txtPassword;
  @FXML private Label lblError;

  @FXML
  private void handleLogin() {
    String u = txtUsuario.getText();
    String p = txtPassword.getText();

    if (u == null || u.isBlank()) { setError("Ingresa el usuario."); return; }
    if (p == null || p.isBlank()) { setError("Ingresa la contraseña."); return; }

    if (u.equals("admin") && p.equals("1234")) {
      setError("");
      var ok = new Alert(Alert.AlertType.INFORMATION);
      ok.setHeaderText(null);
      ok.setContentText("¡Bienvenido, " + u + "!");
      ok.showAndWait();
      // TODO: aquí puedes navegar a otra vista
    } else {
      setError("Credenciales inválidas.");
    }
  }

  private void setError(String msg) {
    if (lblError != null) lblError.setText(msg);
  }
}
