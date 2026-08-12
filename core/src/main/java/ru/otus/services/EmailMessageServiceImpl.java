package ru.otus.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.otus.messaging.EmailMessage;

@Service
@RequiredArgsConstructor
public class EmailMessageServiceImpl implements EmailMessageService {

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;

    @Value("${app.kafka.enabled:false}")
    private Boolean kafkaEnabled;

    @Value("${app.kafka.email-topic:email-topic}")
    private String emailTopic;

    @Override
    public void send(EmailMessage message) {
        if (kafkaEnabled) {
            kafkaTemplate.send(emailTopic, message.to(), message);
        }
    }
}
