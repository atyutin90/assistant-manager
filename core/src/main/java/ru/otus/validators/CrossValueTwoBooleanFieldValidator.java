package ru.otus.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import ru.otus.annotations.CrossValueTwoBooleanField;

public class CrossValueTwoBooleanFieldValidator implements ConstraintValidator<CrossValueTwoBooleanField, Object> {

    private String mainField;

    private String dependentField;

    private String message;

    private boolean expectedValue;

    @Override
    public void initialize(CrossValueTwoBooleanField annotation) {
        this.mainField = annotation.mainField();
        this.dependentField = annotation.dependentField();
        this.message = annotation.message();
        this.expectedValue = annotation.expectedValue();
    }

    @Override
    public boolean isValid(Object field, ConstraintValidatorContext context) {
        boolean result = true;
        Object mainFieldValue = new BeanWrapperImpl(field).getPropertyValue(mainField);
        Object dependentFieldValue = new BeanWrapperImpl(field).getPropertyValue(dependentField);

        if (mainFieldValue != null && mainFieldValue.equals(expectedValue)
            && dependentFieldValue != null && dependentFieldValue.equals(expectedValue)
        ) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(mainField)
                .addConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(dependentField)
                .addConstraintViolation();
            result = false;
        }

        return result;
    }
}
