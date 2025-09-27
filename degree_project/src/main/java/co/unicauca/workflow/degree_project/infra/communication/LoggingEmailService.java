package co.unicauca.workflow.degree_project.infra.communication;

import java.util.logging.Logger;

public class LoggingEmailService implements IEmailService {
    private static final Logger log = Logger.getLogger(LoggingEmailService.class.getName());

    @Override
    public void sendEmail(EmailMessage message) {
        System.out.println("----- SE ENVIA LA SIMULACION DE EMAIL -----");
        System.out.println("De: " + message.sender);
        System.out.println("Para: " + String.join(", ", message.to));
        System.out.println("Asunto: " + message.subject);
        System.out.println("Cuerpo:\n" + message.body);
        System.out.println("----- FINALIZA LA SIMULACION DE EMAIL -----");
    }
}
