package ru.otus.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.otus.validators.CrossValueTwoBooleanFieldValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CrossValueTwoBooleanFieldValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CrossValueTwoBooleanField {
    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String mainField();

    String dependentField();

    boolean expectedValue() default true;
}
