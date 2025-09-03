package co.unicauca.workflow.degree_ptoject;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class main extends Application {
    
    private static Scene scene;
    
  @Override
  public void start(Stage stage) throws Exception {
      scene = new Scene(loadFXML("login"));
      stage.setScene(scene);
      stage.show();
  }
  
  public static void setRoot(String fxml) throws IOException {
      scene.setRoot(loadFXML(fxml));
  }
  
  private static Parent loadFXML(String fxml) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource("/co/unicauca/workflow/degree_ptoject/view/" + fxml + ".fxml"));
    return fxmlLoader.load();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
