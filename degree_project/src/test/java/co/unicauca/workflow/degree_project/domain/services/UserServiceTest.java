package co.unicauca.workflow.degree_project.domain.services;


import co.unicauca.workflow.degree_project.access.IUserRepository;
import co.unicauca.workflow.degree_project.domain.models.Programa;
import co.unicauca.workflow.degree_project.domain.models.Rol;
import co.unicauca.workflow.degree_project.domain.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    IUserRepository repo;

    @Mock
    IPasswordHasher hasher;

    @Captor
    ArgumentCaptor<User> userCaptor;

    @Captor
    ArgumentCaptor<char[]> pwdCaptor;

    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repo, hasher);
    }

    @Test
    void register_ok_normalizaEmail_opcionalCelular_hashea_y_guarda() {
        String nombres = "  Ana  ";
        String apellidos = "  Pérez ";
        String email = " ANA@UNICAUCA.EDU.CO "; // mezcla mayúsculas y espacios
        String password = "Clave1@";
        Programa programa = Programa.Ingenieria_de_Sistemas;
        Rol rol = Rol.Estudiante;
        String celular = "";

        when(repo.existsByEmail("ana@unicauca.edu.co")).thenReturn(false);
        when(hasher.hash(any(char[].class))).thenReturn("HASH_ARGON2");
        when(repo.save(any(User.class))).thenReturn(true);

        RegistrationResult rr = service.register(nombres, apellidos, email, password, programa, rol, celular);

        assertTrue(rr.ok());
        assertNotNull(rr.userId());

        verify(hasher).hash(pwdCaptor.capture());
        char[] pwdSentToHasher = pwdCaptor.getValue();
        for (char c : pwdSentToHasher) {
            assertEquals('\0', c, "El arreglo de password debería quedar limpiado");
        }

        verify(repo).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("Ana", saved.getNombres());
        assertEquals("Pérez", saved.getApellidos());
        assertEquals("ana@unicauca.edu.co", saved.getEmail());
        assertEquals("HASH_ARGON2", saved.getPasswordHash());
        assertEquals(programa, saved.getPrograma());
        assertEquals(rol, saved.getRol());
        assertNull(saved.getCelular());
    }

    @Test
    void register_falla_si_email_duplicado() {
        when(repo.existsByEmail("ana@unicauca.edu.co")).thenReturn(true);

        RegistrationResult rr = service.register(
                "Ana", "Pérez", "ana@unicauca.edu.co", "Clave1@", Programa.Ingenieria_de_Sistemas, Rol.Estudiante, null);

        assertFalse(rr.ok());
        assertEquals("El email ya está registrado.", rr.message());
        Map<String, String> fe = rr.fieldErrors();
        assertNotNull(fe);
        assertTrue(fe.containsKey("email"));

        verify(hasher, never()).hash(any());
        verify(repo, never()).save(any());
    }

    @Test
    void register_falla_por_validacion_email_dominio() {
        RegistrationResult rr = service.register(
                "Ana", "Pérez", "ana@gmail.com", "Clave1@", Programa.Ingenieria_de_Sistemas, Rol.Estudiante, null);

        assertFalse(rr.ok());
        assertNotNull(rr.fieldErrors());
        assertTrue(rr.fieldErrors().containsKey("email"));
        verifyNoInteractions(hasher);
        verify(repo, never()).save(any());
    }

    @Test
    void register_falla_por_password_debil() {
        RegistrationResult rr = service.register(
                "Ana", "Pérez", "ana@unicauca.edu.co", "abcdef", Programa.Ingenieria_de_Sistemas, Rol.Estudiante, null);

        assertFalse(rr.ok());
        assertTrue(rr.fieldErrors().containsKey("password"));
        verifyNoInteractions(hasher);
        verify(repo, never()).save(any());
    }

    @Test
    void register_falla_por_campos_obligatorios() {
        RegistrationResult rr = service.register(
                " ", " ", " ", " ",
                null, null, " ");

        assertFalse(rr.ok());
        assertEquals("Errores de validación", rr.message());
        assertTrue(rr.fieldErrors().containsKey("nombres"));
        assertTrue(rr.fieldErrors().containsKey("apellidos"));
        assertTrue(rr.fieldErrors().containsKey("email"));
        assertTrue(rr.fieldErrors().containsKey("password"));
        assertTrue(rr.fieldErrors().containsKey("programa"));
        assertTrue(rr.fieldErrors().containsKey("rol"));
        verifyNoInteractions(hasher);
        verify(repo, never()).save(any());
    }

    @Test
    void register_falla_si_repoNoGuarda() {
        when(repo.existsByEmail("ana@unicauca.edu.co")).thenReturn(false);
        when(hasher.hash(any())).thenReturn("HASH");
        when(repo.save(any(User.class))).thenReturn(false);

        RegistrationResult rr = service.register(
                "Ana", "Pérez", "ana@unicauca.edu.co", "Clave1@", Programa.Ingenieria_de_Sistemas, Rol.Estudiante, null);

        assertFalse(rr.ok());
        assertEquals("No se pudo guardar el usuario.", rr.message());
    }

    @Test
    void signIn_delega_en_repo() {
        AuthResult expected = new AuthResult("u1", "Estudiante", "Ana Pérez", "Automática Industrial", "Ana@unicauca.edu.co");
        when(repo.authenticate(eq("ana@unicauca.edu.co"), any())).thenReturn(Optional.of(expected));

        Optional<AuthResult> res = service.validarSesion("ana@unicauca.edu.co", "Clave1@".toCharArray());

        assertTrue(res.isPresent());
        assertEquals("u1", res.get().userId());
        verify(repo).authenticate(eq("ana@unicauca.edu.co"), any());
    }

    @Test
    void getName_delega_en_repo() {
        when(repo.getName("ana@unicauca.edu.co")).thenReturn("Ana Pérez");
        assertEquals("Ana Pérez", service.getName("ana@unicauca.edu.co"));
        verify(repo).getName("ana@unicauca.edu.co");
    }
}
