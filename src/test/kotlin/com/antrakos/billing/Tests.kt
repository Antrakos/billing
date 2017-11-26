package com.antrakos.billing

import com.antrakos.billing.models.Bill
import com.antrakos.billing.models.Customer
import com.antrakos.billing.models.Service
import com.antrakos.billing.models.Usage
import com.antrakos.billing.repository.*
import com.antrakos.billing.service.BillService
import com.antrakos.billing.service.CustomerService
import com.antrakos.billing.service.ServiceService
import com.antrakos.billing.service.UsageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
        @Autowired private val customerRepository: CustomerRepository,
        @Autowired private val billService: BillService,
        @Autowired private val usageService: UsageService,
        @Autowired private val customerService: CustomerService,
        @Autowired private val serviceService: ServiceService) {

    @BeforeEach
    fun setup() {
    }

    @Test
    fun generatingBills() {
        val service = serviceService.create(Service(enabled = true, price = 100.0))
        val customer = customerService.create(Customer())
        customerService.addService(customer, service)
        sequenceOf(
                Usage(
                        date = LocalDate.of(2017, 1, 1),
                        value = 0.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 1, 11),
                        value = 5.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 1, 21),
                        value = 8.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 1, 30),
                        value = 10.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 2, 8),
                        value = 13.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 2, 25),
                        value = 18.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 3, 2),
                        value = 21.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 3, 14),
                        value = 27.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 3, 29),
                        value = 30.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 4, 12),
                        value = 35.0,
                        service = service,
                        customer = customer
                )
        ).forEach { usageService.create(it) }
        println(ObjectMapper().registerModules(JavaTimeModule(), KotlinModule()).writerWithDefaultPrettyPrinter().writeValueAsString(
                billService.createBill(customer, service)
        ))

        sequenceOf(
                Usage(
                        date = LocalDate.of(2017, 4, 25),
                        value = 39.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 5, 1),
                        value = 42.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 5, 10),
                        value = 45.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 5, 20),
                        value = 47.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 5, 30),
                        value = 50.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 6, 11),
                        value = 55.0,
                        service = service,
                        customer = customer
                ),
                Usage(
                        date = LocalDate.of(2017, 7, 1),
                        value = 65.0,
                        service = service,
                        customer = customer
                )
        ).forEach { usageService.create(it) }
        println(ObjectMapper().registerModules(JavaTimeModule(), KotlinModule()).writerWithDefaultPrettyPrinter().writeValueAsString(
                billService.createBill(customer, service)
        ))
        customerService.stopService(customer, service, Usage(
                date = LocalDate.now(),
                value = 180.0,
                service = service,
                customer = customer
        ))
    }

    @Test
    fun test() {
        println("Create service")
        sequenceOf(Service(price = 20.0), Service(price = 10.0), Service(price = 100.0), Service(price = 8.0)).map { serviceRepository.save(it) }.forEach(::println)
        println("Update service")
        sequenceOf(Service(1, true, 20.0), Service(2, true, 10.0), Service(3, true, 100.0), Service(4, true, 8.0)).map { serviceRepository.save(it) }.forEach(::println)

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