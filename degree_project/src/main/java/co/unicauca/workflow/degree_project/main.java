package co.unicauca.workflow.degree_project;

import co.unicauca.workflow.degree_project.access.Factory;
import co.unicauca.workflow.degree_project.access.IArchivoRepository;
import co.unicauca.workflow.degree_project.access.IProyectoRepository;
import co.unicauca.workflow.degree_project.access.IUserRepository;
import co.unicauca.workflow.degree_project.domain.services.*;
import co.unicauca.workflow.degree_project.infra.security.Argon2PasswordHasher;
import co.unicauca.workflow.degree_project.presentation.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Objects;

public class main extends Application {
  private static Stage primaryStage;
  private static Scene scene;

  // --- Servicios únicos (composition root) ---
  private static IUserRepository repo;
  private static IPasswordHasher hasher;
  private static UserService userService;
  private static ISignInService signInService;
  private static IRegistrationService registrationService;
  private static IProyectoRepository proyectoRepo;
  private static IArchivoRepository archivoRepo;
  private static IProyectoService proyectoService;

  @Override
  public void start(Stage stage) throws Exception {
    // Composición de dependencias
    var f = Factory.getInstance();
    repo         = f.getRepository("default");
    proyectoRepo = f.getProyectoRepository("default");
    archivoRepo  = f.getArchivoRepository("default");

    proyectoService     = new ProyectoService(proyectoRepo, archivoRepo, f.getConnection());
    hasher              = new Argon2PasswordHasher();
    userService         = new UserService(repo, hasher);
    signInService       = userService;
    registrationService = userService;

    primaryStage = stage;

    // Vista inicial
    Parent root = loadView("/co/unicauca/workflow/degree_project/view/signin.fxml");
    scene = new Scene(root);
    stage.setTitle("Login");
    stage.setScene(scene);
    stage.setResizable(false);
    stage.sizeToScene();
    stage.centerOnScreen();
    stage.show();
  }

  public static void setRoot(String name) throws IOException {
    String path = "/co/unicauca/workflow/degree_project/view/" + name + ".fxml";
    Parent newRoot = loadView(path);
    scene.setRoot(newRoot);
  }

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

  public static Object navigateWithController(String name, String title) throws IOException {
    String path = "/co/unicauca/workflow/degree_project/view/" + name + ".fxml";
    FXMLLoader loader = newInjectedLoader(path);
    Parent root = loader.load();
    Object controller = loader.getController();

    scene.setRoot(root);
    if (primaryStage != null) {
      scene.getRoot().applyCss();
      scene.getRoot().autosize();
      primaryStage.sizeToScene();
      primaryStage.setTitle(title);
      primaryStage.centerOnScreen();
    }
    return controller;
  }

  private static Parent loadView(String absolutePath) throws IOException {
    FXMLLoader loader = newInjectedLoader(absolutePath);
    return loader.load();
  }

  public static FXMLLoader newInjectedLoader(String path) {
    URL url = Objects.requireNonNull(main.class.getResource(path),
        () -> "FXML no encontrado: " + path);

    FXMLLoader loader = new FXMLLoader(url);
    loader.setControllerFactory(type -> {
      try {
        Constructor<?> ctor;
        try {
          ctor = type.getDeclaredConstructor(IProyectoService.class);
          return ctor.newInstance(proyectoService);
        } catch (NoSuchMethodException ignored) {}

        try {
          ctor = type.getDeclaredConstructor(ISignInService.class, IRegistrationService.class);
          return ctor.newInstance(signInService, registrationService);
        } catch (NoSuchMethodException ignored) {}

        Object controller = type.getDeclaredConstructor().newInstance();

        if (controller instanceof SigninController sc)              sc.setServices(signInService);
        if (controller instanceof RegisterController rc)            rc.setServices(registrationService);
        if (controller instanceof FormatoADocenteController fdc)    fdc.setService(proyectoService);
        if (controller instanceof FormatoAEstudianteController fec) fec.setService(proyectoService);
        if (controller instanceof EstadisticasDocenteController ed) ed.setService(proyectoService);
        if (controller instanceof EstadisticasCoordinadorController ec) ec.setService(proyectoService);
        if (controller instanceof Co_Proyecto_Controller cop)       cop.setService(proyectoService);
        if (controller instanceof Co_Observaciones_Controller cob)  cob.setService(proyectoService);

        return controller;

      } catch (InstantiationException | IllegalAccessException |
               InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException("No se pudo crear el controlador: " + type, e);
      }
    });
    return loader;
  }

  public static void main(String[] args) {
    launch(args);
  }
}
