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


    public boolean tieneObservacionesFormatoA(long proyectoId);

    public Archivo obtenerUltimoFormatoAConObservaciones(long proyectoId);


    public EstadoProyecto enforceAutoCancelIfNeeded(long proyectoId);
    
    
    public int countProyectosByEstadoYTipo(String tipo, String estado, String idDocente);
    
    public List<Proyecto> listarFormatosAPorEstudiante(String estudianteId);
    
    Proyecto buscarProyectoPorId(long ProyectoId);
}
