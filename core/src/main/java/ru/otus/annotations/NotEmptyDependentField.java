package ru.otus.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.otus.validators.NotEmptyDependentFieldValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotEmptyDependentFieldValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyDependentField {

    String message() default "{dependentField} {javax.validation.constraints.NotEmpty.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String mainField();

    String dependentField();

    String[] expectedValues() default {};
}
