package ru.otus.integration;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import ru.otus.messaging.EmailMessage;
import ru.otus.service.EmailMessageListener;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailMessageListenerTest {

    @Test
    void shouldSendReceivedMessageAsHtmlEmail() throws Exception {
        var mailSender = mock(JavaMailSender.class);
        var mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        var listener = new EmailMessageListener(mailSender);

        listener.receive(new EmailMessage("employee@example.com", "Subject", "<p>Text</p>"));

        verify(mailSender).send(mimeMessage);
        mimeMessage.saveChanges();
        assertEquals("employee@example.com", mimeMessage.getAllRecipients()[0].toString());
        assertEquals("Subject", mimeMessage.getSubject());
        assertTrue(mimeMessage.getContentType().startsWith("text/html"));
        assertTrue(mimeMessage.getContent().toString().contains("<p>Text</p>"));
    }
}
