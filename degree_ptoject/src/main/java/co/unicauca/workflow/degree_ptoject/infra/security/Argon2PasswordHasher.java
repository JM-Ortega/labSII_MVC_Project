package co.unicauca.workflow.degree_ptoject.infra.security;


import co.unicauca.workflow.degree_ptoject.domain.services.IPasswordHasher;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

public class Argon2PasswordHasher implements IPasswordHasher {

    // Ajusta para ~200–500 ms
    private static final int ITERATIONS = 3;      // t
    private static final int MEMORY_KB = 65536;  // m
    private static final int PARALLELISM = 1;      // p

    @Override
    public String hash(char[] rawPassword) {
        if (rawPassword == null || rawPassword.length == 0) {
            throw new IllegalArgumentException("La contraseña no puede ser null o vacía");
        }
        Argon2 a = Argon2Factory.create(Argon2Types.ARGON2id);
        try {
            return a.hash(ITERATIONS, MEMORY_KB, PARALLELISM, rawPassword);
        } finally {
            a.wipeArray(rawPassword);
        }
    }

    @Override
    public boolean verify(char[] rawPassword, String hashedPassword) {
        if (rawPassword == null || rawPassword.length == 0) return false;
        if (hashedPassword == null || hashedPassword.isBlank()) return false;

        Argon2 a = Argon2Factory.create(Argon2Types.ARGON2id);
        try {
            return a.verify(hashedPassword, rawPassword);
        } catch (RuntimeException e) {
            return false;
        } finally {
            a.wipeArray(rawPassword);
        }
    }
}
