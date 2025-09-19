package co.unicauca.workflow.degree_project.domain.services;

import co.unicauca.workflow.degree_project.presentation.dto.ProjectArchivoDTO;
import java.util.List;

/**
 *
 * @author Maryuri
 */
public interface IUserService {
    List<ProjectArchivoDTO> getDatosEstudiante(String estudianteId);
    
}
