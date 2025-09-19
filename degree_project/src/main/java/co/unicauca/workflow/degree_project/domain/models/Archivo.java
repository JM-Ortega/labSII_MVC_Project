package co.unicauca.workflow.degree_project.domain.models;

/**
 *
 * @author Maryuri
 */
public class Archivo {

    private long id;
    private long proyectoId;
    private TipoArchivo tipo;
    private int nroVersion;
    private String nombreArchivo;
    private String fechaSubida;
    private byte[] blob;
    private EstadoArchivo estado;

    public Archivo() {
    }

    public Archivo(long id, long proyectoId, TipoArchivo tipo, int nroVersion,
                   String nombreArchivo, String fechaSubida, byte[] blob, EstadoArchivo estado) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.tipo = tipo;
        this.nroVersion = nroVersion;
        this.nombreArchivo = nombreArchivo;
        this.fechaSubida = fechaSubida;
        this.blob = blob;
        this.estado = estado;
    }

    // Getters y Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(long proyectoId) {
        this.proyectoId = proyectoId;
    }

    public TipoArchivo getTipo() {
        return tipo;
    }

    public void setTipo(TipoArchivo tipo) {
        this.tipo = tipo;
    }

    public int getNroVersion() {
        return nroVersion;
    }

    public void setNroVersion(int nroVersion) {
        this.nroVersion = nroVersion;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(String fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public byte[] getBlob() {
        return blob;
    }

    public void setBlob(byte[] blob) {
        this.blob = blob;
    }

    public EstadoArchivo getEstado() {
        return estado;
    }

    public void setEstado(EstadoArchivo estado) {
        this.estado = estado;
    }
}
