package com.antrakos.billing

import com.antrakos.billing.models.Service
import com.antrakos.billing.repository.ServiceRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * @author Taras Zubrei
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringTest(@Autowired private val serviceRepository: ServiceRepository){

    @BeforeEach
    fun setup() {
    }

    @Test
    fun test() {
        println("Create")
        sequenceOf(Service(price = 20.0), Service(price = 10.0), Service(price = 100.0), Service(price = 8.0)).map { serviceRepository.save(it) }.forEach(::println)
        println("Update")
        sequenceOf(Service(1, 20.0), Service(2, 10.0), Service(3, 100.0), Service(4, 8.0)).map { serviceRepository.save(it) }.forEach(::println)
        println("Delete")
        (1..4).forEach { serviceRepository.deleteById(it) }
    }

}