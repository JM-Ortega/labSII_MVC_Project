package co.unicauca.workflow.degree_project.infra.communication;

import java.util.ArrayList;
import java.util.List;

public class EmailMessage {
    public List<String> to = new ArrayList<>();
    public String subject;
    public String body;

    public EmailMessage(String to, String subject, String body) {
        this.to.add(to);
        this.subject = subject;
        this.body = body;
    }
}
