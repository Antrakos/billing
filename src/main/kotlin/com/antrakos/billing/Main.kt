package com.antrakos.billing

import com.antrakos.billing.web.SecureUser
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.*
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
 * @author Taras Zubrei
 */

@SpringBootApplication
@EnableScheduling
@EnableSwagger2
open class Application {
    @Bean
    open fun objectMapper(): ObjectMapper = ObjectMapper().
            findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    @Bean
    open fun swaggerApi(): Docket = Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.antrakos.billing"))
            .paths(PathSelectors.any())
            .build()
            .securitySchemes(listOf(BasicAuth("Authorization")))
            .securityContexts(listOf(SecurityContext.builder().securityReferences(listOf(SecurityReference("Authorization", arrayOf(AuthorizationScope("global", "accessEverything"))))).build()))
            .apiInfo(apiInfo)
            .ignoredParameterTypes(SecureUser::class.java)

    private val apiInfo = ApiInfo(
            "Billing Service",
            "Billing system documentation",
            "1.0.0",
            "Terms of Service",
            Contact("John Doe", "www.example.com", "myeaddress@company.com"),
            "Apache License 2.0",
            "http://www.apache.org/licenses/LICENSE-2.0",
            emptyList()
    )
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
