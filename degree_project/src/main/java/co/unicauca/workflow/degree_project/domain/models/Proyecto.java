package co.unicauca.workflow.degree_project.domain.models;

public class Proyecto {

    private long id;
    private TipoTrabajoGrado tipo;
    private EstadoProyecto estado;
    private String titulo;
    private String estudianteId;
    private String docenteId;
    private String fechaCreacion;
    private Archivo archivo;

    public Proyecto() {
    }

    public Proyecto(long id, TipoTrabajoGrado tipo, EstadoProyecto estado, String titulo,
                    String estudianteId, String docenteId, String fechaCreacion, Archivo archivo) {
        this.id = id;
        this.tipo = tipo;
        this.estado = estado;
        this.titulo = titulo;
        this.estudianteId = estudianteId;
        this.docenteId = docenteId;
        this.fechaCreacion = fechaCreacion;
        this.archivo = archivo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TipoTrabajoGrado getTipo() {
        return tipo;
    }

    public void setTipo(TipoTrabajoGrado tipo) {
        this.tipo = tipo;
    }

    public EstadoProyecto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProyecto estado) {
        this.estado = estado;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(String estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(String docenteId) {
        this.docenteId = docenteId;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Archivo getArchivo() {
        return archivo;
    }

    public void setArchivo(Archivo archivo) {
        this.archivo = archivo;
    }
}
