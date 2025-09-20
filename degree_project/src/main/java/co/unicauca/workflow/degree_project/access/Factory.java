package co.unicauca.workflow.degree_project.access;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Factory {
    private static Factory instance;
    private static Connection conn;
    private Factory() {
    }

    public static Factory getInstance() {
        if (instance == null) {
            instance = new Factory();
        }
        return instance;
    }
    private Connection connection() {
        if (conn != null) return conn;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:degree_project.db");
            try (var s = conn.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public IUserRepository getRepository(String type) {
        return switch (type) {
            case "default" -> new SqliteRepository(connection());
            default -> throw new IllegalArgumentException("Tipo de repo no soportado: " + type);
        };
    }

    public IProyectoRepository getProyectoRepository(String type) {
        return switch (type) {
            case "default" -> new ProyectoRepositorySqlite(connection());
            default -> throw new IllegalArgumentException("Tipo de repo no soportado: " + type);
        };
    }

    public IArchivoRepository getArchivoRepository(String type) {
        return switch (type) {
            case "default" -> new ArchivoRepositorySqlite(connection());
            default -> throw new IllegalArgumentException("Tipo de repo no soportado: " + type);
        };
    }

    public Connection getConnection() {
        return connection();
    }
}
