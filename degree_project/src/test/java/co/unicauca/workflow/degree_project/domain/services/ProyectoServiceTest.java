package co.unicauca.workflow.degree_project.domain.services;

import co.unicauca.workflow.degree_project.access.ArchivoRepositorySqlite;
import co.unicauca.workflow.degree_project.access.ProyectoRepositorySqlite;
import co.unicauca.workflow.degree_project.domain.models.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.*;

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

    private static Proyecto baseProyecto(TipoTrabajoGrado tipo, String titulo, String estudianteIdOrCorreo, String docenteId) {
        Proyecto p = new Proyecto();
        p.setTipo(tipo);
        p.setEstado(EstadoProyecto.EN_TRAMITE);
        p.setTitulo(titulo);
        p.setEstudianteId(estudianteIdOrCorreo);
        p.setDocenteId(docenteId);
        return p;
    }

    @Test
    void rf2_crear_proyecto_y_v1_ok() {
        Proyecto p = baseProyecto(TipoTrabajoGrado.TESIS, "Sistema X", "est-1", "doc-1");
        Archivo a = pdf("v1.pdf", "PDF V1");
        Proyecto creado = service.crearProyectoConFormatoA(p, a);
        assertTrue(creado.getId() > 0);
        assertEquals(1, service.maxVersionFormatoA(creado.getId()));
        assertTrue(proyectoRepo.existeProyecto(creado.getId()));
    }

    @Test
    void rf2_docente_sin_cupo_bloquea() {
        for (int i = 1; i <= 7; i++) {
            Proyecto p = baseProyecto(TipoTrabajoGrado.TESIS, "T" + i, "est-" + i, "doc-1");
            seedEstudiante("est-" + i, "e" + i + "@unicauca.edu.co");
            service.crearProyectoConFormatoA(p, pdf("v1.pdf", "x"));
        }
        Proyecto p8 = baseProyecto(TipoTrabajoGrado.TESIS, "T8", "est-99", "doc-1");
        seedEstudiante("est-99", "e99@unicauca.edu.co");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.crearProyectoConFormatoA(p8, pdf("v1.pdf", "x")));
        assertTrue(ex.getMessage().toLowerCase().contains("límite")
                || ex.getMessage().toLowerCase().contains("limite"));
    }

    @Test
    void rf2_estudiante_ocupado_bloquea() {
        Proyecto p1 = baseProyecto(TipoTrabajoGrado.TESIS, "T1", "est-1", "doc-1");
        service.crearProyectoConFormatoA(p1, pdf("v1.pdf", "x"));
        Proyecto p2 = baseProyecto(TipoTrabajoGrado.TESIS, "T2", "est-1", "doc-2");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.crearProyectoConFormatoA(p2, pdf("v1.pdf", "y")));
        assertTrue(ex.getMessage().toLowerCase().contains("estudiante"));
    }

    @Test
    void rf4_subir_v2_y_v3_ok_y_v4_bloquea() {
        Proyecto p = baseProyecto(TipoTrabajoGrado.TESIS, "Proyecto A", "est-1", "doc-1");
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
    void rf2_crear_proyecto_con_correo_estudiante_ok() {
        Proyecto p = baseProyecto(TipoTrabajoGrado.TESIS, "Sistema con correo", "e1@unicauca.edu.co", "doc-1");
        Archivo a = pdf("v1.pdf", "PDF V1");
        Proyecto creado = service.crearProyectoConFormatoA(p, a);
        assertTrue(creado.getId() > 0);
        assertEquals(1, service.maxVersionFormatoA(creado.getId()));
    }

    @Test
    void estudianteLibrePorCorreo_ok_y_error_por_rol() {
        assertTrue(service.estudianteLibrePorCorreo("e1@unicauca.edu.co"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.estudianteLibrePorCorreo("doc2@unicauca.edu.co"));
        assertTrue(ex.getMessage().toLowerCase().contains("no pertenece")
                || ex.getMessage().toLowerCase().contains("estudiante"));
    }

    @Test
    void rf2_crear_proyecto_rechaza_correo_de_docente() {
        Proyecto p = baseProyecto(TipoTrabajoGrado.TESIS, "Inválido", "doc2@unicauca.edu.co", "doc-1");
        Archivo a = pdf("v1.pdf", "V1");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearProyectoConFormatoA(p, a));
        assertTrue(ex.getMessage().toLowerCase().contains("correo")
                || ex.getMessage().toLowerCase().contains("estudiante"));
    }

    @Test
    void crear_proyecto_practica_con_carta_ok() {
        Proyecto p = baseProyecto(TipoTrabajoGrado.PRACTICA_PROFESIONAL, "Práctica con carta", "e1@unicauca.edu.co", "doc-1");
        Archivo formatoA = pdf("formatoA.pdf", "A");
        Archivo carta = pdf("carta.pdf", "C");
        carta.setTipo(TipoArchivo.CARTA_ACEPTACION);
        long id = service.crearProyectoConArchivos(p, java.util.List.of(formatoA, carta));
        assertTrue(id > 0);
        assertEquals(1, service.maxVersionFormatoA(id));
    }

    @Test
    void subir_carta_aceptacion_ok() {
        var creado = service.crearProyectoConFormatoA(
                baseProyecto(TipoTrabajoGrado.TESIS, "Con carta luego", "e1@unicauca.edu.co", "doc-1"),
                pdf("v1.pdf", "A")
        );
        Archivo carta = pdf("carta.pdf", "C");
        carta.setTipo(TipoArchivo.CARTA_ACEPTACION);
        assertDoesNotThrow(() -> service.subirCartaAceptacion(creado.getId(), carta));
        var archivosCarta = service.listarArchivosPorProyecto(creado.getId(), TipoArchivo.CARTA_ACEPTACION);
        assertEquals(1, archivosCarta.size());
        assertEquals("carta.pdf", archivosCarta.get(0).getNombreArchivo());
    }

    @Test
    void docenteTieneCupo_true_y_false() {
        assertTrue(service.docenteTieneCupo("doc-1"));
        for (int i = 1; i <= 7; i++) {
            seedEstudiante("est-x" + i, "ex" + i + "@unicauca.edu.co");
            service.crearProyectoConFormatoA(
                    baseProyecto(TipoTrabajoGrado.TESIS, "T" + i, "est-x" + i, "doc-1"),
                    pdf("v1.pdf", "x")
            );
        }
        assertFalse(service.docenteTieneCupo("doc-1"));
    }

    @Test
    void canResubmit_depende_de_estado_y_versiones() throws Exception {
        var creado = service.crearProyectoConFormatoA(
                baseProyecto(TipoTrabajoGrado.TESIS, "Reintentos", "e1@unicauca.edu.co", "doc-1"),
                pdf("v1.pdf", "A")
        );
        long id = creado.getId();
        assertFalse(service.canResubmit(id));
        var v2 = service.subirNuevaVersionFormatoA(id, pdf("v2.pdf", "B"));
        long v2IdReal = getArchivoIdPorVersion(id, "FORMATO_A", v2.getNroVersion());
        markArchivoEstado(v2IdReal, "OBSERVADO");
        assertTrue(service.canResubmit(id));
        service.subirNuevaVersionFormatoA(id, pdf("v3.pdf", "C"));
        assertFalse(service.canResubmit(id));
    }

    @Test
    void observaciones_helpers() throws Exception {
        var creado = service.crearProyectoConFormatoA(
                baseProyecto(TipoTrabajoGrado.TESIS, "Obs", "e1@unicauca.edu.co", "doc-1"),
                pdf("v1.pdf", "A")
        );
        long id = creado.getId();
        assertFalse(service.tieneObservacionesFormatoA(id));
        assertNull(service.obtenerUltimoFormatoAConObservaciones(id));
        var v2 = service.subirNuevaVersionFormatoA(id, pdf("v2.pdf", "B"));
        long v2IdReal = getArchivoIdPorVersion(id, "FORMATO_A", v2.getNroVersion());
        markArchivoEstado(v2IdReal, "OBSERVADO");
        assertTrue(service.tieneObservacionesFormatoA(id));
        var ultimoObs = service.obtenerUltimoFormatoAConObservaciones(id);
        assertNotNull(ultimoObs);
        assertEquals(v2.getNroVersion(), ultimoObs.getNroVersion());
    }

    @Test
    void auto_rechazo_por_tres_observados() throws Exception {
        var creado = service.crearProyectoConFormatoA(
                baseProyecto(TipoTrabajoGrado.TESIS, "AutoRechazo", "e1@unicauca.edu.co", "doc-1"),
                pdf("v1.pdf", "A")
        );
        long id = creado.getId();
        var v2 = service.subirNuevaVersionFormatoA(id, pdf("v2.pdf", "B"));
        long v2IdReal = getArchivoIdPorVersion(id, "FORMATO_A", v2.getNroVersion());
        markArchivoEstado(v2IdReal, "OBSERVADO");
        var v3 = service.subirNuevaVersionFormatoA(id, pdf("v3.pdf", "C"));
        long v3IdReal = getArchivoIdPorVersion(id, "FORMATO_A", v3.getNroVersion());
        markArchivoEstado(v3IdReal, "OBSERVADO");
        assertEquals(EstadoProyecto.EN_TRAMITE, service.enforceAutoCancelIfNeeded(id));
        insertFormatoAForzadoObs(id);
        assertEquals(EstadoProyecto.RECHAZADO, service.enforceAutoCancelIfNeeded(id));
    }

    @Test
    void listar_y_descargar_basico() {
        var creado = service.crearProyectoConFormatoA(
                baseProyecto(TipoTrabajoGrado.TESIS, "Listado", "e1@unicauca.edu.co", "doc-1"),
                pdf("v1.pdf", "A")
        );
        var lista = service.listarProyectosDocente("doc-1", "");
        assertFalse(lista.isEmpty());
        var archivos = service.listarArchivosPorProyecto(creado.getId(), TipoArchivo.FORMATO_A);
        assertEquals(1, archivos.size());
        var a0 = service.obtenerArchivo(archivos.get(0).getId());
        assertNotNull(a0);
        assertEquals("v1.pdf", a0.getNombreArchivo());
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
                  estado TEXT NOT NULL CHECK (estado IN ('EN_TRAMITE','RECHAZADO','TERMINADO')) DEFAULT 'EN_TRAMITE',
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
                  tipo TEXT NOT NULL CHECK (tipo IN ('FORMATO_A','CARTA_ACEPTACION','ANTEPROYECTO','FINAL','OTRO')),
                  nro_version INTEGER NOT NULL CHECK (nro_version >= 1),
                  nombre_archivo TEXT NOT NULL CHECK (lower(nombre_archivo) LIKE '%.pdf'),
                  fecha_subida TEXT NOT NULL DEFAULT (datetime('now')),
                  blob BLOB NOT NULL,
                  estado TEXT NOT NULL CHECK (estado IN ('PENDIENTE','APROBADO','OBSERVADO')) DEFAULT 'PENDIENTE',
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
            // estudiante base
            ps.setString(1, "est-1"); ps.setString(2, "e1@unicauca.edu.co"); ps.setInt(3, 1); ps.setString(4, "Ana"); ps.setString(5, "Pérez"); ps.executeUpdate();
            // docentes
            ps.setString(1, "doc-1"); ps.setString(2, "d1@unicauca.edu.co"); ps.setInt(3, 2); ps.setString(4, "Carlos"); ps.setString(5, "Rojas"); ps.executeUpdate();
            ps.setString(1, "doc-2"); ps.setString(2, "doc2@unicauca.edu.co"); ps.setInt(3, 2); ps.setString(4, "Lucia");  ps.setString(5, "Mora");  ps.executeUpdate();
            // más estudiantes
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

    // ---------------- Helpers específicos para manipular datos en tests ----------------

    /** Devuelve el id real del archivo por (proyecto, tipo, nro_version) */
    private static long getArchivoIdPorVersion(long proyectoId, String tipo, int nroVersion) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM Archivo WHERE proyecto_id=? AND tipo=? AND nro_version=?")) {
            ps.setLong(1, proyectoId);
            ps.setString(2, tipo);
            ps.setInt(3, nroVersion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new AssertionError("No se encontró el archivo insertado (ver versión/tipo/proyecto).");
    }

    /** Marca el estado de un archivo directamente en la tabla (para simular OBSERVADO/APROBADO) */
    private static void markArchivoEstado(long archivoId, String estado) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE Archivo SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado);
            ps.setLong(2, archivoId);
            assertTrue(ps.executeUpdate() > 0, "No se pudo actualizar estado del archivo");
        }
    }

    /** Inserta una versión de Formato A OBSERVADO de forma forzada (evita límites del service) */
    private static void insertFormatoAForzadoObs(long proyectoId) throws Exception {
        int nextVersion = 1;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(MAX(nro_version),0)+1 FROM Archivo WHERE proyecto_id = ? AND tipo = 'FORMATO_A'")) {
            ps.setLong(1, proyectoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) nextVersion = rs.getInt(1);
            }
        }
        try (PreparedStatement ins = conn.prepareStatement("""
            INSERT INTO Archivo (proyecto_id, tipo, nro_version, nombre_archivo, blob, estado)
            VALUES (?, 'FORMATO_A', ?, ?, ?, 'OBSERVADO')
        """)) {
            ins.setLong(1, proyectoId);
            ins.setInt(2, nextVersion);
            ins.setString(3, "forzado_v"+nextVersion+".pdf");
            ins.setBytes(4, "X".getBytes(StandardCharsets.UTF_8));
            ins.executeUpdate();
        }
    }
}
