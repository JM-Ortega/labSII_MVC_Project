package co.unicauca.workflow.degree_project.domain.services;

import co.unicauca.workflow.degree_project.domain.models.Archivo;
import co.unicauca.workflow.degree_project.domain.models.EstadoProyecto;
import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.models.TipoArchivo;

import java.util.List;

public interface IProyectoService extends ObservableService{
    Proyecto crearProyectoConFormatoA(Proyecto proyecto, Archivo archivo);

    long crearProyectoConArchivos(Proyecto proyecto, List<Archivo> archivos);

    Archivo subirNuevaVersionFormatoA(long proyectoId, Archivo archivo);

    void subirCartaAceptacion(long proyectoId, Archivo carta);

    boolean docenteTieneCupo(String docenteId);

    boolean estudianteLibre(String estudianteId);

    int maxVersionFormatoA(long proyectoId);

    boolean canResubmit(long proyectoId);

    List<Proyecto> listarProyectosDocente(String docenteId, String filtro);

    Archivo obtenerArchivo(long archivoId);

    List<Archivo> listarArchivosPorProyecto(long proyectoId, TipoArchivo tipo);

    boolean estudianteLibrePorCorreo(String correo);

    boolean tieneObservacionesFormatoA(long proyectoId);

    Archivo obtenerUltimoFormatoAConObservaciones(long proyectoId);

    EstadoProyecto enforceAutoCancelIfNeeded(long proyectoId);

    List<Archivo> listarTodosArchivos();

    Proyecto buscarProyectoPorId(long proyectoId);

    void actualizarEstadoProyecto(long proyectoId, EstadoProyecto nuevoEstado);

    String obtenerNombreDocente(String docenteId);

    String obtenerCorreoDocente(String docenteId);
    
    String obtenerCorreoEstudiante(String estudianteId);

    Archivo obtenerFormatoA(long proyectoId);

    int subirObservacion (long proyectoId, Archivo archivo, String correoProfesor, String correoEstudiante);

    int countArchivosByProyectoYEstado(String tipoProyecto, String estadoArchivo);

    void addObserverCoordinador(ObserverCoordinador o);

    void notifyCoordinadores();

    int countProyectosByEstadoYTipo(String tipo, String estado, String idDocente);
    
    List<Proyecto> listarFormatosAPorEstudiante(String estudianteId);
}
