package ru.otus.services.email;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import ru.otus.entity.StaffEvaluation;
import ru.otus.entity.User;

import java.time.LocalDate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.thymeleaf.templatemode.TemplateMode.HTML;

class StaffEvaluationActivationEmailFactoryTest {

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void shouldRenderLocalizedHtmlEmail() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ru"));
        var factory = factory();
        var evaluation = StaffEvaluation.builder()
            .name("Итоговая оценка")
            .dateFrom(LocalDate.of(2026, 8, 1))
            .dateTo(LocalDate.of(2026, 8, 31))
            .build();
        var user = User.builder()
            .lastName("Иванов")
            .firstName("Иван")
            .middleName("Иванович")
            .email("employee@example.com")
            .build();

        var message = factory.create(evaluation, user);

        assertEquals("employee@example.com", message.to());
        assertTrue(message.title().contains("Итоговая оценка"));
        assertTrue(message.text().contains("Здравствуйте, Иванов Иван Иванович!"));
        assertTrue(message.text().contains("1 августа 2026"));
        assertTrue(message.text().contains("31 августа 2026"));
    }

    private StaffEvaluationActivationEmailFactory factory() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasename("classpath:locale/messages");

        var templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(HTML);
        templateResolver.setCharacterEncoding("UTF-8");

        var templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.setTemplateEngineMessageSource(messageSource);
        return new StaffEvaluationActivationEmailFactory(templateEngine, messageSource);
    }
}
