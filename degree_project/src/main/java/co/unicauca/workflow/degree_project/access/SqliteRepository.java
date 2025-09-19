package co.unicauca.workflow.degree_project.access;

import co.unicauca.workflow.degree_project.domain.models.Archivo;
import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.models.User;
import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.domain.services.UserService;
import co.unicauca.workflow.degree_project.infra.security.Argon2PasswordHasher;
import co.unicauca.workflow.degree_project.infra.security.Sesion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqliteRepository implements IUserRepository {

    private static Connection conn;

    public SqliteRepository() {
        connect();
        initDatabase();
    }

    private void initDatabase() {
        String fkOn = "PRAGMA foreign_keys = ON;";

        String sqlRol = """
        CREATE TABLE IF NOT EXISTS Rol (
          idRol INTEGER PRIMARY KEY,
          tipo  TEXT NOT NULL UNIQUE
        );
    """;
        String sqlPrograma = """
        CREATE TABLE IF NOT EXISTS Programa (
          idPrograma INTEGER PRIMARY KEY,
          tipo       TEXT NOT NULL UNIQUE
        );
    """;
        String seedRol = "INSERT OR IGNORE INTO Rol(tipo) VALUES ('Estudiante'), ('Docente'), ('Coordinador');";

        String seedPrograma = """
            INSERT OR IGNORE INTO Programa(tipo)
            VALUES 
              ('Ingenieria_de_Sistemas'),
              ('Ingenieria_Electronica_y_Telecomunicaciones'),
              ('Automatica_Industrial'),
              ('Tecnologia_en_Telematica');
        """;

        String sqlUsuario = """
        CREATE TABLE IF NOT EXISTS Usuario (
          id         TEXT PRIMARY KEY,          -- UUID
          correo     TEXT NOT NULL UNIQUE,
          contrasena TEXT NOT NULL,             -- hash
          rol        INTEGER NOT NULL,          -- FK a Rol
          nombre     TEXT NOT NULL,
          apellido   TEXT NOT NULL,
          programa   INTEGER NOT NULL,          -- FK a Programa
          celular    TEXT,
          FOREIGN KEY (rol)      REFERENCES Rol(idRol),
          FOREIGN KEY (programa) REFERENCES Programa(idPrograma)
        );
    """;

        String sqlProyecto = """
        CREATE TABLE IF NOT EXISTS Proyecto (
          id             INTEGER PRIMARY KEY AUTOINCREMENT,
          tipo           TEXT NOT NULL CHECK (tipo IN ('TESIS','PRACTICA_PROFESIONAL')),
          estado         TEXT NOT NULL CHECK (estado IN ('EN_TRAMITE','CANCELADO','TERMINADO')) DEFAULT 'EN_TRAMITE',
          titulo         TEXT NOT NULL,
          estudiante_id  TEXT NOT NULL,
          docente_id     TEXT NOT NULL,
          fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
          FOREIGN KEY (estudiante_id) REFERENCES Usuario(id) ON UPDATE CASCADE ON DELETE RESTRICT,
          FOREIGN KEY (docente_id)    REFERENCES Usuario(id) ON UPDATE CASCADE ON DELETE RESTRICT
        );
        """;

        String sqlArchivo = """
        CREATE TABLE IF NOT EXISTS Archivo (
          id             INTEGER PRIMARY KEY AUTOINCREMENT,
          proyecto_id    INTEGER NOT NULL,
          tipo           TEXT NOT NULL CHECK (tipo IN ('FORMATO_A','ANTEPROYECTO','FINAL','OTRO')),
          estado         TEXT NOT NULL CHECK (estado IN ('ACEPTADO','RECHAZADO','A_EVALUAR')) DEFAULT 'A_EVALUAR',
          nro_version    INTEGER NOT NULL CHECK (nro_version >= 1),
          nombre_archivo TEXT NOT NULL CHECK (lower(nombre_archivo) LIKE '%.pdf'),
          fecha_subida   TEXT NOT NULL DEFAULT (datetime('now')),
          blob           BLOB NOT NULL,
          FOREIGN KEY (proyecto_id) REFERENCES Proyecto(id) ON UPDATE CASCADE ON DELETE CASCADE,
          UNIQUE (proyecto_id, tipo, nro_version)
        );
        """;

        String idxArchivoProyecto = "CREATE INDEX IF NOT EXISTS idx_archivos_proyecto ON Archivo(proyecto_id);";
        String idxArchivoTipo = "CREATE INDEX IF NOT EXISTS idx_archivos_tipo ON Archivo(proyecto_id, tipo, nro_version);";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(fkOn);

            // catálogos
            stmt.execute(sqlRol);
            stmt.execute(sqlPrograma);
            stmt.execute(seedRol);
            stmt.execute(seedPrograma);

            // datos maestros
            stmt.execute(sqlUsuario);

            // negocio
            stmt.execute(sqlProyecto);
            stmt.execute(sqlArchivo);
            stmt.execute(idxArchivoProyecto);
            stmt.execute(idxArchivoTipo);
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void connect() {
        if (conn != null) {
            return;
        }
        String url = "jdbc:sqlite:degree_project.db";
        try {
            conn = DriverManager.getConnection(url);
            try (Statement s = conn.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void disconnect() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ignored) {
        }
    }

    @Override
    public boolean save(User newUser) {
        try {
            if (newUser == null
                    || newUser.getEmail() == null || newUser.getEmail().isBlank()
                    || !newUser.getEmail().endsWith("@unicauca.edu.co")
                    || newUser.getNombres() == null || newUser.getNombres().isBlank()
                    || newUser.getApellidos() == null || newUser.getApellidos().isBlank()
                    || newUser.getPrograma() == null
                    || newUser.getRol() == null
                    || newUser.getPasswordHash() == null || newUser.getPasswordHash().isBlank()) {
                return false;
            }

            String sql = """
            INSERT INTO Usuario (id, correo, contrasena, rol, nombre, apellido, programa, celular)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, newUser.getId());
                p.setString(2, newUser.getEmail());
                p.setString(3, newUser.getPasswordHash());
                p.setInt(4, newUser.getRol().ordinal() + 1);        // mapea a Rol.idRol seed
                p.setString(5, newUser.getNombres());
                p.setString(6, newUser.getApellidos());
                p.setInt(7, newUser.getPrograma().ordinal() + 1);   // mapea a Programa.idPrograma seed
                p.setString(8, newUser.getCelular());
                p.executeUpdate();
            }
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public String getRol(String email, char[] passwordIngresada) {
        String sql = """
        SELECT u.contrasena, r.tipo
        FROM Usuario u
        INNER JOIN Rol r ON u.rol = r.idRol
        WHERE u.correo = ?
    """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String hash = rs.getString("contrasena");
                String tipoRol = rs.getString("tipo");
                Argon2PasswordHasher hasher = new Argon2PasswordHasher();
                return hasher.verify(passwordIngresada, hash) ? tipoRol : null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String getPassword(String email) {
        String sql = "SELECT contrasena FROM Usuario WHERE correo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("contrasena") : null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public boolean validarIngrereso(String email, char[] passwordIngresada) {
        String hash = getPassword(email);
        if (hash == null) {
            return false;
        }
        Argon2PasswordHasher hasher = new Argon2PasswordHasher();
        return hasher.verify(passwordIngresada, hash);
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM Usuario WHERE correo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }

    @Override
    public String getName(String email) {
        String sql = "SELECT nombre || ' ' || apellido AS nom FROM Usuario WHERE correo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("nom") : null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public AuthResult authenticate(String email, char[] passwordIngresada) {
        String sql = """
            SELECT u.id, u.contrasena, r.tipo AS rol, u.nombre, u.apellido
            FROM Usuario u
            JOIN Rol r ON r.idRol = u.rol
            WHERE u.correo = ?
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String hash = rs.getString("contrasena");
                Argon2PasswordHasher hasher = new Argon2PasswordHasher();
                boolean ok = hasher.verify(passwordIngresada, hash);

                java.util.Arrays.fill(passwordIngresada, '\0');

                if (!ok) {
                    return null;
                }

                String userId = rs.getString("id");
                String rol = rs.getString("rol");
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                AuthResult authResult = new AuthResult(userId, rol, nombreCompleto);
                Sesion.getInstancia().setUsuarioActual(authResult);
                return authResult;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public List<Proyecto> findFormatosAByEstudianteId(String estudianteId) {
        List<Proyecto> documentos = new ArrayList<>();
        String sql = """
            SELECT Proyecto.tipo, Proyecto.titulo, 
                   Archivo.fecha_subida, Archivo.estado, Archivo.nro_version, Archivo.blob
            FROM Proyecto 
            INNER JOIN Archivo ON Proyecto.id = Archivo.proyecto_id
            WHERE Proyecto.estudiante_id = ? AND Archivo.tipo = "FORMATO_A"
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estudianteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Proyecto pro = new Proyecto();
                pro.setTipoProyecto(rs.getString("tipo"));
                pro.setTitulo(rs.getString("titulo"));
                
                Archivo arch = new Archivo();
                arch.setFechaPublicacion(rs.getString("fecha_subida"));
                arch.setEstadoArchivo(rs.getString("estado"));
                arch.setVersion(rs.getInt("nro_version"));
                byte[] contenido = rs.getBytes("blob");
                arch.setContenido(contenido);
                System.out.println("DEBUG: blob null? " + (contenido == null) + " length=" + (contenido == null ? 0 : contenido.length));
                
                pro.agregarArchivo(arch);
                
                documentos.add(pro);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return documentos;
    }

}
