package co.unicauca.workflow.degree_project.domain.models;

/**
 *
 * @author Maryuri
 */
public class Archivo {
    private int id;
    private int version;
    private String nombreArchivo;
    private String tipoArchivo;
    private String estadoArchivo;
    private String fechaPublicacion;
    private byte[] contenido;
    
    private Project proyecto;
    
    public Archivo(){}

    public Archivo(int id, int version, String nombreArchivo, String tipoArchivo, String estadoArchivo, String fechaPublicacion) {
        this.id = id;
        this.version = version;
        this.nombreArchivo = nombreArchivo;
        this.tipoArchivo = tipoArchivo;
        this.estadoArchivo = estadoArchivo;
        this.fechaPublicacion = fechaPublicacion;
    }
    
    public Project getProyecto() {
        return proyecto;
    }

    public void setProyecto(Project proyecto) {
        this.proyecto = proyecto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
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

    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(String fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
    }
    
}
