package co.unicauca.workflow.degree_project.infra.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.function.Supplier;

import co.unicauca.workflow.degree_project.domain.services.IPasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class Argon2PasswordHasherTest {

    private final IPasswordHasher hasher = new Argon2PasswordHasher();

    private static boolean isCleared(char[] a) {
        for (char c : a) {
            if (c != '\0') {
                return false;
            }
        }
        return true;
    }

    @Test
    void hashAndVerify_ok() {
        char[] pwd = "PruebaFuerte#2025".toCharArray();
        String h = hasher.hash(pwd);
        assertNotNull(h);
        assertTrue(h.startsWith("$argon2id$"));
        assertTrue(isCleared(pwd), "pwd debe quedar borrado tras hash()");

        char[] pwd2 = "PruebaFuerte#2025".toCharArray();
        assertTrue(hasher.verify(pwd2, h));
        assertTrue(isCleared(pwd2), "pwd2 debe quedar borrado tras verify()");
    }

    @Test
    void samePassword_producesDifferentHashes() {
        String h1 = hasher.hash("SamePass#1".toCharArray());
        String h2 = hasher.hash("SamePass#1".toCharArray());
        assertNotEquals(h1, h2);
    }

    @Test
    void verify_wrongPassword_returnsFalse() {
        String h = hasher.hash("Hola#2025".toCharArray());
        assertFalse(hasher.verify("Adios#2025".toCharArray(), h));
    }

    @Test
    void verify_malformedHash_returnsFalse() {
        char[] pwd = "Algo#123".toCharArray();
        assertFalse(hasher.verify(pwd, "no-es-hash"));
        assertTrue(isCleared(pwd));

    }

    @Test
    void hash_nullOrEmpty_throwsIAE() {
        var ex1 = assertThrows(IllegalArgumentException.class,
                () -> {
                    hasher.hash((char[]) null);
                });
        assertEquals("La contraseña no puede ser null o vacía", ex1.getMessage());

        var ex2 = assertThrows(IllegalArgumentException.class,
                () -> {
                    hasher.hash(new char[0]);
                });
        assertEquals("La contraseña no puede ser null o vacía", ex2.getMessage());
    }

    @Test
    @Timeout(5)
    void performance_withinReasonableBudget_avg() {

        char[] base = "Bench#2025".toCharArray();
        try {
            Supplier<char[]> fresh = () -> Arrays.copyOf(base, base.length);

            for (int i = 0; i < 2; i++) {
                String h = hasher.hash(fresh.get());
                assertTrue(hasher.verify(fresh.get(), h));
            }

            int runs = 5;
            long totalHashNs = 0, totalVerifyNs = 0;

            for (int i = 0; i < runs; i++) {
                long t1 = System.nanoTime();
                String h = hasher.hash(fresh.get());
                long t2 = System.nanoTime();

                long v1 = System.nanoTime();
                boolean ok = hasher.verify(fresh.get(), h);
                long v2 = System.nanoTime();

                assertTrue(ok);
                totalHashNs += (t2 - t1);
                totalVerifyNs += (v2 - v1);
            }

            double avgHashMs = (totalHashNs / 1_000_000.0) / runs;
            double avgVerifyMs = (totalVerifyNs / 1_000_000.0) / runs;

            assertTrue(avgHashMs < 1000, "hash lento: " + avgHashMs + " ms");
            assertTrue(avgVerifyMs < 1000, "verify lento: " + avgVerifyMs + " ms");
        } finally {
            Arrays.fill(base, '\0');
        }
    }

}
