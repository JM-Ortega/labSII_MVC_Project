package co.unicauca.workflow.degree_ptoject.infra.operation;

import co.unicauca.workflow.degree_ptoject.domain.models.Programa;
import co.unicauca.workflow.degree_ptoject.domain.models.Rol;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class RegistrationValidator {

    private RegistrationValidator() {
    }

    private static final Pattern EMAIL_UNICAUCA
            = Pattern.compile("^[A-Za-z0-9._%+-]+@unicauca\\.edu\\.co$", Pattern.CASE_INSENSITIVE);

    // ≥6, al menos un dígito, una mayúscula y un carácter especial del set
    private static final Pattern PASSWORD
            = Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!¿?*._-]).{6,}$");

    private static final Pattern CEL10 = Pattern.compile("^\\d{10}$");

    public static Map<String, String> validate(String nombres, String apellidos, String email,
            String password, Programa programa, Rol rol, String celular) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(nombres)) {
            errors.put("nombres", "El nombre es obligatorio");
        }
        if (isBlank(apellidos)) {
            errors.put("apellidos", "El apellido es obligatorio");
        }

        if (isBlank(email)) {
            errors.put("email", "El email es obligatorio");
        } else if (!EMAIL_UNICAUCA.matcher(email.trim()).matches()) {
            errors.put("email", "El correo debe ser @unicauca.edu.co");
        }

        if (isBlank(password)) {
            errors.put("password", "La contraseña es obligatoria");
        } else if (!PASSWORD.matcher(password).matches()) {
            errors.put("password", "La contraseña no cumple con las reglas de seguridad:");
        }

        if (!isBlank(celular) && !CEL10.matcher(celular.trim()).matches()) {
            errors.put("celular", "Si ingresas celular, debe tener 10 dígitos");
        }

        if (programa == null) {
            errors.put("programa", "El programa es obligatorio");
        }
        if (rol == null) {
            errors.put("rol", "El rol es obligatorio");
        }

        return errors;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
