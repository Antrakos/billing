package com.antrakos.billing.web

import com.antrakos.billing.models.Customer
import com.antrakos.billing.models.Service
import com.antrakos.billing.models.Usage
import com.antrakos.billing.service.BillService
import com.antrakos.billing.service.CustomerService
import com.antrakos.billing.service.ServiceService
import com.antrakos.billing.service.UsageService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * @author Taras Zubrei
 */
@RestController
@RequestMapping("/customer/")
open class CustomerController(
        private val customerService: CustomerService,
        private val billService: BillService,
        private val usageService: UsageService,
        private val serviceService: ServiceService) {

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody customer: Customer) = map(customerService.create(customer))

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "{id}")
    fun find(@PathVariable("id") id: Int) = map(customerService.find(id))

    @RequestMapping(method = arrayOf(RequestMethod.PUT), value = "{id}/service/{serviceId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun addService(@PathVariable("id") id: Int, @PathVariable("serviceId") serviceId: Int) {
        customerService.addService(customerService.find(id), serviceService.find(serviceId))
    }

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "{id}/service/")
    fun findServices(@PathVariable("id") id: Int) = customerService.findServices(customerService.find(id))

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "{id}/service/{serviceId}/usage")
    fun findUsages(@PathVariable("id") id: Int, @PathVariable("serviceId") serviceId: Int) =
            usageService.find(customerService.find(id), serviceService.find(serviceId))
                    .map {
                        UsageDTO(
                                id = it.id!!,
                                value = it.value,
                                date = it.date
                        )
                    }

    @RequestMapping(method = arrayOf(RequestMethod.DELETE), value = "{id}/service/{serviceId}")
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
    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun findAll() = serviceService.findAllEnabled()

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody service: Service) = serviceService.create(service)

    @RequestMapping(method = arrayOf(RequestMethod.DELETE), value = "{id}")
    fun disable(@PathVariable("id") id: Int) = serviceService.disable(serviceService.find(id))
}

@RestController
@RequestMapping("/usage/")
open class UsageController(
        private val customerService: CustomerService,
        private val usageService: UsageService,
        private val serviceService: ServiceService) {

    @RequestMapping(method = arrayOf(RequestMethod.POST))
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