package co.unicauca.workflow.degree_project.domain.services;

import co.unicauca.workflow.degree_project.access.IArchivoRepository;
import co.unicauca.workflow.degree_project.access.IProyectoRepository;
import co.unicauca.workflow.degree_project.domain.models.*;
import co.unicauca.workflow.degree_project.infra.operation.PdfValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProyectoService implements IProyectoService{
    private final List<Observer> observers = new ArrayList<>();
    private final IProyectoRepository proyectoRepo;
    private final IArchivoRepository archivoRepo;
    private final Connection conn;

    
    public ProyectoService(IProyectoRepository proyectoRepo,
                           IArchivoRepository archivoRepo,
                           Connection conn) {
        this.proyectoRepo = proyectoRepo;
        this.archivoRepo = archivoRepo;
        this.conn = conn;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static RuntimeException toRuntime(Exception ex) {
        return (ex instanceof RuntimeException) ? (RuntimeException) ex : new RuntimeException(ex);
    }

    @Override
    public Proyecto crearProyectoConFormatoA(Proyecto proyecto, Archivo archivo) {
        if (proyecto == null || archivo == null) throw new IllegalArgumentException("Datos incompletos");
        if (isBlank(proyecto.getTitulo()) || isBlank(proyecto.getEstudianteId()) || isBlank(proyecto.getDocenteId()))
            throw new IllegalArgumentException("Título, estudiante y docente son obligatorios");
        if (archivo.getTipo() != null && archivo.getTipo() != TipoArchivo.FORMATO_A)
            throw new IllegalArgumentException("Solo se admite FORMATO_A");
        PdfValidator.assertPdf(archivo.getNombreArchivo(), archivo.getBlob());

        if (!docenteTieneCupo(proyecto.getDocenteId()))
            throw new IllegalStateException("El docente alcanzó el límite de 7 proyectos en curso");

        if (!estudianteLibre(proyecto.getEstudianteId()))
            throw new IllegalStateException("El estudiante ya tiene un proyecto en curso");

        boolean prevAutoCommit;
        try {
            prevAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            proyecto.setEstado(EstadoProyecto.EN_TRAMITE);
            long proyectoId = proyectoRepo.crearProyecto(proyecto);

            archivo.setProyectoId(proyectoId);
            archivo.setTipo(TipoArchivo.FORMATO_A);
            archivo.setNroVersion(1);
            archivo.setEstado(EstadoArchivo.PENDIENTE);
            archivoRepo.insertarFormatoA(archivo);

            conn.commit();
            conn.setAutoCommit(prevAutoCommit);

            proyecto.setId(proyectoId);
            notifyObservers();
            return proyecto;

        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            throw toRuntime(ex);
        }
    }

    @Override
    public long crearProyectoConArchivos(Proyecto proyecto, List<Archivo> archivos) {
        if (proyecto == null || archivos == null || archivos.isEmpty())
            throw new IllegalArgumentException("Datos incompletos");
        if (isBlank(proyecto.getTitulo()) || isBlank(proyecto.getEstudianteId()) || isBlank(proyecto.getDocenteId()))
            throw new IllegalArgumentException("Título, estudiante y docente son obligatorios");

        boolean prevAutoCommit;
        try {
            prevAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            if (!docenteTieneCupo(proyecto.getDocenteId()))
                throw new IllegalStateException("El docente alcanzó el límite de 7 proyectos en curso");
            if (!estudianteLibre(proyecto.getEstudianteId()))
                throw new IllegalStateException("El estudiante ya tiene un proyecto en curso");

            proyecto.setEstado(EstadoProyecto.EN_TRAMITE);
            long proyectoId = proyectoRepo.crearProyecto(proyecto);

            for (Archivo a : archivos) {
                PdfValidator.assertPdf(a.getNombreArchivo(), a.getBlob());
                a.setProyectoId(proyectoId);
                a.setEstado(a.getEstado() == null ? EstadoArchivo.PENDIENTE : a.getEstado());

                if (a.getTipo() == TipoArchivo.FORMATO_A) {
                    int next = Math.max(1, archivoRepo.getMaxVersionFormatoA(proyectoId) + 1);
                    a.setNroVersion(next);
                    archivoRepo.insertarFormatoA(a);
                } else if (a.getTipo() == TipoArchivo.CARTA_ACEPTACION) {
                    a.setNroVersion(1);
                    archivoRepo.insertarArchivo(a);
                } else {
                    a.setNroVersion(1);
                    archivoRepo.insertarArchivo(a);
                }
            }

            conn.commit();
            conn.setAutoCommit(prevAutoCommit);
            notifyObservers();
            return proyectoId;

        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            throw toRuntime(ex);
        }
    }

    @Override
    public Archivo subirNuevaVersionFormatoA(long proyectoId, Archivo archivo) {
        if (!proyectoRepo.existeProyecto(proyectoId))
            throw new IllegalArgumentException("Proyecto no existe");

        String estado = proyectoRepo.getEstadoProyecto(proyectoId);
        if (!"EN_TRAMITE".equalsIgnoreCase(estado))
            throw new IllegalStateException("El proyecto no está en curso");

        int max = archivoRepo.getMaxVersionFormatoA(proyectoId);
        if (max >= 3)
            throw new IllegalStateException("Se alcanzó el máximo de 3 versiones del Formato A");

        PdfValidator.assertPdf(archivo.getNombreArchivo(), archivo.getBlob());

        archivo.setProyectoId(proyectoId);
        archivo.setTipo(TipoArchivo.FORMATO_A);
        archivo.setNroVersion(max + 1);
        archivo.setEstado(EstadoArchivo.PENDIENTE);

        archivoRepo.insertarFormatoA(archivo);
        notifyObservers();
        return archivo;
    }

    @Override
    public void subirCartaAceptacion(long proyectoId, Archivo carta) {
        if (!proyectoRepo.existeProyecto(proyectoId))
            throw new IllegalArgumentException("Proyecto no existe");

        String estado = proyectoRepo.getEstadoProyecto(proyectoId);
        if (!"EN_TRAMITE".equalsIgnoreCase(estado))
            throw new IllegalStateException("El proyecto no está en curso");

        PdfValidator.assertPdf(carta.getNombreArchivo(), carta.getBlob());

        carta.setProyectoId(proyectoId);
        carta.setTipo(TipoArchivo.CARTA_ACEPTACION);
        carta.setNroVersion(1);
        carta.setEstado(carta.getEstado() == null ? EstadoArchivo.PENDIENTE : carta.getEstado());

        archivoRepo.insertarArchivo(carta);
    }

    @Override
    public boolean docenteTieneCupo(String docenteId) {
        return proyectoRepo.countProyectosEnTramiteDocente(docenteId) < 7;
    }

    @Override
    public boolean estudianteLibre(String estudianteId) {
        if (!proyectoRepo.existeEstudiante(estudianteId)) {
            throw new IllegalArgumentException("El estudiante no existe");
        }
        return !proyectoRepo.estudianteTieneProyectoEnTramite(estudianteId);
    }


    @Override
    public int maxVersionFormatoA(long proyectoId) {
        return archivoRepo.getMaxVersionFormatoA(proyectoId);
    }

    @Override
    public boolean canResubmit(long proyectoId) {
        String estado = proyectoRepo.getEstadoProyecto(proyectoId);
        if (estado == null || !"EN_TRAMITE".equalsIgnoreCase(estado)) return false;

        int max = archivoRepo.getMaxVersionFormatoA(proyectoId);
        if (max == 0) return true;
        if (max >= 3) return false;

        Archivo ultimo = archivoRepo.getUltimoFormatoA(proyectoId);
        if (ultimo == null) return true;
        return ultimo.getEstado() == EstadoArchivo.OBSERVADO;
    }


    @Override
    public List<Proyecto> listarProyectosDocente(String docenteId, String filtro) {
        return proyectoRepo.listarPorDocente(docenteId, filtro);
    }

    @Override
    public Archivo obtenerArchivo(long archivoId) {
        return archivoRepo.obtenerArchivo(archivoId);
    }

    @Override
    public List<Archivo> listarArchivosPorProyecto(long proyectoId, TipoArchivo tipo) {
        return archivoRepo.listarArchivosPorProyecto(proyectoId, tipo);
    }

    @Override
    public boolean tieneObservacionesFormatoA(long proyectoId) {
        Archivo ultimo = archivoRepo.getUltimoFormatoA(proyectoId);
        return ultimo != null && ultimo.getEstado() == EstadoArchivo.OBSERVADO;
    }

    @Override
    public Archivo obtenerUltimoFormatoAConObservaciones(long proyectoId) {
        Archivo ultimo = archivoRepo.getUltimoFormatoA(proyectoId);
        if (ultimo != null && ultimo.getEstado() == EstadoArchivo.OBSERVADO) return ultimo;
        return null;
    }

    @Override
    public EstadoProyecto enforceAutoCancelIfNeeded(long proyectoId) {
        int observados = archivoRepo.countFormatoAByEstado(proyectoId, EstadoArchivo.OBSERVADO);
        if (observados >= 3) {
            proyectoRepo.actualizarEstadoProyecto(proyectoId, EstadoProyecto.CANCELADO);
            return EstadoProyecto.CANCELADO;
        }
        return EstadoProyecto.EN_TRAMITE;
    }

    //Coordinador
    @Override
    public List<Archivo> listarTodosArchivos() {
        return archivoRepo.listarArchivos();
    }
    
    @Override
    public Proyecto buscarProyectoPorId(long proyectoId){
        return proyectoRepo.proyectoPorId(proyectoId);
    }
    
    @Override
    public String obtenerNombreDocente(String docenteId){
        return proyectoRepo.nombreDocente(docenteId);
    }

    @Override
    public Archivo obtenerFormatoA(long archivoId) {
        Archivo ultimo = archivoRepo.getFormatoA(archivoId);
        if (ultimo != null) return ultimo;
        return null;
    }

    
    @Override
    public int countArchivosByEstadoYTipo(String tipo, String estado) {
        try {
            TipoArchivo tipoEnum = TipoArchivo.valueOf(tipo.toUpperCase());
            EstadoArchivo estadoEnum = EstadoArchivo.valueOf(estado.toUpperCase());
            return archivoRepo.countArchivosByEstadoYTipo(tipoEnum, estadoEnum);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo o estado inválido: " + tipo + ", " + estado, e);
        }
    }

    @Override
    public List<Proyecto> listarFormatosAPorEstudiante(String estudianteId) {
        return archivoRepo.listarFormatosAPorEstudiante(estudianteId);
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }

    @Override
    public int subirObservacion (long proyectoId, Archivo archivo) {
        if (!proyectoRepo.existeProyecto(proyectoId))
            throw new IllegalArgumentException("Proyecto no existe");

        String estado = proyectoRepo.getEstadoProyecto(proyectoId);
        if (!"EN_TRAMITE".equalsIgnoreCase(estado))
            throw new IllegalStateException("El proyecto no está en curso");

        int max = archivoRepo.getMaxVersionFormatoA(proyectoId);
        if (max > 3)
            throw new IllegalStateException("Se alcanzó el maximo de 3 versiones del Formato A");
        
        
        if (archivo.getEstado().toString().equals("APROBADO")) {
            archivo.setNroVersion(max);
            Proyecto proyecto = buscarProyectoPorId(proyectoId);
            proyecto.setEstado(EstadoProyecto.TERMINADO);
            proyecto.setArchivo(archivo);
            
            archivo.setEstado(EstadoArchivo.OBSERVADO);
            
            archivoRepo.actualizarFormatoA(archivo);
            notifyObservers();
            
            return 1;
        } else if (archivo.getEstado().toString().equals("OBSERVADO")) {
            archivo.setNroVersion(max);

            Proyecto proyecto = buscarProyectoPorId(proyectoId);
            proyecto.setArchivo(archivo);
            
            archivoRepo.actualizarFormatoA(archivo);
            notifyObservers();

            return 2;
        }
        return 3;
    }
}
