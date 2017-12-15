package com.antrakos.billing

import com.antrakos.billing.models.Service
import com.antrakos.billing.models.Usage
import com.antrakos.billing.service.BillGenerator
import com.antrakos.billing.web.BillDTO
import com.antrakos.billing.web.CustomerDTO
import com.antrakos.billing.web.CustomerRequest
import com.antrakos.billing.web.UsageRequest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.collection.IsEmptyCollection.empty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EndpointTest(@Autowired private val restTemplate: TestRestTemplate, @Autowired private val billGenerator: BillGenerator) {

    @Test
    fun customerWorkflow() {
        val customerRequest = CustomerRequest(name = "Test customer", address = "Kyiv, Yangel Street, 8, 2-09", username = "test_customer", password = "password")
        val customerDTO = restTemplate.postForObject("/customer/", customerRequest, CustomerDTO::class.java)
        assertThat(customerDTO.name, `is`(equalTo(customerRequest.name)))
        assertThat(customerDTO.balance, `is`(equalTo(0.0)))
        assertThat(customerDTO.address, `is`(equalTo(customerRequest.address)))
        assertThat(customerDTO.services, empty())
        assertThat(customerDTO.unpaidBills, empty())
        assertThat(customerDTO.id, `is`(equalTo(0)))

        val customerRestTemplate = restTemplate.withBasicAuth(customerRequest.username, customerRequest.password)
        val adminRestTemplate = restTemplate.withBasicAuth("admin", "admin")

        val customerDTO1 = customerRestTemplate.getForObject("/customer/{id}", CustomerDTO::class.java, customerDTO.id)
        assertThat(customerDTO1.name, `is`(equalTo(customerRequest.name)))
        assertThat(customerDTO1.balance, `is`(equalTo(0.0)))
        assertThat(customerDTO1.address, `is`(equalTo(customerRequest.address)))
        assertThat(customerDTO1.services, empty())
        assertThat(customerDTO1.unpaidBills, empty())
        assertThat(customerDTO1.id, `is`(equalTo(0)))

        val serviceRequest = Service(price = 100.0)
        val serviceResponse = adminRestTemplate.postForObject("/service/", serviceRequest, Service::class.java)
        assertThat(serviceResponse.price, `is`(equalTo(serviceRequest.price)))
        assertThat(serviceResponse.id, `is`(equalTo(0)))
        assertThat(serviceResponse.enabled, `is`(equalTo(true)))


        val service = serviceResponse.id!!
        val customer = customerDTO.id
        adminRestTemplate.put("/customer/$customer/service/$service", null)

        val customerDTO2 = customerRestTemplate.getForObject("/customer/{id}", CustomerDTO::class.java, customerDTO.id)
        assertThat(customerDTO2.name, `is`(equalTo(customerRequest.name)))
        assertThat(customerDTO2.balance, `is`(equalTo(0.0)))
        assertThat(customerDTO2.address, `is`(equalTo(customerRequest.address)))
        assertThat(customerDTO2.services, hasSize(1))
        assertThat(customerDTO2.services[0].price, `is`(equalTo(serviceRequest.price)))
        assertThat(customerDTO2.services[0].id, `is`(equalTo(0)))
        assertThat(customerDTO2.services[0].enabled, `is`(equalTo(true)))
        assertThat(customerDTO2.unpaidBills, empty())
        assertThat(customerDTO2.id, `is`(equalTo(0)))

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
        ).forEach { adminRestTemplate.postForEntity("/usage/", it, Usage::class.java) }

        billGenerator.generateBills()
        val bills = listOf(
                BillDTO(id = 0, date = LocalDate.of(2017, 2, 8), amount = 1300.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 1, date = LocalDate.of(2017, 3, 2), amount = 800.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 2, date = LocalDate.of(2017, 4, 12), amount = 1400.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false)
        )


        val customerDTO3 = customerRestTemplate.getForObject("/customer/{id}", CustomerDTO::class.java, customerDTO.id)
        assertThat(customerDTO3.name, `is`(equalTo(customerRequest.name)))
        assertThat(customerDTO3.balance, `is`(equalTo(0.0)))
        assertThat(customerDTO3.address, `is`(equalTo(customerRequest.address)))
        assertThat(customerDTO3.services, hasSize(1))
        assertThat(customerDTO3.services[0].price, `is`(equalTo(serviceRequest.price)))
        assertThat(customerDTO3.services[0].id, `is`(equalTo(0)))
        assertThat(customerDTO3.services[0].enabled, `is`(equalTo(true)))
        assertThat(customerDTO3.unpaidBills, hasSize(3))
        assertThat(customerDTO3.unpaidBills, `is`(equalTo(bills)))
        assertThat(customerDTO3.id, `is`(equalTo(0)))

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
        ).forEach { adminRestTemplate.postForEntity("/usage/", it, Usage::class.java) }

        billGenerator.generateBills()

        val bills2 = listOf(
                BillDTO(id = 0, date = LocalDate.of(2017, 2, 8), amount = 1300.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 1, date = LocalDate.of(2017, 3, 2), amount = 800.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 2, date = LocalDate.of(2017, 4, 12), amount = 1400.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 3, date = LocalDate.of(2017, 5, 1), amount = 700.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 4, date = LocalDate.of(2017, 6, 11), amount = 1300.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 5, date = LocalDate.of(2017, 7, 1), amount = 1000.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false)
        )

        val customerDTO4 = customerRestTemplate.getForObject("/customer/{id}", CustomerDTO::class.java, customerDTO.id)
        assertThat(customerDTO4.name, `is`(equalTo(customerRequest.name)))
        assertThat(customerDTO4.balance, `is`(equalTo(0.0)))
        assertThat(customerDTO4.address, `is`(equalTo(customerRequest.address)))
        assertThat(customerDTO4.services, hasSize(1))
        assertThat(customerDTO4.services[0].price, `is`(equalTo(serviceRequest.price)))
        assertThat(customerDTO4.services[0].id, `is`(equalTo(0)))
        assertThat(customerDTO4.services[0].enabled, `is`(equalTo(true)))
        assertThat(customerDTO4.unpaidBills, hasSize(6))
        assertThat(customerDTO4.unpaidBills, `is`(equalTo(bills2)))
        assertThat(customerDTO4.id, `is`(equalTo(0)))

        adminRestTemplate.exchange("/customer/$customer/service/$service", HttpMethod.DELETE, HttpEntity(180.0), String::class.java)

        val bills3 = listOf(
                BillDTO(id = 0, date = LocalDate.of(2017, 2, 8), amount = 1300.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 1, date = LocalDate.of(2017, 3, 2), amount = 800.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 2, date = LocalDate.of(2017, 4, 12), amount = 1400.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 3, date = LocalDate.of(2017, 5, 1), amount = 700.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 4, date = LocalDate.of(2017, 6, 11), amount = 1300.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 5, date = LocalDate.of(2017, 7, 1), amount = 1000.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false),
                BillDTO(id = 6, date = LocalDate.now(), amount = 11500.0, service = Service(id = 0, enabled = true, price = 100.0), paid = false)
        )

        val billsRequest = customerRestTemplate.exchange("/customer/$customer/bill", HttpMethod.GET, null, object : ParameterizedTypeReference<List<BillDTO>>() {}).body!!
        assertThat(billsRequest, hasSize(7))
        assertThat(billsRequest, `is`(equalTo(bills3)))

        bills3.forEach {
            adminRestTemplate.put("/bill/${it.id}", null)
        }

        val billsRequest2 = customerRestTemplate.exchange("/customer/$customer/bill", HttpMethod.GET, null, object : ParameterizedTypeReference<List<BillDTO>>() {}).body!!
        assertThat(billsRequest2, hasSize(7))
        assertThat(billsRequest2.all { it.paid }, `is`(equalTo(true)))

        val servicesRequest = customerRestTemplate.exchange("/service/", HttpMethod.GET, null, object : ParameterizedTypeReference<List<Service>>() {}).body!!
        assertThat(servicesRequest, hasSize(1))
        assertThat(servicesRequest[0], `is`(equalTo(Service(id = 0, enabled = true, price = 100.0))))

        adminRestTemplate.delete("/service/$service")

        val servicesRequest2 = customerRestTemplate.exchange("/service/", HttpMethod.GET, null, object : ParameterizedTypeReference<List<Service>>() {}).body!!
        assertThat(servicesRequest2, empty())
    }
}