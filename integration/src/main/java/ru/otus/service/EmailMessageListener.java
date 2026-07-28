package ru.otus.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import ru.otus.messaging.EmailMessage;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@RequiredArgsConstructor
public class EmailMessageListener {

    private final JavaMailSender mailSender;

    @KafkaListener(topics = "${app.kafka.email-topic:email-topic}")
    public void receive(EmailMessage message) throws MessagingException {
        var mailMessage = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(mailMessage, false, UTF_8.name());
        helper.setTo(message.to());
        helper.setSubject(message.title());
        helper.setText(message.text(), true);
        mailSender.send(mailMessage);
    }
}
