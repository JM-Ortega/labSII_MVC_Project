package co.unicauca.workflow.degree_ptoject;

import java.io.IOException;
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

    private static Scene scene;
    
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

        scene = new Scene(root); 
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
    
    public static void setRoot(String fxml, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(main.class.getResource(
            "/co/unicauca/workflow/degree_ptoject/view/" + fxml + ".fxml"
        ));
        Parent root = loader.load();

        if (fxml.equals("signin")) {
            SigninController signinCtrl = loader.getController();

            IUserRepository repo = Factory.getInstance().getRepository("default");
            IPasswordHasher hasher = new Argon2PasswordHasher();
            UserService userService = new UserService(repo, hasher);

            ISignInService signInService = userService;
            IRegistrationService registrationService = userService;

            signinCtrl.setServices(registrationService, signInService);
        }

        Stage stage = (Stage) scene.getWindow();
        stage.setTitle(title);
        scene.setRoot(root);
    }


    private static Parent loadFXML(String fxml) throws IOException {
      FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource("/co/unicauca/workflow/degree_ptoject/view/" + fxml + ".fxml"));
      return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
