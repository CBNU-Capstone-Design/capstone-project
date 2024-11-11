package capstone.cycle.user.annotation.validator;

import capstone.cycle.user.annotation.IsSocialProvider;
import capstone.cycle.user.entity.SocialProvider;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class SocialProviderValidator implements ConstraintValidator<IsSocialProvider, String> {
    String kakao = SocialProvider.KAKAO.getProvider();
    private List<String> ALLOWED_PROVIDERS = Arrays.asList(kakao);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        //소셜 프로바이더가 비어있을
        if (value == null || value.trim().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("social provider는 필수값입니다.").addConstraintViolation();
            return false;
        }

        //형식이 잘못된 경우
        if (!ALLOWED_PROVIDERS.contains(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
