package com.antrakos.billing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * @author Taras Zubrei
 */

@SpringBootApplication
@EnableScheduling
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
