package co.unicauca.workflow.degree_project.access;

import co.unicauca.workflow.degree_project.domain.models.EstadoProyecto;
import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.models.TipoTrabajoGrado;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProyectoRepositorySqliteTest {

    static Connection conn;
    ProyectoRepositorySqlite repo;

    @BeforeAll
    static void setUpAll() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
        }
        initSchema(conn);
    }

    @BeforeEach
    void setUp() throws Exception {
        cleanTables(conn);
        seedCatalogos(conn);
        seedUsuarios(conn);
        repo = new ProyectoRepositorySqlite(conn);
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        if (conn != null) conn.close();
    }

    @Test
    void crearProyecto_y_verificar_existencia_y_estado() {
        Proyecto p = baseProyecto(TipoTrabajoGrado.TESIS, "Título A", "est-1", "doc-1");
        long id = repo.crearProyecto(p);
        assertTrue(repo.existeProyecto(id));
        assertEquals(EstadoProyecto.EN_TRAMITE.name(), repo.getEstadoProyecto(id));
    }

    @Test
    void contar_proyectos_en_tramite_por_docente() {
        assertEquals(0, repo.countProyectosEnTramiteDocente("doc-1"));
        long p1 = repo.crearProyecto(baseProyecto(TipoTrabajoGrado.TESIS, "T1", "est-1", "doc-1"));
        long p2 = repo.crearProyecto(baseProyecto(TipoTrabajoGrado.TESIS, "T2", "est-2", "doc-1"));
        assertTrue(p1 > 0 && p2 > 0);
        assertEquals(2, repo.countProyectosEnTramiteDocente("doc-1"));
    }

    @Test
    void estudiante_tiene_proyecto_en_tramite() throws Exception {
        assertFalse(repo.estudianteTieneProyectoEnTramite("est-1"));
        long p1 = repo.crearProyecto(baseProyecto(TipoTrabajoGrado.TESIS, "T1", "est-1", "doc-1"));
        assertTrue(p1 > 0);
        assertTrue(repo.estudianteTieneProyectoEnTramite("est-1"));
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Proyecto SET estado='RECHAZADO' WHERE id=?")) {
            ps.setLong(1, p1);
            ps.executeUpdate();
        }

        assertFalse(repo.estudianteTieneProyectoEnTramite("est-1"));
    }

    @Test
    void listar_por_docente_con_y_sin_filtro() {
        long a = repo.crearProyecto(baseProyecto(TipoTrabajoGrado.TESIS, "Sistema de recomendación", "est-1", "doc-1"));
        long b = repo.crearProyecto(baseProyecto(TipoTrabajoGrado.TESIS, "Visión por computador", "est-2", "doc-1"));
        long c = repo.crearProyecto(baseProyecto(TipoTrabajoGrado.TESIS, "Procesamiento de lenguaje", "est-3", "doc-2"));
        assertTrue(a > 0 && b > 0 && c > 0);
        List<Proyecto> todosDoc1 = repo.listarPorDocente("doc-1", "");
        assertEquals(2, todosDoc1.size());
        List<Proyecto> filtroTitulo = repo.listarPorDocente("doc-1", "visión");
        assertEquals(1, filtroTitulo.size());
        assertEquals("Visión por computador", filtroTitulo.get(0).getTitulo());
        List<Proyecto> filtroEstudiante = repo.listarPorDocente("doc-1", "Ana");
        assertEquals(1, filtroEstudiante.size());
    }
    
    @Test
    void countProyectosByEstadoYTipo() throws SQLException {
        assertEquals(0, repo.countProyectosByEstadoYTipo("TESIS", EstadoProyecto.EN_TRAMITE, "doc-1"));

        long p1 = repo.crearProyecto(baseProyecto(TipoTrabajoGrado.TESIS, "Proyecto 1", "est-1", "doc-1"));
        long p2 = repo.crearProyecto(baseProyecto(TipoTrabajoGrado.TESIS, "Proyecto 2", "est-2", "doc-1"));
        assertTrue(p1 > 0 && p2 > 0);

        long p3 = repo.crearProyecto(baseProyecto(TipoTrabajoGrado.TESIS, "Proyecto rechazado", "est-3", "doc-1"));
        assertTrue(p3 > 0);
        try (PreparedStatement ps = conn.prepareStatement("UPDATE Proyecto SET estado='TERMINADO' WHERE id=?")) {
            ps.setLong(1, p3);
            ps.executeUpdate();
        }

        long p4 = repo.crearProyecto(baseProyecto(TipoTrabajoGrado.PRACTICA_PROFESIONAL, "Práctica 1", "est-1", "doc-1"));
        assertTrue(p4 > 0);

        assertEquals(2, repo.countProyectosByEstadoYTipo("TESIS", EstadoProyecto.EN_TRAMITE, "doc-1"));
        assertEquals(1, repo.countProyectosByEstadoYTipo("TESIS", EstadoProyecto.TERMINADO, "doc-1"));
        assertEquals(1, repo.countProyectosByEstadoYTipo("PRACTICA_PROFESIONAL", EstadoProyecto.EN_TRAMITE, "doc-1"));
    }

    private static void initSchema(Connection c) throws Exception {
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
        String sqlUsuario = """
            CREATE TABLE IF NOT EXISTS Usuario (
              id         TEXT PRIMARY KEY,
              correo     TEXT NOT NULL UNIQUE,
              contrasena TEXT NOT NULL,
              rol        INTEGER NOT NULL,
              nombre     TEXT NOT NULL,
              apellido   TEXT NOT NULL,
              programa   INTEGER NOT NULL,
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
              tipo           TEXT NOT NULL CHECK (tipo IN ('FORMATO_A','ANTEPROYECTO','FINAL','OTRO')),
              nro_version    INTEGER NOT NULL CHECK (nro_version >= 1),
              nombre_archivo TEXT NOT NULL CHECK (lower(nombre_archivo) LIKE '%.pdf'),
              fecha_subida   TEXT NOT NULL DEFAULT (datetime('now')),
              blob           BLOB NOT NULL,
              estado         TEXT NOT NULL CHECK (estado IN ('PENDIENTE','APROBADO','RECHAZADO')) DEFAULT 'PENDIENTE',
              FOREIGN KEY (proyecto_id) REFERENCES Proyecto(id) ON UPDATE CASCADE ON DELETE CASCADE,
              UNIQUE (proyecto_id, tipo, nro_version)
            );
        """;
        try (Statement s = c.createStatement()) {
            s.execute(fkOn);
            s.execute(sqlRol);
            s.execute(sqlPrograma);
            s.execute(sqlUsuario);
            s.execute(sqlProyecto);
            s.execute(sqlArchivo);
        }
    }

    private static void cleanTables(Connection c) throws Exception {
        try (Statement s = c.createStatement()) {
            s.execute("DELETE FROM Archivo");
            s.execute("DELETE FROM Proyecto");
            s.execute("DELETE FROM Usuario");
            s.execute("DELETE FROM Rol");
            s.execute("DELETE FROM Programa");
        }
    }

    private static void seedCatalogos(Connection c) throws Exception {
        try (Statement s = c.createStatement()) {
            s.execute("INSERT OR IGNORE INTO Rol(idRol, tipo) VALUES (1,'Estudiante'),(2,'Docente'),(3,'Coordinador');");
            s.execute("""
                INSERT OR IGNORE INTO Programa(idPrograma, tipo) VALUES
                (1,'Ingenieria_de_Sistemas'),
                (2,'Ingenieria_Electronica_y_Telecomunicaciones'),
                (3,'Automatica_Industrial'),
                (4,'Tecnologia_en_Telematica');
            """);
        }
    }

    private static void seedUsuarios(Connection c) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO Usuario(id, correo, contrasena, rol, nombre, apellido, programa, celular)
            VALUES (?, ?, 'hash', ?, ?, ?, 1, null)
        """)) {
            ps.setString(1, "est-1"); ps.setString(2, "e1@unicauca.edu.co"); ps.setInt(3, 1); ps.setString(4, "Ana"); ps.setString(5, "Pérez"); ps.executeUpdate();
            ps.setString(1, "est-2"); ps.setString(2, "e2@unicauca.edu.co"); ps.setInt(3, 1); ps.setString(4, "Luis"); ps.setString(5, "Gómez"); ps.executeUpdate();
            ps.setString(1, "est-3"); ps.setString(2, "e3@unicauca.edu.co"); ps.setInt(3, 1); ps.setString(4, "María"); ps.setString(5, "López"); ps.executeUpdate();
            ps.setString(1, "doc-1"); ps.setString(2, "d1@unicauca.edu.co"); ps.setInt(3, 2); ps.setString(4, "Carlos"); ps.setString(5, "Rojas"); ps.executeUpdate();
            ps.setString(1, "doc-2"); ps.setString(2, "d2@unicauca.edu.co"); ps.setInt(3, 2); ps.setString(4, "Sofía"); ps.setString(5, "Martínez"); ps.executeUpdate();
        }
    }

    private Proyecto baseProyecto(TipoTrabajoGrado tipo, String titulo, String estudianteId, String docenteId) {
        Proyecto p = new Proyecto();
        p.setTipo(tipo);
        p.setEstado(EstadoProyecto.EN_TRAMITE);
        p.setTitulo(titulo);
        p.setEstudianteId(estudianteId);
        p.setDocenteId(docenteId);
        return p;
    }

}
