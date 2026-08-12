package ru.otus.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import ru.otus.annotations.NotEmptyDependentField;

import java.util.List;

public class NotEmptyDependentFieldValidator implements ConstraintValidator<NotEmptyDependentField, Object> {

    private String mainField;

    private String dependentField;

    private List<String> expectedValues;

    @Override
    public void initialize(NotEmptyDependentField annotation) {
        this.mainField = annotation.mainField();
        this.dependentField = annotation.dependentField();
        this.expectedValues = annotation.expectedValues() != null ? List.of(annotation.expectedValues()) : List.of();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext context) {
        Object mainFieldValue = new BeanWrapperImpl(o).getPropertyValue(mainField);
        Object dependentFieldValue = new BeanWrapperImpl(o).getPropertyValue(dependentField);

        if (mainFieldValue == null || !expectedValues.contains(mainFieldValue.toString())) {
            return true;
        }

        if (dependentFieldValue != null && !dependentFieldValue.toString().isEmpty()) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addPropertyNode(dependentField)
            .addConstraintViolation();
        return false;
    }
}
