package co.unicauca.workflow.degree_ptoject.domain.services;

public interface IPasswordHasher {
     String hash(char[] rawPassword);
     boolean verify(char[] plainPassword, String hashedPassword);
}
