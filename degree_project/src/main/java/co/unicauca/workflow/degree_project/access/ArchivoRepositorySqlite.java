package co.unicauca.workflow.degree_project.access;

import co.unicauca.workflow.degree_project.domain.models.Archivo;
import co.unicauca.workflow.degree_project.domain.models.EstadoArchivo;
import co.unicauca.workflow.degree_project.domain.models.EstadoProyecto;
import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.models.TipoArchivo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArchivoRepositorySqlite implements IArchivoRepository {

    private final Connection conn;

    public ArchivoRepositorySqlite(Connection conn) {
        this.conn = conn;
    }

    @Override
    public int getMaxVersionFormatoA(long proyectoId) {
        final String sql = """
            SELECT COALESCE(MAX(nro_version), 0) AS maxv
            FROM Archivo
            WHERE proyecto_id = ? AND tipo = 'FORMATO_A'
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, proyectoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("maxv") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertarFormatoA(Archivo archivo) {
        final String sql = """
            INSERT INTO Archivo (proyecto_id, tipo, nro_version, nombre_archivo, blob, estado)
            VALUES (?, 'FORMATO_A', ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, archivo.getProyectoId());
            ps.setInt(2, archivo.getNroVersion());
            ps.setString(3, archivo.getNombreArchivo());
            ps.setBytes(4, archivo.getBlob());
            ps.setString(5, archivo.getEstado() == null ? EstadoArchivo.PENDIENTE.name() : archivo.getEstado().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertarArchivo(Archivo archivo) {
        if (archivo.getTipo() == TipoArchivo.CARTA_ACEPTACION) {
            archivo.setNroVersion(1);
            final String sql = """
                INSERT INTO Archivo (proyecto_id, tipo, nro_version, nombre_archivo, blob, estado)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(proyecto_id, tipo, nro_version)
                DO UPDATE SET nombre_archivo = excluded.nombre_archivo,
                              blob = excluded.blob,
                              estado = excluded.estado,
                              fecha_subida = datetime('now')
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, archivo.getProyectoId());
                ps.setString(2, archivo.getTipo().name());
                ps.setInt(3, archivo.getNroVersion());
                ps.setString(4, archivo.getNombreArchivo());
                ps.setBytes(5, archivo.getBlob());
                ps.setString(6, archivo.getEstado() == null ? EstadoArchivo.PENDIENTE.name() : archivo.getEstado().name());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            final String sql = """
                INSERT INTO Archivo (proyecto_id, tipo, nro_version, nombre_archivo, blob, estado)
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, archivo.getProyectoId());
                ps.setString(2, archivo.getTipo().name());
                ps.setInt(3, archivo.getNroVersion());
                ps.setString(4, archivo.getNombreArchivo());
                ps.setBytes(5, archivo.getBlob());
                ps.setString(6, archivo.getEstado() == null ? EstadoArchivo.PENDIENTE.name() : archivo.getEstado().name());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Archivo obtenerArchivo(long archivoId) {
        final String sql = """
            SELECT id, proyecto_id, tipo, nro_version, nombre_archivo, fecha_subida, blob, estado
            FROM Archivo
            WHERE id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, archivoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Archivo a = new Archivo();
                a.setId(rs.getLong("id"));
                a.setProyectoId(rs.getLong("proyecto_id"));
                a.setTipo(TipoArchivo.valueOf(rs.getString("tipo")));
                a.setNroVersion(rs.getInt("nro_version"));
                a.setNombreArchivo(rs.getString("nombre_archivo"));
                a.setFechaSubida(rs.getString("fecha_subida"));
                a.setBlob(rs.getBytes("blob"));
                a.setEstado(EstadoArchivo.valueOf(rs.getString("estado")));
                return a;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Archivo> listarArchivosPorProyecto(long proyectoId, TipoArchivo tipo) {
        final String sql = """
            SELECT id, proyecto_id, tipo, nro_version, nombre_archivo, fecha_subida, blob, estado
            FROM Archivo
            WHERE proyecto_id = ? AND tipo = ?
            ORDER BY nro_version ASC
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, proyectoId);
            ps.setString(2, tipo.name());
            List<Archivo> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Archivo a = new Archivo();
                    a.setId(rs.getLong("id"));
                    a.setProyectoId(rs.getLong("proyecto_id"));
                    a.setTipo(TipoArchivo.valueOf(rs.getString("tipo")));
                    a.setNroVersion(rs.getInt("nro_version"));
                    a.setNombreArchivo(rs.getString("nombre_archivo"));
                    a.setFechaSubida(rs.getString("fecha_subida"));
                    a.setBlob(rs.getBytes("blob"));
                    a.setEstado(EstadoArchivo.valueOf(rs.getString("estado")));
                    out.add(a);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public int countFormatoAByEstado(long proyectoId, EstadoArchivo estado) {
        final String sql = """
            SELECT COUNT(*) AS c
            FROM Archivo
            WHERE proyecto_id = ? AND tipo = 'FORMATO_A' AND estado = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, proyectoId);
            ps.setString(2, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("c") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Archivo getUltimoFormatoA(long proyectoId) {
        final String sql = """
            SELECT id, proyecto_id, tipo, nro_version, nombre_archivo, fecha_subida, blob, estado
            FROM Archivo
            WHERE proyecto_id = ? AND tipo = 'FORMATO_A'
            ORDER BY nro_version DESC
            LIMIT 1
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, proyectoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Archivo a = new Archivo();
                a.setId(rs.getLong("id"));
                a.setProyectoId(rs.getLong("proyecto_id"));
                a.setTipo(TipoArchivo.valueOf(rs.getString("tipo")));
                a.setNroVersion(rs.getInt("nro_version"));
                a.setNombreArchivo(rs.getString("nombre_archivo"));
                a.setFechaSubida(rs.getString("fecha_subida"));
                a.setBlob(rs.getBytes("blob"));
                a.setEstado(EstadoArchivo.valueOf(rs.getString("estado")));
                return a;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Proyecto> listarFormatosAPorEstudiante(String estudianteId) {
        final String sql = """
            SELECT p.id, p.tipo, a.nro_version, a.nombre_archivo, 
                   a.fecha_subida, a.blob, a.estado
            FROM Archivo a
            INNER JOIN Proyecto p ON a.proyecto_id = p.id
            WHERE p.estudiante_id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estudianteId);
            List<Proyecto> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Proyecto p = new Proyecto();
                    Archivo a = new Archivo();
                    p.setArchivo(a);
                    p.setId(rs.getLong("id"));
                    p.setTipo(rs.getString("tipo"));
                    p.getArchivo().setNroVersion(rs.getInt("nro_version"));
                    p.getArchivo().setNombreArchivo(rs.getString("nombre_archivo"));
                    p.getArchivo().setFechaSubida(rs.getString("fecha_subida"));
                    p.getArchivo().setBlob(rs.getBytes("blob"));
                    p.getArchivo().setEstado(EstadoArchivo.valueOf(rs.getString("estado")));
                    out.add(p);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Proyecto buscarProyectoPorId(long proyectoId) {
        final String sql = """
            SELECT id, tipo, estado, titulo, estudiante_id, docente_id, fecha_creacion
            FROM Proyecto
            WHERE id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, proyectoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Proyecto p = new Proyecto();
                    p.setId(rs.getLong("id"));
                    p.setTipo(rs.getString("tipo"));
                    p.setEstado(EstadoProyecto.valueOf(rs.getString("estado")));
                    p.setTitulo(rs.getString("titulo"));
                    p.setEstudianteId(rs.getString("estudiante_id"));
                    p.setDocenteId(rs.getString("docente_id"));
                    p.setFechaCreacion(rs.getString("fecha_creacion"));
                    return p;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    
}
