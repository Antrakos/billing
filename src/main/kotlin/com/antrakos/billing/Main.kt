package com.antrakos.billing

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * @author Taras Zubrei
 */

@SpringBootApplication
@EnableScheduling
open class Application {
    @Bean
    open fun objectMapper(): ObjectMapper = ObjectMapper().
            findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
