package co.unicauca.workflow.degree_project.infra.operation;

import co.unicauca.workflow.degree_project.domain.services.ITransactionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class SqliteTransactionManager implements ITransactionManager {
    private final Connection conn;
    private Boolean previousAutoCommit = null;

    public SqliteTransactionManager(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void begin() {
        try {
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commit() {
        try {
            conn.commit();
            if (previousAutoCommit != null) conn.setAutoCommit(previousAutoCommit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            previousAutoCommit = null;
        }
    }

    @Override
    public void rollback() {
        try {
            conn.rollback();
            if (previousAutoCommit != null) conn.setAutoCommit(previousAutoCommit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            previousAutoCommit = null;
        }
    }
}
