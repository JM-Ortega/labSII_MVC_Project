package co.unicauca.workflow.degree_project.infra.security;

import co.unicauca.workflow.degree_project.domain.services.AuthResult;

public class Sesion {
    private static Sesion instancia;
    private AuthResult usuarioActual;

    private Sesion() {}

    public static Sesion getInstancia() {
        if (instancia == null) instancia = new Sesion();
        return instancia;
    }

    public AuthResult getUsuarioActual() { return usuarioActual; }
    public void setUsuarioActual(AuthResult usuario) { this.usuarioActual = usuario; }
    public void limpiar() { this.usuarioActual = null; }
}
