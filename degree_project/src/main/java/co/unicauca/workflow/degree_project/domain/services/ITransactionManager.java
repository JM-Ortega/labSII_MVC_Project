package co.unicauca.workflow.degree_project.domain.services;

public interface ITransactionManager {
    void begin();
    void commit();
    void rollback();
}
