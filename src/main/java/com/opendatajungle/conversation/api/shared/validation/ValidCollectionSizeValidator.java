package com.opendatajungle.conversation.api.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ValidCollectionSizeValidator implements ConstraintValidator<ValidSizeByType, Collection<?>> {
    private final ValidationProperties validationProperties;
    private SizeType sizeType;

    public ValidCollectionSizeValidator(ValidationProperties validationProperties) {
        this.validationProperties = validationProperties;
    }

    @Override
    public void initialize(ValidSizeByType annotation) {
        this.sizeType = annotation.value();
    }

    @Override
    public boolean isValid(Collection<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        int maxSize = validationProperties.maxSizeFor(sizeType);
        if (value.size() <= maxSize) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Field exceeds the maximum allowed number of " + maxSize + " elements").addConstraintViolation();
        return false;
    }
}
