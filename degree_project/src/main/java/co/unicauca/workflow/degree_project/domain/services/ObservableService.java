package co.unicauca.workflow.degree_project.domain.services;

public interface ObservableService {
    void addObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}
