package co.unicauca.workflow.degree_project.presentation.dto;

/**
 *
 * @author Maryuri
 */
public class ProjectArchivoDTO {
    private String tipoProyecto;
    private String titulo;
    private String fechaEmision;
    private String estado;
    private int version;
    private byte[] contenido;

    public ProjectArchivoDTO(){}
    
    public ProjectArchivoDTO(String tipoProyecto, String titulo, String fechaEmision, String estado, int version) {
        this.tipoProyecto = tipoProyecto;
        this.titulo = titulo;
        this.fechaEmision = fechaEmision;
        this.estado = estado;
        this.version = version;
    }

    public String getTipo() { return tipoProyecto; }
    public void setTipo(String tipoProyecto) { this.tipoProyecto = tipoProyecto; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(String fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getTipoProyecto() {
        return tipoProyecto;
    }

    public void setTipoProyecto(String tipoProyecto) {
        this.tipoProyecto = tipoProyecto;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
    }
    
}
