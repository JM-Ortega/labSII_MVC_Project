package co.unicauca.workflow.degree_project.domain.services;

import java.util.Optional;

public interface ISignInService {
    Optional<AuthResult> validarSesion(String email, char[] passwordIngresada);
}

