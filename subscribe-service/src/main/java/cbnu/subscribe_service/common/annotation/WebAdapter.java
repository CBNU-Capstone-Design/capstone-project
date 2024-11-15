package cbnu.subscribe_service.common.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.web.bind.annotation.RestController;

@Documented
@RestController
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebAdapter {
}
