package co.unicauca.workflow.degree_ptoject.domain.services;

public interface ISignInService {

    boolean validarSesion(String email, char[] passwordIngresada);

    String getRol(String email, char[] passwordIngresada);

    int validacion(String usuario, char[] passwordIngresada);
}
