package co.unicauca.workflow.degree_project.access;

import co.unicauca.workflow.degree_project.domain.models.Proyecto;
import co.unicauca.workflow.degree_project.domain.models.User;
import co.unicauca.workflow.degree_project.domain.services.AuthResult;
import java.util.List;

import java.util.Optional;


public interface IUserRepository {
    boolean save(User newUser);
    
    String getRol(String email, char[] passwordIngresada);
    
    String getPassword(String email);
    
    boolean validarIngrereso(String email, char[] passwordIngresada);

    boolean existsByEmail(String email);
    
    String getName(String email);
    
    public Optional<AuthResult> authenticate(String email, char[] passwordIngresada);

}
