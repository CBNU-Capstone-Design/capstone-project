package capstone.cycle.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@OpenAPIDefinition(info = @Info(title = "cycle", version = "v1"))
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@EnableWebMvc
public class SwaggerConfig implements WebMvcConfigurer {

    @Bean
    public GroupedOpenApi userOpenApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .pathsToMatch("/api/u/v1/**")
                .build();
    }

    @Bean
    public GroupedOpenApi commonOpenApi() {
        return GroupedOpenApi.builder()
                .group("common")
                .pathsToMatch("/api/n/v1/**")
                .build();
    }

    @Bean
    public GroupedOpenApi postOpenApi() {
        return GroupedOpenApi.builder()
                .group("post")
                .pathsToMatch("/api/p/v1/**")
                .build();
    }
    @Bean
    public GroupedOpenApi clubOpenApi() {
        return GroupedOpenApi.builder()
                .group("club")
                .pathsToMatch("/api/c/v1/**")
                .build();
    }
}
