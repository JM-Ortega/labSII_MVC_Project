package co.unicauca.workflow.degree_ptoject;

import co.unicauca.workflow.degree_ptoject.access.Factory;
import co.unicauca.workflow.degree_ptoject.access.IUserRepository;
import co.unicauca.workflow.degree_ptoject.domain.services.IPasswordHasher;
import co.unicauca.workflow.degree_ptoject.domain.services.IRegistrationService;
import co.unicauca.workflow.degree_ptoject.domain.services.ISignInService;
import co.unicauca.workflow.degree_ptoject.domain.services.UserService;
import co.unicauca.workflow.degree_ptoject.infra.security.Argon2PasswordHasher;
import co.unicauca.workflow.degree_ptoject.presentation.SigninController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Composición de dependencias
        IUserRepository repo = Factory.getInstance().getRepository("default");
        IPasswordHasher hasher = new Argon2PasswordHasher();
        UserService userService = new UserService(repo, hasher);

        ISignInService signInService = userService;
        IRegistrationService registrationService = userService;

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/co/unicauca/workflow/degree_ptoject/view/signin.fxml")
        );
        Parent root = loader.load();

        SigninController signinCtrl = loader.getController();
        signinCtrl.setServices(registrationService, signInService);

        stage.setTitle("Login");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
