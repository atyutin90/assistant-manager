package ru.otus.services;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.otus.messaging.EmailMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailMessageServiceImplTest {

    @Test
    void shouldSendMessageToEmailTopicUsingRecipientAsKey() {
        @SuppressWarnings("unchecked")
        var kafkaTemplate = (KafkaTemplate<String, EmailMessage>) mock(KafkaTemplate.class);
        var service = new EmailMessageServiceImpl(kafkaTemplate);
        ReflectionTestUtils.setField(service, "emailTopic", "email-topic");
        var message = new EmailMessage("employee@example.com", "Subject", "Text");

        service.send(message);

        verify(kafkaTemplate).send("email-topic", "employee@example.com", message);
    }
}
