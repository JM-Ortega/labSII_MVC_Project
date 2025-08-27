package co.unicauca.workflow.degree_ptoject.access;

import co.unicauca.workflow.degree_ptoject.domain.models.User;



public interface IUserRepository {
    boolean save(User newUser);
    
    String getRol(String email, char[] passwordIngresada);
    
    String getPassword(String email);
    
    boolean validarIngrereso(String email, char[] passwordIngresada);

    boolean existsByEmail(String email);

}
