package co.unicauca.workflow.degree_project.domain.services;

import co.unicauca.workflow.degree_project.access.IUserRepository;
import co.unicauca.workflow.degree_project.domain.models.Programa;
import co.unicauca.workflow.degree_project.domain.models.Rol;
import co.unicauca.workflow.degree_project.domain.models.User;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static co.unicauca.workflow.degree_project.infra.operation.RegistrationValidator.validate;

public class UserService implements IRegistrationService, ISignInService, IUserService {

    private final IUserRepository repo;
    private final IPasswordHasher hasher;

    public UserService(IUserRepository repo, IPasswordHasher hasher) {
        this.repo = repo;
        this.hasher = hasher;
    }

    // ===== Registro =====
    @Override
    public RegistrationResult register(String nombres,
                                       String apellidos,
                                       String email,
                                       String password,
                                       Programa programa,
                                       Rol rol,
                                       String celular) {
        String nom  = safe(nombres);
        String ape  = safe(apellidos);
        String mail = safe(email).toLowerCase();
        String cel  = emptyToNull(celular);

        Map<String,String> fe = validate(nom, ape, mail, password, programa, rol, cel);
        if (!fe.isEmpty()) {
            return RegistrationResult.fail("Errores de validación", fe);
        }

        if (repo.existsByEmail(mail)) {
            return RegistrationResult.fail("El email ya está registrado.",
                    Map.of("email", "El email ya está registrado."));
        }

        char[] pwd = password.toCharArray();
        String hash = hasher.hash(pwd);
        Arrays.fill(pwd, '\0'); // limpiar en memoria

        User u = new User(nom, ape, cel, programa, rol, mail, hash);

        boolean ok = repo.save(u);
        return ok ? RegistrationResult.ok(u.getId())
                  : RegistrationResult.fail("No se pudo guardar el usuario.");
    }

    // ===== Sign-in =====
    @Override
    public Optional<AuthResult> validarSesion(String email, char[] passwordIngresada) {
        return repo.authenticate(email, passwordIngresada);
    }

    // ===== Otros servicios de usuario =====
    @Override
    public String getName(String email) {
        return repo.getName(email);
    }

    // Helpers
    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
    private static String emptyToNull(String s) {
        String t = safe(s);
        return t.isEmpty() ? null : t;
    }
}
