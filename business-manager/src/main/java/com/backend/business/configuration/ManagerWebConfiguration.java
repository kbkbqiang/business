package com.backend.business.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Configuration
@EnableSwagger2
@Profile({"default", "dev-online", "test"})
public class ManagerWebConfiguration {

    @Bean
    public Docket appDoc() {
        List<Parameter> pars = new ArrayList<>();
        ParameterBuilder tokenPar = new ParameterBuilder();
        tokenPar.name("token")
                .description("token")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build();
        pars.add(tokenPar.build());
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(appApiInfo())
                .directModelSubstitute(Date.class, Double.class)
                .directModelSubstitute(LocalDate.class, Double.class)
                .directModelSubstitute(Byte.class, Integer.class)
                .globalOperationParameters(pars)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.backend.business.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo appApiInfo() {
        return new ApiInfoBuilder()
                .title("manager-web(管理后台) 接口文档")
                .version("1.0.0")
                .build();
    }
}
