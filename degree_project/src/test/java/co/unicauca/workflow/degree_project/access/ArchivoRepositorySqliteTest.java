package co.unicauca.workflow.degree_project.access;

import co.unicauca.workflow.degree_project.domain.models.Archivo;
import co.unicauca.workflow.degree_project.domain.models.EstadoArchivo;
import co.unicauca.workflow.degree_project.domain.models.TipoArchivo;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArchivoRepositorySqliteTest {

    static Connection conn;
    ArchivoRepositorySqlite repoArchivos;
    ProyectoRepositorySqlite repoProyectos;
    long proyectoId;

    @BeforeAll
    static void setUpAll() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
        }
        initSchema(conn);
        seedCatalogos(conn);
    }

    @BeforeEach
    void setUp() throws Exception {
        cleanTables(conn);
        seedUsuarios(conn);
        repoArchivos = new ArchivoRepositorySqlite(conn);
        repoProyectos = new ProyectoRepositorySqlite(conn);
        proyectoId = crearProyectoDummy(conn, "doc-1", "est-1", "TESIS", "Proyecto X");
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        if (conn != null) conn.close();
    }

    @Test
    void insertar_v1_y_obtener_max_version() {
        assertEquals(0, repoArchivos.getMaxVersionFormatoA(proyectoId));
        Archivo a1 = archivo(proyectoId, 1, "v1.pdf", "PDF V1".getBytes(StandardCharsets.UTF_8));
        repoArchivos.insertarFormatoA(a1);
        assertEquals(1, repoArchivos.getMaxVersionFormatoA(proyectoId));
    }

    @Test
    void insertar_varias_versiones_incrementa_max_version() {
        repoArchivos.insertarFormatoA(archivo(proyectoId, 1, "v1.pdf", "V1".getBytes()));
        repoArchivos.insertarFormatoA(archivo(proyectoId, 2, "v2.pdf", "V2".getBytes()));
        assertEquals(2, repoArchivos.getMaxVersionFormatoA(proyectoId));
    }

    @Test
    void obtener_archivo_por_id() throws Exception {
        repoArchivos.insertarFormatoA(archivo(proyectoId, 1, "v1.pdf", "CONTENIDO".getBytes()));
        long archivoId = lastArchivoId(conn);
        Archivo got = repoArchivos.obtenerArchivo(archivoId);
        assertNotNull(got);
        assertEquals("v1.pdf", got.getNombreArchivo());
        assertEquals(TipoArchivo.FORMATO_A, got.getTipo());
        assertEquals(1, got.getNroVersion());
        assertNotNull(got.getBlob());
        assertEquals(EstadoArchivo.PENDIENTE, got.getEstado());
    }

@Test
void listar_archivos_por_proyecto_ordenados_por_version() {
    repoArchivos.insertarFormatoA(archivo(proyectoId, 1, "v1.pdf", "V1".getBytes()));
    repoArchivos.insertarFormatoA(archivo(proyectoId, 2, "v2.pdf", "V2".getBytes()));
    repoArchivos.insertarFormatoA(archivo(proyectoId, 3, "v3.pdf", "V3".getBytes()));

    List<Archivo> lista = repoArchivos.listarArchivosPorProyecto(proyectoId, TipoArchivo.FORMATO_A);

    assertEquals(3, lista.size());
    assertEquals(1, lista.get(0).getNroVersion());
    assertEquals(2, lista.get(1).getNroVersion());
    assertEquals(3, lista.get(2).getNroVersion());
}


    private static void initSchema(Connection c) throws Exception {
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
            s.execute(sqlRol);
            s.execute(sqlPrograma);
            s.execute(sqlUsuario);
            s.execute(sqlProyecto);
            s.execute(sqlArchivo);
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
            ps.setString(1, "doc-1"); ps.setString(2, "d1@unicauca.edu.co"); ps.setInt(3, 2); ps.setString(4, "Carlos"); ps.setString(5, "Rojas"); ps.executeUpdate();
        }
    }

    private static void cleanTables(Connection c) throws Exception {
        try (Statement s = c.createStatement()) {
            s.execute("DELETE FROM Archivo");
            s.execute("DELETE FROM Proyecto");
            s.execute("DELETE FROM Usuario");
        }
    }

    private static long crearProyectoDummy(Connection c, String docenteId, String estudianteId, String tipo, String titulo) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO Proyecto (tipo, estado, titulo, estudiante_id, docente_id)
            VALUES (?, 'EN_TRAMITE', ?, ?, ?)
        """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tipo);
            ps.setString(2, titulo);
            ps.setString(3, estudianteId);
            ps.setString(4, docenteId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("No se pudo crear proyecto dummy");
    }

    private static long lastArchivoId(Connection c) throws Exception {
        try (Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id FROM Archivo ORDER BY id DESC LIMIT 1")) {
            if (rs.next()) return rs.getLong(1);
        }
        throw new IllegalStateException("Sin archivos");
    }

    private static Archivo archivo(long proyectoId, int version, String nombre, byte[] bytes) {
        Archivo a = new Archivo();
        a.setProyectoId(proyectoId);
        a.setTipo(TipoArchivo.FORMATO_A);
        a.setNroVersion(version);
        a.setNombreArchivo(nombre);
        a.setBlob(bytes);
        a.setEstado(EstadoArchivo.PENDIENTE);
        return a;
    }
}
