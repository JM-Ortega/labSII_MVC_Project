package co.unicauca.workflow.degree_project.infra.security;

import co.unicauca.workflow.degree_project.domain.services.AuthResult;

/**
 *
 * @author Maryuri
 */
public class Sesion {
    private static Sesion instancia;
    private static AuthResult usuarioActual;
    
    private Sesion(){}
    
    public static Sesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }
    
    public static void setUsuarioActual(AuthResult usuario){
        usuarioActual = usuario;
    }
    
    public static AuthResult getUsuarioActual(){
        return usuarioActual;
    }
    
    public static void limpiarSesion() {
        usuarioActual = null;
    }
}
