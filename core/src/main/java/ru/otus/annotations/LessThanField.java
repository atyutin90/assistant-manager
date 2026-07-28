package ru.otus.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.otus.validators.LessThanFieldValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для сравнения одной даты с другой.
 */
@Constraint(validatedBy = LessThanFieldValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface LessThanField {

    String message() default "{error.value-must-be-greater}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String dateFrom();

    String dateTo();
}
