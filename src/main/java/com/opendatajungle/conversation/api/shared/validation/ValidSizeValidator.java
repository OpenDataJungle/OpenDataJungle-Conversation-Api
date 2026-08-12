package com.opendatajungle.conversation.api.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class ValidSizeValidator implements ConstraintValidator<ValidSizeByType, String> {
    private final ValidationProperties validationProperties;
    private SizeType sizeType;

    public ValidSizeValidator(ValidationProperties validationProperties) {
        this.validationProperties = validationProperties;
    }

    @Override
    public void initialize(ValidSizeByType annotation) {
        this.sizeType = annotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        int maxSize = switch (sizeType) {
            case MESSAGE -> validationProperties.messageMaxSize();
            case SYSTEM_MESSAGE -> validationProperties.systemMessageMaxSize();
            case TITLE -> validationProperties.titleMaxSize();
        };
        if (value.length() <= maxSize) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Field exceeds the maximum allowed length of " + maxSize + " characters").addConstraintViolation();
        return false;
    }
}
