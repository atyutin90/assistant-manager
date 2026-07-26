package ru.otus.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import ru.otus.annotations.LessThanField;

import java.time.LocalDate;

public class LessThanFieldValidator implements ConstraintValidator<LessThanField, Object> {

    private String dateFrom;

    private String dateTo;

    private String message;

    @Override
    public void initialize(LessThanField annotation) {
        this.dateFrom = annotation.dateFrom();
        this.dateTo = annotation.dateTo();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(Object field, ConstraintValidatorContext context) {
        try {
            Object dateFromValue = new BeanWrapperImpl(field).getPropertyValue(dateFrom);
            Object dateToValue = new BeanWrapperImpl(field).getPropertyValue(dateTo);

            if (dateFromValue == null || dateToValue == null) {
                return true;
            }

            if (dateFromValue instanceof LocalDate && dateToValue instanceof LocalDate) {
                if (!((LocalDate) dateFromValue).isBefore((LocalDate) dateToValue)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(dateTo)
                        .addConstraintViolation();
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
