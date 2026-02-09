package com.goDelivery.goDelivery.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DeliverySettingsValidator.class)
@Documented
public @interface ValidDeliverySettings {
    String message() default "Invalid delivery settings";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
