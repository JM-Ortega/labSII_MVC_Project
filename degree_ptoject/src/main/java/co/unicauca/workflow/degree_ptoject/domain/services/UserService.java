package co.unicauca.workflow.degree_ptoject.domain.services;


import co.unicauca.workflow.degree_ptoject.access.IUserRepository;
import co.unicauca.workflow.degree_ptoject.domain.models.User;
import jakarta.validation.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

public class UserService implements IRegistrationService, ISignInService {

    private final IUserRepository repo;
    private final IPasswordHasher hasher;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public UserService(IUserRepository repo, IPasswordHasher hasher) {
        this.repo = repo;
        this.hasher = hasher;
    }
    /*
    @Override
    public RegistrationResult register(RegisterUserDTO dto) {

        if (dto == null) {
            return RegistrationResult.fail("Datos de registro vacíos");
        }

        Set<ConstraintViolation<RegisterUserDTO>> v = validator.validate(dto);
        if (!v.isEmpty()) {
            Map<String, String> fe = new LinkedHashMap<>();
            Map<String, Integer> prioPorCampo = new LinkedHashMap<>();

            for (var e : v) {
                String field = e.getPropertyPath().toString();
                int pr = prioridad(e);
                Integer actual = prioPorCampo.get(field);
                if (actual == null || pr < actual) {
                    prioPorCampo.put(field, pr);
                    fe.put(field, e.getMessage());
                }
            }
            return RegistrationResult.fail("Errores de validación", fe);

        }

        String email = dto.getEmail().trim().toLowerCase();

        if (repo.existsByEmail(email)) {
            return RegistrationResult.fail("El email ya está registrado.", Map.of("email", "El email ya está registrado."));
        }

        String hash = hasher.hash(dto.getPassword().toCharArray());

        User u = new User(
                dto.getNombres().trim(),
                dto.getApellidos().trim(),
                (dto.getCelular() == null || dto.getCelular().isBlank()) ? null : dto.getCelular().trim(),
                dto.getPrograma(),
                dto.getRol(),
                email,
                hash
        );

        boolean ok = repo.save(u);
        return ok ? RegistrationResult.ok(u.getId())
                : RegistrationResult.fail("No se pudo guardar el usuario.");
    }*/

    private static int prioridad(ConstraintViolation<?> v) {
        Class<?> ann = v.getConstraintDescriptor().getAnnotation().annotationType();

        if (ann == jakarta.validation.constraints.NotBlank.class
                || ann == jakarta.validation.constraints.NotNull.class) {
            return 1;
        }

        if (ann == jakarta.validation.constraints.Size.class) {
            return 2;
        }
        if (ann == jakarta.validation.constraints.Email.class) {
            return 3;
        }
        if (ann == jakarta.validation.constraints.Pattern.class) {
            return 4;
        }

        return 99;
    }

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
}
