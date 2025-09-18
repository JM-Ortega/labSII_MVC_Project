package co.unicauca.workflow.degree_project.domain.models;

import java.sql.Blob;
import java.util.Date;

public class Archivo {
    private int id;
    private int idProyecto;
    private int version;
    private Blob pdf;
    private String tipoArchivo;
    private String estadoArchivo;
    private Date fechaPulicacion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(int idProyecto) {
        this.idProyecto = idProyecto;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Blob getPdf() {
        return pdf;
    }

    public void setPdf(Blob pdf) {
        this.pdf = pdf;
    }

    public String getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public String getEstadoArchivo() {
        return estadoArchivo;
    }

    public void setEstadoArchivo(String estadoArchivo) {
        this.estadoArchivo = estadoArchivo;
    }

    public Date getFechaPulicacion() {
        return fechaPulicacion;
    }

    public void setFechaPulicacion(Date fechaPulicacion) {
        this.fechaPulicacion = fechaPulicacion;
    }
    
    
}
