package ru.otus.services.email;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.otus.entity.StaffEvaluation;
import ru.otus.entity.User;
import ru.otus.messaging.EmailMessage;

import java.time.format.DateTimeFormatter;

import static java.time.format.FormatStyle.LONG;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

@Component
@RequiredArgsConstructor
public class StaffEvaluationActivationEmailFactory {

    private static final String TEMPLATE = "email/staff-evaluation-activation";

    private final SpringTemplateEngine templateEngine;

    private final MessageSource messageSource;

    public EmailMessage create(StaffEvaluation staffEvaluation, User user) {
        var locale = getLocale();
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(LONG).withLocale(locale);
        var context = new Context(locale);
        context.setVariable("fullName", user.getDisplayName());
        context.setVariable("dateFrom", dateFormatter.format(staffEvaluation.getDateFrom()));
        context.setVariable("dateTo", dateFormatter.format(staffEvaluation.getDateTo()));

        var subject = messageSource.getMessage(
            "email.staff-evaluation-activation.subject",
            new Object[]{staffEvaluation.getName()},
            locale
        );
        return new EmailMessage(user.getEmail(), subject, templateEngine.process(TEMPLATE, context));
    }
}
