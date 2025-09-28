package co.unicauca.workflow.degree_project.access;

import co.unicauca.workflow.degree_project.domain.models.EstadoProyecto;
import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.models.TipoTrabajoGrado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProyectoRepositorySqlite implements IProyectoRepository {

    private final Connection conn;

    public ProyectoRepositorySqlite(Connection conn) {
        this.conn = conn;
    }

    @Override
    public long crearProyecto(Proyecto proyecto) {
        final String sql = """
                    INSERT INTO Proyecto (tipo, estado, titulo, estudiante_id, docente_id)
                    VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Convertir enum TipoTrabajoGrado a String antes de guardar
            ps.setString(1, proyecto.getTipo() != null ? proyecto.getTipo().name() : null);

            // EstadoProyecto: si es null, por defecto EN_TRAMITE
            ps.setString(2, proyecto.getEstado() == null
                    ? EstadoProyecto.EN_TRAMITE.name()
                    : proyecto.getEstado().name());

            ps.setString(3, proyecto.getTitulo());
            ps.setString(4, proyecto.getEstudianteId());
            ps.setString(5, proyecto.getDocenteId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            throw new SQLException("No generated key");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean existeProyecto(long proyectoId) {
        final String sql = "SELECT 1 FROM Proyecto WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, proyectoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getEstadoProyecto(long proyectoId) {
        final String sql = "SELECT estado FROM Proyecto WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, proyectoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("estado") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countProyectosEnTramiteDocente(String docenteId) {
        final String sql = "SELECT COUNT(*) AS c FROM Proyecto WHERE docente_id = ? AND estado = 'EN_TRAMITE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, docenteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("c") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean estudianteTieneProyectoEnTramite(String estudianteId) {
        final String sql = "SELECT 1 FROM Proyecto WHERE estudiante_id = ? AND estado = 'EN_TRAMITE' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estudianteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Proyecto> listarPorDocente(String docenteId, String filtro) {
        final boolean conFiltro = filtro != null && !filtro.trim().isEmpty();
        final String like = "%" + (conFiltro ? filtro.trim().toLowerCase() : "") + "%";

        final String sql = """
                SELECT
                  p.id,
                  p.tipo,
                  p.estado,
                  p.titulo,
                  p.estudiante_id,
                  p.docente_id,
                  p.fecha_creacion,
                  COALESCE(MAX(a.nro_version), 0) AS version_formato_a
                FROM Proyecto p
                LEFT JOIN Archivo a
                  ON a.proyecto_id = p.id AND a.tipo = 'FORMATO_A'
                JOIN Usuario u
                  ON u.id = p.estudiante_id
                WHERE p.docente_id = ?
                """ + (conFiltro ? """
                AND (
                  lower(p.titulo) LIKE ?
                  OR lower(u.nombre || ' ' || u.apellido) LIKE ?
                )
                """ : "") + """
                GROUP BY p.id, p.tipo, p.estado, p.titulo, p.estudiante_id, p.docente_id, p.fecha_creacion
                ORDER BY p.fecha_creacion DESC
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, docenteId);
            if (!whereFiltro.isEmpty()) {
                String pat = "%" + filtro.trim().toLowerCase() + "%";
                ps.setString(idx++, pat); // título
                ps.setString(idx++, pat); // nombre completo
                ps.setString(idx++, pat); // correo
            }
            List<Proyecto> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Proyecto p = new Proyecto();
                    p.setId(rs.getLong("id"));
                    p.setTitulo(rs.getString("titulo"));

                    // Mapear a enum
                    String tipoStr = rs.getString("tipo");
                    p.setTipo(TipoTrabajoGrado.valueOf(tipoStr));

                    p.setEstado(EstadoProyecto.valueOf(rs.getString("estado")));
                    p.setTitulo(rs.getString("titulo"));
                    p.setEstudianteId(rs.getString("estudiante_id"));
                    p.setDocenteId(rs.getString("docente_id"));
                    p.setFechaCreacion(rs.getString("fecha_creacion"));
                    out.add(p);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void actualizarEstadoProyecto(long proyectoId, EstadoProyecto nuevoEstado) {
        final String sql = "UPDATE Proyecto SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setLong(2, proyectoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existeEstudiante(String estudianteId) {
        final String sql = "SELECT COUNT(*) AS c FROM Usuario WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estudianteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("c") > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public int countProyectosByEstadoYTipo(String tipo, EstadoProyecto estado, String idDocente) {
        final String sql = """
            SELECT COUNT(*) AS c
            FROM Proyecto
            WHERE tipo = ? 
              AND estado = ? 
              AND docente_id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo);  
            ps.setString(2, estado.name());
            ps.setString(3, idDocente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("c") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Coordinador
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
                    p.setTipo(TipoTrabajoGrado.valueOf(rs.getString("tipo")));
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


    @Override
    public String nombreDocente(String docenteId){
        String nombreDocente = null;

        String sql = """
                    SELECT u.nombre, u.apellido
                    FROM Usuario u
                    JOIN Rol r ON u.rol = r.idRol
                    WHERE u.id = ? AND r.tipo = 'Docente'
                     """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docenteId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nombreDocente = rs.getString("nombre") + " " + rs.getString("apellido");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return nombreDocente;
    }
    
    @Override
    public String correoDocente(String docenteId){
        String correoDocente = "";

        String sql = """
                    SELECT u.correo
                    FROM Usuario u
                    JOIN Rol r ON u.rol = r.idRol
                    WHERE u.id = ? AND r.tipo = 'Docente'
                     """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, docenteId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    correoDocente = rs.getString("correo"); 
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return correoDocente;
    }

    @Override
    public void update(Proyecto proyecto) {
        String sql = "UPDATE Proyecto SET tipo=?, estado=?, titulo=?, estudiante_id=?, docente_id=?, fecha_creacion=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, proyecto.getTipo().name());
            ps.setString(2, proyecto.getEstado().name());
            ps.setString(3, proyecto.getTitulo());
            ps.setString(4, proyecto.getEstudianteId());
            ps.setString(5, proyecto.getDocenteId());
            ps.setString(6, proyecto.getFechaCreacion());
            ps.setLong(7, proyecto.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean existeEstudiantePorCorreo(String correo) {
        final String sql = """
                    SELECT 1
                    FROM Usuario u
                    JOIN Rol r ON r.idRol = u.rol
                    WHERE lower(u.correo) = lower(?) AND lower(r.tipo) = 'estudiante'
                    LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getEstudianteIdPorCorreo(String correo) {
        final String sql = """
                    SELECT u.id
                    FROM Usuario u
                    JOIN Rol r ON r.idRol = u.rol
                    WHERE lower(u.correo) = lower(?) AND lower(r.tipo) = 'estudiante'
                    LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("id") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean estudianteTieneProyectoEnTramitePorCorreo(String correo) {
        final String sql = """
                    SELECT 1
                    FROM Proyecto p
                    JOIN Usuario u ON u.id = p.estudiante_id
                    JOIN Rol r ON r.idRol = u.rol
                    WHERE lower(u.correo) = lower(?) AND lower(r.tipo) = 'estudiante'
                      AND p.estado = 'EN_TRAMITE'
                    LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
