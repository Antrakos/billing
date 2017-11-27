package com.antrakos.billing

import com.antrakos.billing.models.*
import com.antrakos.billing.repository.*
import com.antrakos.billing.service.BillGenerator
import com.antrakos.billing.web.CustomerDTO
import com.antrakos.billing.web.UsageRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
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
        @Autowired private val userRepository: UserRepository,
        @Autowired private val passwordEncoder: PasswordEncoder,
        @Autowired private val customerToServiceMappingRepository: CustomerToServiceMappingRepository,
        @Autowired private val customerRepository: CustomerRepository,
        @Autowired private val billGenerator: BillGenerator,
        @Autowired private val restTemplate: TestRestTemplate) {

    @BeforeEach
    fun setup() {
    }

    @Test
    fun security() {
        val user = userRepository.save(User(
                username = "admin",
                password = passwordEncoder.encode("admin"),
                enabled = true,
                role = Role.WORKER
        ))
        val restTemplate = TestRestTemplate("admin", "admin")
        val server = MockRestServiceServer.createServer(restTemplate.restTemplate)
        server.expect(requestTo("/customer/")).andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.OK))
        restTemplate.postForObject("/customer/", Customer(), CustomerDTO::class.java)
        server.verify()
    }

    @Test
    fun generatingBills() {
        val customerResponse = restTemplate.postForObject("/customer/", Customer(), CustomerDTO::class.java)
        val serviceResponse = restTemplate.postForObject("/service/", Service(price = 100.0), Service::class.java)
        val service = serviceResponse.id!!
        val customer = customerResponse.id
        restTemplate.put("/customer/$customer/service/$service", null)
        sequenceOf(
                UsageRequest(
                        date = LocalDate.of(2017, 1, 1),
                        value = 0.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 1, 11),
                        value = 5.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 1, 21),
                        value = 8.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 1, 30),
                        value = 10.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 2, 8),
                        value = 13.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 2, 25),
                        value = 18.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 3, 2),
                        value = 21.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 3, 14),
                        value = 27.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 3, 29),
                        value = 30.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 4, 12),
                        value = 35.0,
                        serviceId = service,
                        customerId = customer
                )
        ).forEach { restTemplate.postForEntity("/usage/", it, Usage::class.java) }

        billGenerator.generateBills()

        sequenceOf(
                UsageRequest(
                        date = LocalDate.of(2017, 4, 25),
                        value = 39.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 5, 1),
                        value = 42.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 5, 10),
                        value = 45.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 5, 20),
                        value = 47.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 5, 30),
                        value = 50.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 6, 11),
                        value = 55.0,
                        serviceId = service,
                        customerId = customer
                ),
                UsageRequest(
                        date = LocalDate.of(2017, 7, 1),
                        value = 65.0,
                        serviceId = service,
                        customerId = customer
                )
        ).forEach { restTemplate.postForEntity("/usage/", it, Usage::class.java) }

        billGenerator.generateBills()
        restTemplate.exchange("/customer/$customer/service/$service", HttpMethod.DELETE, HttpEntity(180.0), String::class.java)
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