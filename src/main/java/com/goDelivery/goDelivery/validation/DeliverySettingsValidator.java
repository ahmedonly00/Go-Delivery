package com.goDelivery.goDelivery.validation;

import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.dtos.restaurant.DeliverySettingsRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DeliverySettingsValidator implements ConstraintValidator<ValidDeliverySettings, DeliverySettingsRequest> {

    @Override
    public void initialize(ValidDeliverySettings constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(DeliverySettingsRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Let @NotNull handle null checks
        }

        if (request.getDeliveryType() == null) {
            return true; // Let @NotNull on deliveryType handle this
        }

        // Disable default constraint violation
        context.disableDefaultConstraintViolation();

        if (request.getDeliveryType() == DeliveryType.SELF_DELIVERY) {
            return validateSelfDelivery(request, context);
        } else if (request.getDeliveryType() == DeliveryType.SYSTEM_DELIVERY) {
            return validateSystemDelivery(request, context);
        }

        return true;
    }

    private boolean validateSelfDelivery(DeliverySettingsRequest request, ConstraintValidatorContext context) {
        boolean isValid = true;

        // Validate radius unit is provided
        if (request.getRadiusUnit() == null) {
            context.buildConstraintViolationWithTemplate("Radius unit is required for self delivery")
                    .addPropertyNode("radiusUnit")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate base delivery fee
        if (request.getBaseDeliveryFee() == null || request.getBaseDeliveryFee() <= 0) {
            context.buildConstraintViolationWithTemplate(
                    "Base delivery fee is required and must be positive for self delivery")
                    .addPropertyNode("baseDeliveryFee")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate per km fee
        if (request.getPerKmFee() == null || request.getPerKmFee() < 0) {
            context.buildConstraintViolationWithTemplate(
                    "Per kilometer fee is required and must be non-negative for self delivery")
                    .addPropertyNode("perKmFee")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate delivery radius
        if (request.getDeliveryRadius() == null || request.getDeliveryRadius() <= 0) {
            context.buildConstraintViolationWithTemplate(
                    "Delivery radius is required and must be positive for self delivery")
                    .addPropertyNode("deliveryRadius")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }

    private boolean validateSystemDelivery(DeliverySettingsRequest request, ConstraintValidatorContext context) {
        // No additional validation required for SYSTEM_DELIVERY
        return true;
    }
}
