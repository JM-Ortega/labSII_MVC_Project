package co.unicauca.workflow.degree_project.infra.communication;

public class EmailMessage {
    public String to;
    public String subject;
    public String body;
    public String sender;

    public EmailMessage(String from, String to, String subject, String body) {
        this.sender = from;
        this.to=to;
        this.subject = subject;
        this.body = body;
    }

    public String getTo() {
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
