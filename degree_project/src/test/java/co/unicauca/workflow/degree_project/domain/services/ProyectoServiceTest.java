package co.unicauca.workflow.degree_project.domain.services;

import co.unicauca.workflow.degree_project.access.ArchivoRepositorySqlite;
import co.unicauca.workflow.degree_project.access.ProyectoRepositorySqlite;
import co.unicauca.workflow.degree_project.domain.models.Archivo;
import co.unicauca.workflow.degree_project.domain.models.EstadoProyecto;
import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.models.TipoArchivo;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProyectoServiceTest {

    static Connection conn;
    ProyectoRepositorySqlite proyectoRepo;
    ArchivoRepositorySqlite  archivoRepo;
    IProyectoService service;

    @BeforeAll
    static void beforeAll() throws Exception {
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
        proyectoRepo = new ProyectoRepositorySqlite(conn);
        archivoRepo  = new ArchivoRepositorySqlite(conn);
        service      = new ProyectoService(proyectoRepo, archivoRepo, conn);
    }

    @AfterAll
    static void afterAll() throws Exception {
        if (conn != null) conn.close();
    }

    @Test
    void rf2_crear_proyecto_y_v1_ok() {
        Proyecto p = baseProyecto("TESIS", "Sistema X", "est-1", "doc-1");
        Archivo  a = pdf("v1.pdf", "PDF V1");

        Proyecto creado = service.crearProyectoConFormatoA(p, a);

        assertTrue(creado.getId() > 0);
        assertEquals(1, service.maxVersionFormatoA(creado.getId()));
        assertTrue(proyectoRepo.existeProyecto(creado.getId()));
    }

    @Test
    void rf2_docente_sin_cupo_bloquea() {
        for (int i = 1; i <= 7; i++) {
            Proyecto p = baseProyecto("TESIS", "T"+i, "est-" + i, "doc-1");
            seedEstudiante("est-"+i, "e"+i+"@unicauca.edu.co");
            service.crearProyectoConFormatoA(p, pdf("v1.pdf", "x"));
        }
        Proyecto p8 = baseProyecto("TESIS", "T8", "est-99", "doc-1");
        seedEstudiante("est-99", "e99@unicauca.edu.co");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.crearProyectoConFormatoA(p8, pdf("v1.pdf", "x")));
        assertTrue(ex.getMessage().toLowerCase().contains("límite") || ex.getMessage().toLowerCase().contains("limite"));
    }

    @Test
    void rf2_estudiante_ocupado_bloquea() {
        Proyecto p1 = baseProyecto("TESIS", "T1", "est-1", "doc-1");
        service.crearProyectoConFormatoA(p1, pdf("v1.pdf", "x"));

        Proyecto p2 = baseProyecto("TESIS", "T2", "est-1", "doc-2");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.crearProyectoConFormatoA(p2, pdf("v1.pdf", "y")));
        assertTrue(ex.getMessage().toLowerCase().contains("estudiante"));
    }

    @Test
    void rf4_subir_v2_y_v3_ok_y_v4_bloquea() {
        Proyecto p = baseProyecto("TESIS", "Proyecto A", "est-1", "doc-1");
        Proyecto creado = service.crearProyectoConFormatoA(p, pdf("v1.pdf", "V1"));
        long id = creado.getId();

        Archivo v2 = service.subirNuevaVersionFormatoA(id, pdf("v2.pdf", "V2"));
        assertEquals(2, v2.getNroVersion());

        Archivo v3 = service.subirNuevaVersionFormatoA(id, pdf("v3.pdf", "V3"));
        assertEquals(3, v3.getNroVersion());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.subirNuevaVersionFormatoA(id, pdf("v4.pdf", "V4")));
        assertTrue(ex.getMessage().toLowerCase().contains("3 versiones"));

        assertEquals(3, service.maxVersionFormatoA(id));
    }
    
    @Test
    void count_proyectos_by_estado_y_tipo() {
        Proyecto p1 = baseProyecto("TESIS", "Proyecto A", "est-1", "doc-1");
        Proyecto p2 = baseProyecto("TESIS", "Proyecto B", "est-2", "doc-1");
        service.crearProyectoConFormatoA(p1, pdf("v1.pdf", "x"));
        service.crearProyectoConFormatoA(p2, pdf("v1.pdf", "y"));

        int total = service.countProyectosByEstadoYTipo("TESIS", "EN_TRAMITE", "doc-1");
        assertEquals(2, total);
    }
    
    @Test
    void listar_formatosA_por_estudiante() {
        Proyecto p = baseProyecto("TESIS", "Proyecto C", "est-1", "doc-1");
        Proyecto creado = service.crearProyectoConFormatoA(p, pdf("v1.pdf", "V1"));
        service.subirNuevaVersionFormatoA(creado.getId(), pdf("v2.pdf", "V2"));
        service.subirNuevaVersionFormatoA(creado.getId(), pdf("v3.pdf", "V3"));

        List<Proyecto> proyectos = service.listarFormatosAPorEstudiante("est-1");

        assertEquals(3, proyectos.size());
    }

    private static Proyecto baseProyecto(String tipo, String titulo, String estudianteId, String docenteId) {
        Proyecto p = new Proyecto();
        p.setTipo(tipo);
        p.setEstado(EstadoProyecto.EN_TRAMITE);
        p.setTitulo(titulo);
        p.setEstudianteId(estudianteId);
        p.setDocenteId(docenteId);
        return p;
    }

    private static Archivo pdf(String nombre, String contenido) {
        Archivo a = new Archivo();
        a.setTipo(TipoArchivo.FORMATO_A);
        a.setNombreArchivo(nombre);
        a.setBlob(contenido.getBytes(StandardCharsets.UTF_8));
        return a;
    }

    private static void initSchema(Connection c) throws Exception {
        try (Statement s = c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS Rol (idRol INTEGER PRIMARY KEY, tipo TEXT NOT NULL UNIQUE);
            """);
            s.execute("""
                CREATE TABLE IF NOT EXISTS Programa (idPrograma INTEGER PRIMARY KEY, tipo TEXT NOT NULL UNIQUE);
            """);
            s.execute("""
                CREATE TABLE IF NOT EXISTS Usuario (
                  id TEXT PRIMARY KEY, correo TEXT NOT NULL UNIQUE, contrasena TEXT NOT NULL, rol INTEGER NOT NULL,
                  nombre TEXT NOT NULL, apellido TEXT NOT NULL, programa INTEGER NOT NULL, celular TEXT,
                  FOREIGN KEY (rol) REFERENCES Rol(idRol), FOREIGN KEY (programa) REFERENCES Programa(idPrograma)
                );
            """);
            s.execute("""
                CREATE TABLE IF NOT EXISTS Proyecto (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  tipo TEXT NOT NULL CHECK (tipo IN ('TESIS','PRACTICA_PROFESIONAL')),
                  estado TEXT NOT NULL CHECK (estado IN ('EN_TRAMITE','CANCELADO','TERMINADO')) DEFAULT 'EN_TRAMITE',
                  titulo TEXT NOT NULL,
                  estudiante_id TEXT NOT NULL,
                  docente_id TEXT NOT NULL,
                  fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
                  FOREIGN KEY (estudiante_id) REFERENCES Usuario(id) ON UPDATE CASCADE ON DELETE RESTRICT,
                  FOREIGN KEY (docente_id)  REFERENCES Usuario(id) ON UPDATE CASCADE ON DELETE RESTRICT
                );
            """);
            s.execute("""
                CREATE TABLE IF NOT EXISTS Archivo (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  proyecto_id INTEGER NOT NULL,
                  tipo TEXT NOT NULL CHECK (tipo IN ('FORMATO_A','ANTEPROYECTO','FINAL','OTRO')),
                  nro_version INTEGER NOT NULL CHECK (nro_version >= 1),
                  nombre_archivo TEXT NOT NULL CHECK (lower(nombre_archivo) LIKE '%.pdf'),
                  fecha_subida TEXT NOT NULL DEFAULT (datetime('now')),
                  blob BLOB NOT NULL,
                  estado TEXT NOT NULL CHECK (estado IN ('PENDIENTE','APROBADO','RECHAZADO')) DEFAULT 'PENDIENTE',
                  FOREIGN KEY (proyecto_id) REFERENCES Proyecto(id) ON UPDATE CASCADE ON DELETE CASCADE,
                  UNIQUE (proyecto_id, tipo, nro_version)
                );
            """);
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
            for (int i = 2; i <= 7; i++) {
                ps.setString(1, "est-" + i);
                ps.setString(2, "e" + i + "@unicauca.edu.co");
                ps.setInt(3, 1);
                ps.setString(4, "Est" + i);
                ps.setString(5, "Test");
                ps.executeUpdate();
            }
        }
    }

    private void seedEstudiante(String id, String correo) {
        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT OR IGNORE INTO Usuario(id, correo, contrasena, rol, nombre, apellido, programa, celular)
            VALUES (?, ?, 'hash', 1, 'Temp', 'Alumno', 1, null)
        """)) {
            ps.setString(1, id);
            ps.setString(2, correo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void cleanTables(Connection c) throws Exception {
        try (Statement s = c.createStatement()) {
            s.execute("DELETE FROM Archivo");
            s.execute("DELETE FROM Proyecto");
            s.execute("DELETE FROM Usuario");
        }
    }
}
