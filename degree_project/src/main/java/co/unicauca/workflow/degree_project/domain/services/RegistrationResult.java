/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package co.unicauca.workflow.degree_project.domain.services;

import java.util.Map;

/**
 *
 * @author Ortega
 */
public record RegistrationResult(
        boolean ok,
        String message,
        String userId,
        Map<String, String> fieldErrors
        ) {

    public static RegistrationResult ok(String id) {
        return new RegistrationResult(true, "OK", id, Map.of());
    }

    public static RegistrationResult fail(String m) {
        return new RegistrationResult(false, m, null, Map.of());
    }

    public static RegistrationResult fail(String m, Map<String, String> fe) {
        return new RegistrationResult(false, m, null, fe);
    }
}
