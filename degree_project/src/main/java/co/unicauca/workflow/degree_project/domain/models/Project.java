package co.unicauca.workflow.degree_project.domain.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Maryuri
 */
public class Project {
    private int idEstudiante;
    private int idProfesor;
    private String estadoP;
    private String tipoProyecto;
    private String titulo;
    private Date fechaProyecto;
    private List<Archivo> archivos  = new ArrayList<>();
    
    public Project(){}

    public Project(int idEstudiante, int idProfesor, String estadoP, String tipoProyecto, String titulo, Date fechaProyecto) {
        this.idEstudiante = idEstudiante;
        this.idProfesor = idProfesor;
        this.estadoP = estadoP;
        this.tipoProyecto = tipoProyecto;
        this.titulo = titulo;
        this.fechaProyecto = fechaProyecto;
        this.archivos = new ArrayList<>();
    }
    
    public void agregarArchivo(Archivo archivo) {
        archivo.setProyecto(this); 
        archivos.add(archivo);
    }

    public List<Archivo> getArchivos() {
        return archivos;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(int idProfesor) {
        this.idProfesor = idProfesor;
    }

    public String getEstadoP() {
        return estadoP;
    }

    public void setEstadoP(String estadoP) {
        this.estadoP = estadoP;
    }

    public String getTipoProyecto() {
        return tipoProyecto;
    }

    public void setTipoProyecto(String tipoProyecto) {
        this.tipoProyecto = tipoProyecto;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Date getFechaProyecto() {
        return fechaProyecto;
    }

    public void setFechaProyecto(Date fechaProyecto) {
        this.fechaProyecto = fechaProyecto;
    }
    
}
