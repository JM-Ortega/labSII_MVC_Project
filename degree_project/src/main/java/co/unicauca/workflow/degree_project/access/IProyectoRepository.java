package co.unicauca.workflow.degree_project.access;

import co.unicauca.workflow.degree_project.domain.models.EstadoProyecto;
import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import java.util.List;

public interface IProyectoRepository {
    long crearProyecto(Proyecto proyecto);

    boolean existeProyecto(long proyectoId);

    String getEstadoProyecto(long proyectoId);

    int countProyectosEnTramiteDocente(String docenteId);

    boolean estudianteTieneProyectoEnTramite(String estudianteId);

    List<Proyecto> listarPorDocente(String docenteId, String filtro);

    void actualizarEstadoProyecto(long proyectoId, EstadoProyecto nuevoEstado);

    boolean existeEstudiante(String estudianteId);
    
    public int countProyectosByEstadoYTipo(String tipo, EstadoProyecto estado, String idDocente);
    //Coordinador
    Proyecto proyectoPorId(long proyectoId);
    String nombreDocente(String docenteId);
    String correoDocente(String docenteId);
    void update(Proyecto proyecto);
}
