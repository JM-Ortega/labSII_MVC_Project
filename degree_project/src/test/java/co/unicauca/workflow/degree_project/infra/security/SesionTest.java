package co.unicauca.workflow.degree_project.infra.security;

import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SesionTest {

    @BeforeEach
    void resetSesion() {
        Sesion.getInstancia().limpiar();
    }

    @Test
    void singletonSiempreDevuelveLaMismaInstancia() {
        Sesion s1 = Sesion.getInstancia();
        Sesion s2 = Sesion.getInstancia();
        assertSame(s1, s2, "Sesion debería ser singleton");
    }

    @Test
    void setYGetUsuarioActual() {
        AuthResult usuario = new AuthResult("u1", "Estudiante", "Ana Pérez", "Automática Industrial", "Ana@unicauca.edu.co");
        Sesion sesion = Sesion.getInstancia();

        sesion.setUsuarioActual(usuario);

        assertNotNull(sesion.getUsuarioActual());
        assertEquals("u1", sesion.getUsuarioActual().userId());
        assertEquals("Estudiante", sesion.getUsuarioActual().rol());
    }

    @Test
    void limpiarDejaUsuarioEnNull() {
        AuthResult usuario = new AuthResult("u2", "Docente", "Carlos Rojas", "Automática Industrial", "Carlos@unicauca.edu.co");
        Sesion sesion = Sesion.getInstancia();
        sesion.setUsuarioActual(usuario);

        sesion.limpiar();

        assertNull(sesion.getUsuarioActual(), "Después de limpiar debe quedar en null");
    }
}


