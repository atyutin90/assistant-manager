package ru.otus.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import ru.otus.annotations.MismatchField;

public class MismatchFieldValidator implements ConstraintValidator<MismatchField, Object> {

    private String firstField;

    private String secondField;

    private String message;

    @Override
    public void initialize(MismatchField annotation) {
        this.firstField = annotation.firstField();
        this.secondField = annotation.secondField();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(Object field, ConstraintValidatorContext context) {
        boolean result = true;
        Object firstFieldValue = new BeanWrapperImpl(field).getPropertyValue(firstField);
        Object secondFieldValue = new BeanWrapperImpl(field).getPropertyValue(secondField);

        if (firstFieldValue != null && (!firstFieldValue.equals(secondFieldValue))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(secondField)
                .addConstraintViolation();
            result = false;
        }

        return result;
    }
}
