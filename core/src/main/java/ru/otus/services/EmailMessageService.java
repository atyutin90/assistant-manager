package ru.otus.services;

import ru.otus.messaging.EmailMessage;

public interface EmailMessageService {

    void send(EmailMessage message);
}
