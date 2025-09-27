package co.unicauca.workflow.degree_project.infra.operation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfValidatorTest {

    @Test
    void validPdf_noException() {
        assertDoesNotThrow(() ->
                PdfValidator.assertPdf("documento.pdf", "contenido".getBytes()));
    }

    @Test
    void invalidExtension_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                PdfValidator.assertPdf("documento.txt", "contenido".getBytes()));
        assertTrue(ex.getMessage().contains("archivo .pdf"));
    }

    @Test
    void nullName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                PdfValidator.assertPdf(null, "contenido".getBytes()));
        assertTrue(ex.getMessage().contains("archivo .pdf"));
    }

    @Test
    void emptyBytes_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                PdfValidator.assertPdf("documento.pdf", new byte[0]));
        assertTrue(ex.getMessage().contains("vacío"));
    }

    @Test
    void nullBytes_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                PdfValidator.assertPdf("documento.pdf", null));
        assertTrue(ex.getMessage().contains("vacío"));
    }
}
