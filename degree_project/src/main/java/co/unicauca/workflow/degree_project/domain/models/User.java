package co.unicauca.workflow.degree_project.domain.models;

import java.util.Objects;
import java.util.UUID;

public class User {

    private final String id;
    private String nombres;
    private String apellidos;
    private String celular;
    private Programa programa;
    private Rol rol;
    private static String email;
    private String passwordHash;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String nombres, String apellidos, String celular, Programa programa, Rol rol, String email, String passwordHash) {
        this.id = UUID.randomUUID().toString();
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.celular = celular;
        this.programa = programa;
        this.rol = rol;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombresU) {
        nombres = nombresU;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public Programa getPrograma() {
        return programa;
    }

    public void setPrograma(Programa programa) {
        this.programa = programa;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String usuario) {
        email = usuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nombres=" + nombres +
                ", apellidos=" + apellidos +
                ", celular=" + celular +
                ", programa=" + programa +
                ", rol=" + rol +
                ", email=" + email +
                '}';
    }
}
