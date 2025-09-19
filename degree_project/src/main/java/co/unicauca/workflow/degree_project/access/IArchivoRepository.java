package co.unicauca.workflow.degree_project.access;

import co.unicauca.workflow.degree_project.domain.models.Archivo;
import co.unicauca.workflow.degree_project.domain.models.EstadoArchivo;
import co.unicauca.workflow.degree_project.domain.models.TipoArchivo;

import java.util.List;

public interface IArchivoRepository {
    int getMaxVersionFormatoA(long proyectoId);

    void insertarFormatoA(Archivo archivo);

    void insertarArchivo(Archivo archivo);

    Archivo obtenerArchivo(long archivoId);

    List<Archivo> listarArchivosPorProyecto(long proyectoId, TipoArchivo tipo);

    int countFormatoAByEstado(long proyectoId, EstadoArchivo estado);

    Archivo getUltimoFormatoA(long proyectoId);

}
