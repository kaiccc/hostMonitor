package com.mon.host.common.swagger;

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.annotation.Annotation;


/**
 * 类描述：配置swagger2信息
 *
 * @author yuan
 * @version 1.0
 */
@Configuration
@EnableWebMvc
@EnableSwagger2
public class SwaggerConfig {



    @Bean
    public Docket apiNormalGroup() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .build()
                .apiInfo(apiInfo());
    }

    private static Predicate<RequestHandler> withNonMethodAnnotation(final Class<? extends Annotation> annotation) {
        return new Predicate<RequestHandler>() {
            @Override
            public boolean apply(RequestHandler input) {
                return null == AnnotationUtils.findAnnotation(input.getHandlerMethod().getMethod(), annotation);
            }
        };
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("API接口")
                .version("v2.0.0")
                .build();
    }

}