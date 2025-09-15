package co.unicauca.workflow.degree_project.domain.services;

import co.unicauca.workflow.degree_project.access.IUserRepository;
import co.unicauca.workflow.degree_project.domain.models.Programa;
import co.unicauca.workflow.degree_project.domain.models.Rol;
import co.unicauca.workflow.degree_project.domain.models.User;
import static co.unicauca.workflow.degree_project.infra.operation.RegistrationValidator.validate;

import java.util.Map;

import java.util.Arrays;

public class UserService implements IRegistrationService, ISignInService, IUserService {

    private final IUserRepository repo;
    private final IPasswordHasher hasher;

    public UserService(IUserRepository repo, IPasswordHasher hasher) {
        this.repo = repo;
        this.hasher = hasher;
    }

    @Override
    public RegistrationResult register(String nombres,
            String apellidos,
            String email,
            String password,
            Programa programa,
            Rol rol,
            String celular) {
        // Normalizar entradas
        String nom = safe(nombres);
        String ape = safe(apellidos);
        String mail = safe(email).toLowerCase();
        String cel = emptyToNull(celular);

        //Validación de formato/reglas simples
        Map<String, String> fe = validate(nom, ape, mail, password, programa, rol, cel);
        if (!fe.isEmpty()) {
            return RegistrationResult.fail("Errores de validación", fe);
        }

        //Regla con acceso a infraestructura: email único
        if (repo.existsByEmail(mail)) {
            return RegistrationResult.fail("El email ya está registrado.",
                    Map.of("email", "El email ya está registrado."));
        }

        // 3) Hash y persistencia
        char[] pwd = password.toCharArray();
        String hash = hasher.hash(pwd);
        Arrays.fill(pwd, '\0'); // limpiar en memoria

        User u = new User(
                nom,
                ape,
                cel,
                programa,
                rol,
                mail,
                hash
        );

        boolean ok = repo.save(u);
        return ok ? RegistrationResult.ok(u.getId())
                : RegistrationResult.fail("No se pudo guardar el usuario.");
    }

    // Helpers locales (útiles para normalizar)
    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static String emptyToNull(String s) {
        String t = safe(s);
        return t.isEmpty() ? null : t;
    }

    // ===== ISignInService =====
    @Override
    public boolean validarSesion(String email, char[] passwordIngresada) {
        boolean flag = repo.validarIngrereso(email, passwordIngresada);
        return flag;
    }

    @Override
    public String getRol(String email, char[] passwordIngresada) {
        String rol = repo.getRol(email, passwordIngresada);
        return rol;
    }

    @Override
    public int validacion(String usuario, char[] passwordIngresada) {
        //Toca pq despues de usar verify se me borra el array
        char[] copia = Arrays.copyOf(passwordIngresada, passwordIngresada.length);

        boolean flag = validarSesion(usuario, passwordIngresada);
        if (!flag) {
            return 0;
        } else {
            switch (getRol(usuario, copia)) {
                case "Estudiante":
                    return 1;
                case "Docente":
                    return 2;
            }
        }
        return 0;
    }

    @Override
    public String getName(String email) {
        String nombre = repo.getName(email);
        return nombre;
    }
}
