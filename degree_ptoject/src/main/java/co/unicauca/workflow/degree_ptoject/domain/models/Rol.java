package co.unicauca.workflow.degree_ptoject.domain.models;

public enum Rol {
    Estudiante("Estudiante"),
    Docente("Docente");
    
    private final String displayName;
    
    Rol(String displayName) { 
        this.displayName = displayName; }
    
    
    @Override
    public String toString() {
        return displayName;
    }
}
