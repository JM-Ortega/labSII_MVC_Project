package co.unicauca.workflow.degree_project.infra.security;

import co.unicauca.workflow.degree_project.domain.services.AuthResult;

/**
 *
 * @author Maryuri
 */
public class Sesion {
    private static AuthResult usuarioActual;
    
    private Sesion(){}
    
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
