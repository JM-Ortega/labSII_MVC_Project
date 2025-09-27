package co.unicauca.workflow.degree_project.access;

import co.unicauca.workflow.degree_project.domain.models.User;
import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import co.unicauca.workflow.degree_project.domain.services.UserService;
import co.unicauca.workflow.degree_project.infra.security.Argon2PasswordHasher;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqliteRepository implements IUserRepository {

    private static Connection conn;

    public SqliteRepository(Connection conn) {
        this.conn = conn;
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
          estado         TEXT NOT NULL CHECK (estado IN ('EN_TRAMITE','RECHAZADO','TERMINADO')) DEFAULT 'EN_TRAMITE',
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
                  tipo           TEXT NOT NULL CHECK (tipo IN ('FORMATO_A','ANTEPROYECTO','FINAL','CARTA_ACEPTACION','OTRO')),
                  nro_version    INTEGER NOT NULL CHECK (nro_version >= 1),
                  nombre_archivo TEXT NOT NULL CHECK (lower(nombre_archivo) LIKE '%.pdf'),
                  fecha_subida   TEXT NOT NULL DEFAULT (datetime('now')),
                  blob           BLOB NOT NULL,
                  estado         TEXT NOT NULL CHECK (estado IN ('PENDIENTE','APROBADO','RECHAZADO','OBSERVADO')) DEFAULT 'PENDIENTE',
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
                p.setInt(4, newUser.getRol().ordinal() + 1);
                p.setString(5, newUser.getNombres());
                p.setString(6, newUser.getApellidos());
                p.setInt(7, newUser.getPrograma().ordinal() + 1);
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
    public Optional<AuthResult> authenticate(String email, char[] passwordIngresada) {
        String sql = """
                    SELECT u.id, u.contrasena, r.tipo AS rol, u.nombre, u.apellido, u.programa
                    FROM Usuario u
                    JOIN Rol r ON r.idRol = u.rol
                    WHERE u.correo = ?
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                String hash = rs.getString("contrasena");
                Argon2PasswordHasher hasher = new Argon2PasswordHasher();
                boolean ok = hasher.verify(passwordIngresada, hash);

                java.util.Arrays.fill(passwordIngresada, '\0');

                if (!ok) {
                    return Optional.empty();
                }

                String userId = rs.getString("id");
                String rol = rs.getString("rol");
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");      
                String programa= rs.getString("programa");
                programa = switch (programa) {
                    case "1" -> "Ingenieria de Sistemas";
                    case "2" -> "Ingenieria Electronica y Telecomunicaciones";
                    case "3" -> "Automatica Industrial";
                    case "4" -> "Tecnologia en Telematica";
                    default -> "";
                };               
                
                return Optional.of(new AuthResult(userId, rol, nombreCompleto, programa, email));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
            return Optional.empty();
        }
    }


}
