package com.antrakos.billing.web

import com.antrakos.billing.models.*
import com.antrakos.billing.service.*
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * @author Taras Zubrei
 */
@RestController
@Api(tags = ["Customer"])
@RequestMapping("/customer/")
open class CustomerController(
        private val customerService: CustomerService,
        private val billService: BillService,
        private val usageService: UsageService,
        private val userService: UserService,
        private val serviceService: ServiceService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(
            value = "Create new customer",
            notes = "Create new customer in billing system",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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
    @ApiOperation(
            value = "Find customer details",
            notes = "Find all details about existing customer",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    fun find(@PathVariable("id") id: Int, @AuthenticationPrincipal user: SecureUser) = user.checkAccess(id).let { map(customerService.find(id)) }

    @PutMapping("{id}/service/{serviceId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(
            value = "Add service",
            notes = "Add service for customer to use"
    )
    fun addService(@PathVariable("id") id: Int, @PathVariable("serviceId") serviceId: Int, @AuthenticationPrincipal user: SecureUser) = user.checkAccess(id).let {
        customerService.addService(customerService.find(id), serviceService.find(serviceId))
    }

    @GetMapping("{id}/service/")
    @ApiOperation(
            value = "Find customer active services",
            notes = "Find all details about customer's services",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    fun findServices(@PathVariable("id") id: Int, @AuthenticationPrincipal user: SecureUser) = user.checkAccess(id).let { customerService.findServices(customerService.find(id)) }

    @GetMapping("{id}/service/{serviceId}/usage")
    @ApiOperation(
            value = "Find customer usages",
            notes = "Find all details about customer's usages",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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

    @GetMapping("{id}/bill")
    @ApiOperation(
            value = "Find all customer's bills",
            notes = "Find all details about customer's bills",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    fun findBills(@PathVariable("id") id: Int, @AuthenticationPrincipal user: SecureUser) = user.checkAccess(id).let {
        billService.find(customerService.find(id))
                .map {
                    BillDTO(
                            id = it.id!!,
                            amount = it.amount,
                            paid = it.paid,
                            service = it.service,
                            date = it.date
                    )
                }
    }

    @GetMapping("{id}/service/{serviceId}/bill")
    @ApiOperation(
            value = "Find customer's bills of service",
            notes = "Find all details about customer's bills of service",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    fun findBills(@PathVariable("id") id: Int, @PathVariable("serviceId") serviceId: Int, @AuthenticationPrincipal user: SecureUser) = user.checkAccess(id).let {
        billService.find(customerService.find(id), serviceService.find(serviceId))
                .map {
                    BillDTO(
                            id = it.id!!,
                            amount = it.amount,
                            paid = it.paid,
                            date = it.date
                    )
                }
    }

    @DeleteMapping("{id}/service/{serviceId}")
    @ApiOperation(
            value = "Stop service",
            notes = "Stop customer usage of service"
    )
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
@Api(tags = ["Service"])
open class ServiceController(private val serviceService: ServiceService) {
    @GetMapping
    @ApiOperation(
            value = "Find all available services",
            notes = "Find all details about available services",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    fun findAll() = serviceService.findAllEnabled()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(
            value = "Add new service",
            notes = "Add new service available to use",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    fun create(@RequestBody service: Service) = serviceService.create(service)

    @DeleteMapping("{id}")
    @ApiOperation(
            value = "Disable service",
            notes = "Disable service to prevent further usage"
    )
    fun disable(@PathVariable("id") id: Int) = serviceService.disable(serviceService.find(id))
}

@RestController
@RequestMapping("/usage/")
@Api(tags = ["Usage"])
open class UsageController(
        private val customerService: CustomerService,
        private val usageService: UsageService,
        private val serviceService: ServiceService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(
            value = "Add customer usage",
            notes = "Add recent customer usage of service",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    fun create(@RequestBody usage: UsageRequest) = usageService.create(
            Usage(
                    date = usage.date,
                    value = usage.value,
                    customer = customerService.find(usage.customerId),
                    service = serviceService.find(usage.serviceId)
            )
    )
}