package co.unicauca.workflow.degree_project.infra.communication;

import java.util.ArrayList;
import java.util.List;

public class EmailMessage {
    public List<String> to = new ArrayList<>();
    public String subject;
    public String body;
    public String sender;

    public EmailMessage(String from, String to, String subject, String body) {
        this.sender = from;
        this.to.add(to);
        this.subject = subject;
        this.body = body;
    }

    public List<String> getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getSender() {
        return sender;
    }
}
