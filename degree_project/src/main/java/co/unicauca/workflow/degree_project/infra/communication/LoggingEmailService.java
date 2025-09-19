package co.unicauca.workflow.degree_project.infra.communication;

import java.util.logging.Logger;

public class LoggingEmailService implements IEmailService {
    private static final Logger log = Logger.getLogger(LoggingEmailService.class.getName());

    @Override
    public void sendEmail(EmailMessage message) {
        log.info("----- SIMULATED EMAIL SEND -----");
        log.info("To: " + String.join(", ", message.to));
        log.info("Subject: " + message.subject);
        log.info("Body:\n" + message.body);
        log.info("----- END SIMULATED EMAIL -----");
    }
}
