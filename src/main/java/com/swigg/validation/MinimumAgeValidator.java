package com.swigg.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.Period;

public class MinimumAgeValidator implements ConstraintValidator<MinimumAge, LocalDateTime> {

    private int minimumAge;

    @Override
    public void initialize(MinimumAge constraintAnnotation) {
        this.minimumAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDateTime dob, ConstraintValidatorContext context) {
        if (dob == null) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        Period period = Period.between(dob.toLocalDate(), now.toLocalDate());
        return period.getYears() >= minimumAge;
    }
}
