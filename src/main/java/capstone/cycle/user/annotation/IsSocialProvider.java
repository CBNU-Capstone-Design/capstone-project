package capstone.cycle.user.annotation;

import capstone.cycle.user.annotation.validator.SocialProviderValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SocialProviderValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsSocialProvider {

    String message() default "지원하지 않는 socialProvider 입니다.";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};
}
