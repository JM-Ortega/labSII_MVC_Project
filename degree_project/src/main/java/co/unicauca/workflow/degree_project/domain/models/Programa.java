package co.unicauca.workflow.degree_project.domain.models;

public enum Programa {
    Ingenieria_de_Sistemas("Ingeniería de Sistemas"),
    Ingenieria_Electronica_y_Telecomunicaciones("Ingeniería Electrónica y Telecomunicaciones"),
    Automatica_Industrial("Automática Industrial"),
    Tecnologia_en_Telematica("Tecnología en Telemática");

    private final String displayName;

    Programa(String displayName) {
        this.displayName = displayName;
    }
    

    @Override
    public String toString() {
        return displayName;
    }
}