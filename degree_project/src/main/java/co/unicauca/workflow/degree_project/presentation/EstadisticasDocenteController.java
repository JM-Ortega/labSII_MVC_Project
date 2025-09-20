package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.IProyectoService;
import co.unicauca.workflow.degree_project.domain.services.Observer;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;

public class EstadisticasDocenteController implements Initializable, Observer{
    
    @FXML private BarChart<String, Number> BarChartEstadisticas;
    private IProyectoService proyectoService;
    
    public EstadisticasDocenteController(IProyectoService proyectoService) {
        this.proyectoService = proyectoService;
        this.proyectoService.addObserver(this);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarEstadisticasFormatoA();
        
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

    @Override
    public void update() {
        cargarEstadisticasFormatoA();
    }

}
