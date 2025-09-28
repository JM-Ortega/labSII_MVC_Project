package co.unicauca.workflow.degree_project.domain.models;

public enum Rol {
    Estudiante("Estudiante"),
    Docente("Docente"),
    Coordinador("Coordinador");
    
    private final String displayName;
    
    Rol(String displayName) { 
        this.displayName = displayName; }
    
    
    @Override
    public String toString() {
        return displayName;
    }
}
