package co.unicauca.workflow.degree_project.presentation;

import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.domain.services.IProyectoService;
import co.unicauca.workflow.degree_project.domain.services.Observer;
import co.unicauca.workflow.degree_project.infra.security.Sesion;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;

public class EstadisticasDocenteController implements Initializable, Observer{
    
    @FXML private BarChart<String, Number> BarChartEstadisticas;
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
        cargarEstadisticas();
    }
    
    private void cargarEstadisticas() {
        seriesTesis.getData().add(new XYChart.Data("TERMINADOS", obtenerCantidad("TESIS", "TERMINADO")));
        seriesTesis.getData().add(new XYChart.Data("RECHAZADOS", obtenerCantidad("TESIS", "RECHAZADO")));
        seriesTesis.getData().add(new XYChart.Data("EN TRAMITE", obtenerCantidad("TESIS", "EN_TRAMITE")));

        seriesPractica.getData().add(new XYChart.Data("TERMINADOS", obtenerCantidad("PRACTICA_PROFESIONAL", "TERMINADO")));
        seriesPractica.getData().add(new XYChart.Data("RECHAZADOS", obtenerCantidad("PRACTICA_PROFESIONAL", "RECHAZADO")));
        seriesPractica.getData().add(new XYChart.Data("EN TRAMITE", obtenerCantidad("PRACTICA_PROFESIONAL", "EN_TRAMITE")));

    }
    
    private int obtenerCantidad(String tipo, String estado){
        AuthResult auth = Sesion.getInstancia().getUsuarioActual();
        return proyectoService.countProyectosByEstadoYTipo(tipo, estado, auth.userId());
    }
    
    public void setService(IProyectoService proyectoService) {
        this.proyectoService = proyectoService;
        this.proyectoService.addObserver(this);
    }

    @Override
    public void update() {
        cargarEstadisticas();
    }
}
