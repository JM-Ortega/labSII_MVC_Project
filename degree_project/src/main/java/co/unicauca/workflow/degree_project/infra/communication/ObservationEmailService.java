package co.unicauca.workflow.degree_project.infra.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObservationEmailService implements IEmailService{
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendEmail(EmailMessage message) {
        log.info("----- SIMULATED EMAIL SEND -----");
        log.info("To: {}", String.join(", ", message.to));
        log.info("Subject: {}", message.subject);
        log.info("Body:\n{}", message.body);
        log.info("----- END SIMULATED EMAIL -----");
    }
}
