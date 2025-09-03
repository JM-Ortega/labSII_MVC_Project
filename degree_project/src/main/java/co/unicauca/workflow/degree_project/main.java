package co.unicauca.workflow.degree_project;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import co.unicauca.workflow.degree_project.access.Factory;
import co.unicauca.workflow.degree_project.access.IUserRepository;
import co.unicauca.workflow.degree_project.domain.services.IPasswordHasher;
import co.unicauca.workflow.degree_project.domain.services.IRegistrationService;
import co.unicauca.workflow.degree_project.domain.services.ISignInService;
import co.unicauca.workflow.degree_project.domain.services.UserService;
import co.unicauca.workflow.degree_project.infra.security.Argon2PasswordHasher;
import co.unicauca.workflow.degree_project.presentation.RegisterController;
import co.unicauca.workflow.degree_project.presentation.SigninController;
import co.unicauca.workflow.degree_project.presentation.DocenteController;
import co.unicauca.workflow.degree_project.presentation.EstudianteController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class main extends Application {

    // --- Stage/Scene únicos ---
    private static Stage primaryStage;
    private static Scene scene;

    // --- Servicios únicos para toda la app ---
    private static IUserRepository repo;
    private static IPasswordHasher hasher;
    private static UserService userService;
    private static ISignInService signInService;
    private static IRegistrationService registrationService;

    @Override
    public void start(Stage stage) throws Exception {
        // 1) Composición de dependencias
        repo = Factory.getInstance().getRepository("default");
        hasher = new Argon2PasswordHasher();
        userService = new UserService(repo, hasher);
        signInService = userService;
        registrationService = userService;

        // 2) Guarda el stage principal
        primaryStage = stage;

        // 3) Carga vista inicial
        Parent root = loadFXML("/co/unicauca/workflow/degree_project/view/signin.fxml");

        // 4) Configura escena/ventana
        scene = new Scene(root);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    // Cambia el root de la escena a /view/{name}.fxml
    public static void setRoot(String name) throws IOException {
        String path = "/co/unicauca/workflow/degree_project/view/" + name + ".fxml";
        Parent newRoot = loadFXML(path);
        scene.setRoot(newRoot);
    }

    //Navega, cambia título y AJUSTA el tamaño al definido en el FXML destino.
    public static void navigate(String name, String title) throws IOException {
        setRoot(name);
        if (primaryStage != null) {
            
            scene.getRoot().applyCss();
            scene.getRoot().autosize();
            primaryStage.sizeToScene();
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
        }
    }

    //Carga FXML e INYECTA servicios si el controlador los necesita.
    private static Parent loadFXML(String absolutePath) throws IOException {
        FXMLLoader loader = new FXMLLoader(main.class.getResource(absolutePath));
        loader.setControllerFactory(type -> {
            try {
                Object controller = type.getDeclaredConstructor().newInstance();

                switch (controller) {
                    case SigninController sc -> sc.setServices(signInService);
                    case RegisterController rc -> rc.setServices(registrationService);
                    default -> {
                    }
                }
                return controller;
            } catch (InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("No se pudo crear el controlador: " + type, e);
            }
        });
        return loader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
