package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.domain.services.IProyectoService;
import co.unicauca.workflow.degree_project.infra.security.Sesion;
import co.unicauca.workflow.degree_project.main;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;

public class EstadisticasDocenteController implements Initializable {
    
    @FXML private Label nombreDocente; 
    @FXML private BarChart<String, Number> BarChartEstadisticas;
    private IProyectoService proyectoService;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarDatos();
        cargarEstadisticasFormatoA();
    }    
    
    public void cargarDatos() {
        AuthResult auth = Sesion.getInstancia().getUsuarioActual();
        if (auth != null) {
            nombreDocente.setText(auth.nombre());
        } else {
            System.err.println("No hay sesión activa; redirigiendo a login.");
            try {
                main.navigate("signin", "Login");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void cargarEstadisticasFormatoA() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Formato A");

        series.getData().add(new XYChart.Data<>("APROBADOS", obtenerCantidad("APROBADO")));
        series.getData().add(new XYChart.Data<>("OBSERVADOS", obtenerCantidad("OBSERVADO")));
        series.getData().add(new XYChart.Data<>("PENDIENTES", obtenerCantidad("PENDIENTE")));

        BarChartEstadisticas.getData().clear();
        BarChartEstadisticas.getData().add(series);
    }
    
    private int obtenerCantidad(String estado){
        return proyectoService.countArchivosByEstadoYTipo("FORMATO_A", estado);
    }
    
    public void setService(IProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

}
