package co.unicauca.workflow.degree_project.infra.operation;

import co.unicauca.workflow.degree_project.domain.models.Programa;
import co.unicauca.workflow.degree_project.domain.models.Rol;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationValidatorTest {

    @Test
    void validInput_noErrors() {
        Map<String, String> errors = RegistrationValidator.validate(
                "Ana", "Pérez",
                "ana@unicauca.edu.co",
                "Clave1@",
                Programa.Ingenieria_de_Sistemas,
                Rol.Estudiante,
                "3001234567"
        );

        assertTrue(errors.isEmpty(), "No debería haber errores con datos válidos");
    }

    @Test
    void missingRequiredFields() {
        Map<String, String> errors = RegistrationValidator.validate(
                "", "", "", "",
                null, null, ""
        );

        assertEquals(6, errors.size());
        assertTrue(errors.containsKey("nombres"));
        assertTrue(errors.containsKey("apellidos"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("password"));
        assertTrue(errors.containsKey("programa"));
        assertTrue(errors.containsKey("rol"));
    }

    @Test
    void invalidEmailFormat() {
        Map<String, String> errors = RegistrationValidator.validate(
                "Juan", "Ortega",
                "juan@gmail.com",
                "Clave1@",
                Programa.Ingenieria_de_Sistemas,
                Rol.Estudiante,
                "3001234567"
        );

        assertEquals("El correo debe ser @unicauca.edu.co", errors.get("email"));
    }

    @Test
    void weakPasswordFails() {
        Map<String, String> errors = RegistrationValidator.validate(
                "Ana", "Pérez",
                "ana@unicauca.edu.co",
                "abcdef",
                Programa.Ingenieria_de_Sistemas,
                Rol.Estudiante,
                "3001234567"
        );

        assertTrue(errors.containsKey("password"));
    }

    @Test
    void invalidCellphoneFormat() {
        Map<String, String> errors = RegistrationValidator.validate(
                "Ana", "Pérez",
                "ana@unicauca.edu.co",
                "Clave1@",
                Programa.Ingenieria_de_Sistemas,
                Rol.Estudiante,
                "12345"
        );

        assertEquals("Si ingresas celular, debe tener 10 dígitos", errors.get("celular"));
    }

    @Test
    void cellphoneOptional_okWhenBlank() {
        Map<String, String> errors = RegistrationValidator.validate(
                "Ana", "Pérez",
                "ana@unicauca.edu.co",
                "Clave1@",
                Programa.Ingenieria_de_Sistemas,
                Rol.Estudiante,
                ""
        );

        assertTrue(errors.isEmpty());
    }
}
