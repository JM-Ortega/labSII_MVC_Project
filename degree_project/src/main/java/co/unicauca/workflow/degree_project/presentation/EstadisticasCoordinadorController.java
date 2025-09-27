package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.IProyectoService;
import co.unicauca.workflow.degree_project.domain.services.ObserverCoordinador;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;

public class EstadisticasCoordinadorController implements Initializable, ObserverCoordinador{
    @FXML 
    private BarChart<String, Number> BarChartEstadisticas;
    private XYChart.Series<String, Number> seriesTesis;
    private XYChart.Series<String, Number> seriesPractica;
    private IProyectoService proyectoService;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        seriesTesis = new XYChart.Series();
        seriesTesis.setName("TESIS");
        seriesPractica = new XYChart.Series();
        seriesPractica.setName("PRACTICA PROFESIONAL");
        BarChartEstadisticas.getData().addAll(seriesTesis, seriesPractica);
        cargarEstadisticasCoordinador();
    }
    
    private void cargarEstadisticasCoordinador() {
        // Para Tesis (FORMATO_A)
        seriesTesis.getData().add(new XYChart.Data("OBSERVADO", obtenerArchCantidad("TESIS", "OBSERVADO")));
        seriesTesis.getData().add(new XYChart.Data("PENDIENTE", obtenerArchCantidad("TESIS", "PENDIENTE")));

        // Para Práctica (FORMATO_B o el que corresponda en tu enum)
        seriesPractica.getData().add(new XYChart.Data("OBSERVADO", obtenerArchCantidad("PRACTICA_PROFESIONAL", "OBSERVADO")));
        seriesPractica.getData().add(new XYChart.Data("PENDIENTE", obtenerArchCantidad("PRACTICA_PROFESIONAL", "PENDIENTE")));
    }
    
    public int obtenerArchCantidad(String tipoProyecto, String estado) {
        return proyectoService.countArchivosByProyectoYEstado(tipoProyecto, estado);
    }
    
    public void setService(IProyectoService proyectoService) {
        this.proyectoService = proyectoService;
        this.proyectoService.addObserverCoordinador(this);
    }

    @Override
    public void updateEstadisticasCoordinador() {
        cargarEstadisticasCoordinador();
    }
}
