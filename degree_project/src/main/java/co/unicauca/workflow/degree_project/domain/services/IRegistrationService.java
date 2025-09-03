/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.workflow.degree_project.domain.services;

import co.unicauca.workflow.degree_project.domain.models.Programa;
import co.unicauca.workflow.degree_project.domain.models.Rol;

/**
 *
 * @author Ortega
 */
public interface IRegistrationService {

    RegistrationResult register(
            String nombres,
            String apellidos,
            String email,
            String password,
            Programa programa,
            Rol rol,
            String celular
    );
}
