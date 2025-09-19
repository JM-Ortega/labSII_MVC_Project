package co.unicauca.workflow.degree_project.domain.models;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Proyecto {
    private final SimpleStringProperty nombreProyecto;
        private final SimpleStringProperty nombreProfesor;
        private final SimpleStringProperty tipo;
        private final SimpleStringProperty fecha;
        private final SimpleStringProperty estado;
        private final SimpleObjectProperty<byte[]> archivoPdf;

        public Proyecto(String nombreProyecto, String nombreProfesor, String tipo, String fecha, String estado, byte[] archivoPdf) {
            this.nombreProyecto = new SimpleStringProperty(nombreProyecto);
            this.nombreProfesor = new SimpleStringProperty(nombreProfesor);
            this.tipo = new SimpleStringProperty(tipo);
            this.fecha = new SimpleStringProperty(fecha);
            this.estado = new SimpleStringProperty(estado);
            this.archivoPdf = new SimpleObjectProperty<>(archivoPdf);
        }

        public String getNombreProyecto() { return nombreProyecto.get(); }
        public String getNombreProfesor() { return nombreProfesor.get(); }
        public String getTipo() { return tipo.get(); }
        public String getFecha() { return fecha.get(); }
        public String getEstado() { return estado.get(); }
        public byte[] getArchivoPdf() { return archivoPdf.get(); }
}
