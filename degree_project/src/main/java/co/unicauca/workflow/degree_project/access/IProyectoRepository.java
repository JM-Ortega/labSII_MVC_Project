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
    
    int countProyectosByEstadoYTipo(String tipo, EstadoProyecto estado, String idDocente);

    Proyecto buscarProyectoPorId(long proyectoId);

    String nombreDocente(String docenteId);

    String correoDocente(String docenteId);
    
    String correoEstudainte(String estudianteId);

    void update(Proyecto proyecto);

    boolean existeEstudiantePorCorreo(String correo);

    String getEstudianteIdPorCorreo(String correo);

    boolean estudianteTieneProyectoEnTramitePorCorreo(String correo);
}
