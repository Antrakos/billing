package com.antrakos.billing

import com.antrakos.billing.models.Bill
import com.antrakos.billing.models.Customer
import com.antrakos.billing.models.Service
import com.antrakos.billing.models.Usage
import com.antrakos.billing.repository.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringTest(
        @Autowired private val serviceRepository: ServiceRepository,
        @Autowired private val usageRepository: UsageRepository,
        @Autowired private val billRepository: BillRepository,
        @Autowired private val customerToServiceMappingRepository: CustomerToServiceMappingRepository,
        @Autowired private val customerRepository: CustomerRepository) {

    @BeforeEach
    fun setup() {
    }

    @Test
    fun test() {
        println("Create service")
        sequenceOf(Service(price = 20.0), Service(price = 10.0), Service(price = 100.0), Service(price = 8.0)).map { serviceRepository.save(it) }.forEach(::println)
        println("Update service")
        sequenceOf(Service(1, 20.0), Service(2, 10.0), Service(3, 100.0), Service(4, 8.0)).map { serviceRepository.save(it) }.forEach(::println)

        println("Create customer")
        sequenceOf(Customer(balance = 20.0), Customer(balance = 10.0), Customer(balance = 100.0), Customer(balance = 8.0)).map { customerRepository.save(it) }.forEach(::println)
        println("Update customer")
        sequenceOf(Customer(1, 20.0), Customer(2, 10.0), Customer(3, 100.0), Customer(4, 8.0)).map { customerRepository.save(it) }.forEach(::println)

        println("Create usage")
        usageRepository.save(
                Usage(
                        date = LocalDate.now(),
                        value = 100.0,
                        customer = customerRepository.findById(1)!!,
                        service = serviceRepository.findById(1)!!
                )
        )
        println("Update usage")
        usageRepository.save(
                Usage(
                        id = 1,
                        date = LocalDate.now(),
                        value = 120.0,
                        customer = customerRepository.findById(2)!!,
                        service = serviceRepository.findById(2)!!
                )
        )

        println("Create bill")
        billRepository.save(
                Bill(
                        date = LocalDate.now(),
                        amount = 100.0,
                        customer = customerRepository.findById(1)!!,
                        service = serviceRepository.findById(1)!!
                )
        )
        println("Update bill")
        billRepository.save(
                Bill(
                        id = 1,
                        date = LocalDate.now(),
                        amount = 120.0,
                        customer = customerRepository.findById(2)!!,
                        service = serviceRepository.findById(2)!!
                )
        )

        println("Delete -- All")
        sequenceOf(billRepository, usageRepository).forEach { it.deleteById(1) }
        (1..2).forEach { customerToServiceMappingRepository.deleteById(it) }
        (1..4).forEach { customerRepository.deleteById(it) }
        (1..4).forEach { serviceRepository.deleteById(it) }
    }

}