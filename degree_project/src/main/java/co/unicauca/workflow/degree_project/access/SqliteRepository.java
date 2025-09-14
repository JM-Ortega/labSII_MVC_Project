package co.unicauca.workflow.degree_project.access;

import co.unicauca.workflow.degree_project.domain.models.User;
import co.unicauca.workflow.degree_project.domain.models.Rol;       // Estudiante, Docente
import co.unicauca.workflow.degree_project.domain.models.Programa; // sistemas, electronica, telematica, industrial
import co.unicauca.workflow.degree_project.domain.services.UserService;
import co.unicauca.workflow.degree_project.infra.security.Argon2PasswordHasher;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqliteRepository implements IUserRepository {

    private static Connection conn;

    public SqliteRepository() {
        connect();
        initDatabase();
    }

    private void initDatabase() {
        String sql1 = "PRAGMA foreign_keys = ON;";

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

        String seedRol = "INSERT OR IGNORE INTO Rol(tipo) VALUES ('Estudiante'), ('Docente');";

        String seedPrograma = """
            INSERT OR IGNORE INTO Programa(tipo)
            VALUES ('sistemas'), ('electronica'), ('telematica'), ('industrial');
        """;

        String sqlUsuario = """
            CREATE TABLE IF NOT EXISTS Usuario (
              id         TEXT PRIMARY KEY,          -- UUID
              correo     TEXT NOT NULL UNIQUE,
              contrasena TEXT NOT NULL,             -- hash
              rol        INTEGER NOT NULL,
              FOREIGN KEY (rol) REFERENCES Rol(idRol)
            );
        """;

        String sqlDocente = """
            CREATE TABLE IF NOT EXISTS Docente (
              id       TEXT PRIMARY KEY,            -- = Usuario.id
              nombre   TEXT NOT NULL,
              apellido TEXT NOT NULL,
              programa INTEGER NOT NULL,
              celular  TEXT,
              FOREIGN KEY (id)       REFERENCES Usuario(id)   ON DELETE CASCADE,
              FOREIGN KEY (programa) REFERENCES Programa(idPrograma)
            );
        """;

        String sqlEstudiante = """
            CREATE TABLE IF NOT EXISTS Estudiante (
              id       TEXT PRIMARY KEY,            -- = Usuario.id
              nombre   TEXT NOT NULL,
              apellido TEXT NOT NULL,
              programa INTEGER NOT NULL,
              celular  TEXT,
              FOREIGN KEY (id)       REFERENCES Usuario(id)   ON DELETE CASCADE,
              FOREIGN KEY (programa) REFERENCES Programa(idPrograma)
            );
        """;

        // Coordinador singleton (solo 1)
        String sqlCoordinador = """
            CREATE TABLE IF NOT EXISTS Coordinador (
              id       TEXT PRIMARY KEY,            -- = Usuario.id (si lo deseas)
              nombre   TEXT NOT NULL,
              apellido TEXT NOT NULL,
              celular  TEXT
            );
        """;

        String trgCoordinadorSingleton = """
            CREATE TRIGGER IF NOT EXISTS trg_coordinador_singleton
            BEFORE INSERT ON Coordinador
            FOR EACH ROW
            WHEN (SELECT COUNT(*) FROM Coordinador) >= 1
            BEGIN
              SELECT RAISE(ABORT, 'Solo puede existir un coordinador');
            END;
        """;

        // FormatoA con PK compuesta (id,version)
        String sqlFormatoA = """
            CREATE TABLE IF NOT EXISTS FormatoA (
              id           TEXT    NOT NULL,        -- expediente
              version      INTEGER NOT NULL,        -- 1..3
              idEstudiante TEXT    NOT NULL,
              idProfesor   TEXT    NOT NULL,        -- Docente autor
              pdf          BLOB    NOT NULL,
              estadoFA     TEXT    NOT NULL,        -- a_evaluar | aceptado | rechazado
              creado_en    INTEGER NOT NULL DEFAULT (unixepoch()),
              actualizado_en INTEGER NOT NULL DEFAULT (unixepoch()),
              PRIMARY KEY (id, version),
              CHECK (version BETWEEN 1 AND 3),
              CHECK (estadoFA IN ('a_evaluar','aceptado','rechazado')),
              FOREIGN KEY (idEstudiante) REFERENCES Estudiante(id) ON DELETE CASCADE,
              FOREIGN KEY (idProfesor)   REFERENCES Docente(id)    ON DELETE SET NULL
            );
        """;

        String viewFormatoAActual = """
            CREATE VIEW IF NOT EXISTS FormatoA_Actual AS
            SELECT f.*
            FROM FormatoA f
            JOIN (SELECT id, MAX(version) AS v FROM FormatoA GROUP BY id) ult
              ON ult.id = f.id AND ult.v = f.version;
        """;

        // ===== Reglas de negocio sin tabla Evaluacion =====
        // Solo 1 "a_evaluar" por expediente
        String idxAevaluar = """
            CREATE UNIQUE INDEX IF NOT EXISTS ux_fa_un_aevaluar
            ON FormatoA(id)
            WHERE estadoFA = 'a_evaluar';
        """;

        String trgOneAevaluar = """
            CREATE TRIGGER IF NOT EXISTS trg_fa_one_aevaluar_per_expediente
            BEFORE INSERT ON FormatoA
            FOR EACH ROW
            WHEN NEW.estadoFA = 'a_evaluar'
             AND EXISTS (SELECT 1 FROM FormatoA x WHERE x.id = NEW.id AND x.estadoFA = 'a_evaluar')
            BEGIN
              SELECT RAISE(ABORT, 'Ya existe una version a_evaluar para este expediente');
            END;
        """;

        // v(n+1) solo si v(n) fue 'rechazado'
        String trgChain = """
            CREATE TRIGGER IF NOT EXISTS trg_fa_enforce_chain
            BEFORE INSERT ON FormatoA
            FOR EACH ROW
            WHEN NEW.version > 1
             AND NOT EXISTS (
               SELECT 1 FROM FormatoA prev
               WHERE prev.id = NEW.id
                 AND prev.version = NEW.version - 1
                 AND prev.estadoFA = 'rechazado'
             )
            BEGIN
              SELECT RAISE(ABORT, 'Para crear la nueva version, la version anterior debe estar rechazada');
            END;
        """;

        // No permitir nuevas versiones si ya hay una 'aceptado'
        String trgNoAfterAccept = """
            CREATE TRIGGER IF NOT EXISTS trg_fa_no_after_accept
            BEFORE INSERT ON FormatoA
            FOR EACH ROW
            WHEN EXISTS (SELECT 1 FROM FormatoA acc WHERE acc.id = NEW.id AND acc.estadoFA = 'aceptado')
            BEGIN
              SELECT RAISE(ABORT, 'El expediente ya fue aceptado; no se permiten nuevas versiones');
            END;
        """;

        // Restringir UPDATE de estado: solo desde 'a_evaluar' -> 'aceptado'/'rechazado'
        String trgUpdateEstado = """
            CREATE TRIGGER IF NOT EXISTS trg_fa_valid_state_update
            BEFORE UPDATE OF estadoFA ON FormatoA
            FOR EACH ROW
            WHEN NOT (OLD.estadoFA = 'a_evaluar' AND NEW.estadoFA IN ('aceptado','rechazado'))
            BEGIN
              SELECT RAISE(ABORT, 'Transicion de estado invalida');
            END;
        """;

        // Actualizar 'actualizado_en' en cada UPDATE
        String trgTouchUpdate = """
            CREATE TRIGGER IF NOT EXISTS trg_fa_touch_update
            AFTER UPDATE ON FormatoA
            FOR EACH ROW
            BEGIN
              UPDATE FormatoA
              SET actualizado_en = unixepoch()
              WHERE id = NEW.id AND version = NEW.version;
            END;
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql1);
            stmt.execute(sqlRol);
            stmt.execute(sqlPrograma);
            stmt.execute(seedRol);
            stmt.execute(seedPrograma);
            stmt.execute(sqlUsuario);
            stmt.execute(sqlDocente);
            stmt.execute(sqlEstudiante);

            stmt.execute(sqlCoordinador);
            stmt.execute(trgCoordinadorSingleton);

            stmt.execute(sqlFormatoA);
            stmt.execute(viewFormatoAActual);

            stmt.execute(idxAevaluar);
            stmt.execute(trgOneAevaluar);
            stmt.execute(trgChain);
            stmt.execute(trgNoAfterAccept);
            stmt.execute(trgUpdateEstado);
            stmt.execute(trgTouchUpdate);
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

    // ================= Métodos IUserRepository (sin cambios) =================
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

            conn.setAutoCommit(false);

            String sqlUsuarioIns = "INSERT INTO Usuario (id, correo, contrasena, rol) VALUES (?, ?, ?, ?)";
            try (PreparedStatement p = conn.prepareStatement(sqlUsuarioIns)) {
                p.setString(1, newUser.getId());
                p.setString(2, newUser.getEmail());
                p.setString(3, newUser.getPasswordHash());
                p.setInt(4, newUser.getRol().ordinal() + 1); // asumiendo orden en tabla Rol
                p.executeUpdate();
            }

            if (newUser.getRol() == Rol.Estudiante) {
                String sqlEstIns = "INSERT INTO Estudiante (id, nombre, apellido, programa, celular) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement p = conn.prepareStatement(sqlEstIns)) {
                    p.setString(1, newUser.getId());
                    p.setString(2, newUser.getNombres());
                    p.setString(3, newUser.getApellidos());
                    p.setInt(4, newUser.getPrograma().ordinal() + 1);
                    p.setString(5, newUser.getCelular());
                    p.executeUpdate();
                }
            } else {
                String sqlDocIns = "INSERT INTO Docente (id, nombre, apellido, programa, celular) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement p = conn.prepareStatement(sqlDocIns)) {
                    p.setString(1, newUser.getId());
                    p.setString(2, newUser.getNombres());
                    p.setString(3, newUser.getApellidos());
                    p.setInt(4, newUser.getPrograma().ordinal() + 1);
                    p.setString(5, newUser.getCelular());
                    p.executeUpdate();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
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
        String sql = """
        SELECT COALESCE(e.nombre, d.nombre) AS nom
        FROM Usuario u
        LEFT JOIN Estudiante e ON e.id = u.id
        LEFT JOIN Docente d ON d.id = u.id
        WHERE u.correo = ?
        LIMIT 1;
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
