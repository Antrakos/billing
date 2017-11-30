package com.antrakos.billing.web

import com.antrakos.billing.models.*
import com.antrakos.billing.service.*
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.annotation.security.PermitAll

/**
 * @author Taras Zubrei
 */
@RestController
@RequestMapping("/customer/")
open class CustomerController(
        private val customerService: CustomerService,
        private val billService: BillService,
        private val usageService: UsageService,
        private val userService: UserService,
        private val serviceService: ServiceService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody customerRequest: CustomerRequest): CustomerDTO {
        val customer = customerService.create(Customer())
        userService.create(customerRequest.let {
            User(
                    username = it.username,
                    password = it.password,
                    role = Role.CUSTOMER,
                    enabled = true,
                    customerId = customer.id
            )
        })
        return map(customer)
    }

    @GetMapping("{id}")
    fun find(@PathVariable("id") id: Int, @AuthenticationPrincipal user: SecureUser) = user.checkAccess(id).let { map(customerService.find(id)) }

    @PutMapping("{id}/service/{serviceId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun addService(@PathVariable("id") id: Int, @PathVariable("serviceId") serviceId: Int, @AuthenticationPrincipal user: SecureUser) = user.checkAccess(id).let {
        customerService.addService(customerService.find(id), serviceService.find(serviceId))
    }

    @GetMapping("{id}/service/")
    fun findServices(@PathVariable("id") id: Int, @AuthenticationPrincipal user: SecureUser) = user.checkAccess(id).let { customerService.findServices(customerService.find(id)) }

    @GetMapping("{id}/service/{serviceId}/usage")
    fun findUsages(@PathVariable("id") id: Int, @PathVariable("serviceId") serviceId: Int, @AuthenticationPrincipal user: SecureUser) = user.checkAccess(id).let {
        usageService.find(customerService.find(id), serviceService.find(serviceId))
                .map {
                    UsageDTO(
                            id = it.id!!,
                            value = it.value,
                            date = it.date
                    )
                }
    }

    @DeleteMapping("{id}/service/{serviceId}")
    fun stopService(@PathVariable("id") id: Int, @PathVariable("serviceId") serviceId: Int, @RequestBody lastUsageValue: Double) {
        val service = serviceService.find(serviceId)
        val customer = customerService.find(id)
        customerService.stopService(customer, service, Usage(service = service, customer = customer, value = lastUsageValue))
    }

    private fun map(entity: Customer) = CustomerDTO(
            id = entity.id!!,
            balance = entity.balance,
            services = customerService.findServices(entity),
            unpaidBills = billService.find(entity).map { BillDTO(id = it.id!!, paid = it.paid, amount = it.amount, date = it.date, service = it.service) }
    )
}

@RestController
@RequestMapping("/service/")
open class ServiceController(private val serviceService: ServiceService) {
    @GetMapping
    fun findAll() = serviceService.findAllEnabled()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody service: Service) = serviceService.create(service)

    @DeleteMapping("{id}")
    fun disable(@PathVariable("id") id: Int) = serviceService.disable(serviceService.find(id))
}

@RestController
@RequestMapping("/usage/")
open class UsageController(
        private val customerService: CustomerService,
        private val usageService: UsageService,
        private val serviceService: ServiceService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody usage: UsageRequest) = usageService.create(
            Usage(
                    date = usage.date,
                    value = usage.value,
                    customer = customerService.find(usage.customerId),
                    service = serviceService.find(usage.serviceId)
            )
    )
}