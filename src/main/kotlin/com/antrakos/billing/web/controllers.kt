package com.antrakos.billing.web

import com.antrakos.billing.models.Customer
import com.antrakos.billing.models.Usage
import com.antrakos.billing.service.BillService
import com.antrakos.billing.service.CustomerService
import com.antrakos.billing.service.ServiceService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

/**
 * @author Taras Zubrei
 */
@RestController("/customer/")
open class CustomerController(
        private val customerService: CustomerService,
        private val billService: BillService,
        private val serviceService: ServiceService) {

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody customer: Customer) = map(customerService.create(customer))

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "{id}")
    fun find(@PathVariable("id") id: Int) = map(customerService.find(id))

    @RequestMapping(method = arrayOf(RequestMethod.POST), value = "{id}/service/{serviceId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun addService(@PathVariable("id") id:Int, @PathVariable("serviceId") serviceId: Int) {
        customerService.addService(customerService.find(id), serviceService.find(serviceId))
    }

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "{id}/service/")
    fun findServices(@PathVariable("id") id:Int) = customerService.findServices(customerService.find(id))

    @RequestMapping(method = arrayOf(RequestMethod.DELETE), value = "{id}/service/{serviceId}", consumes = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    fun stopService(@PathVariable("id") id:Int, @PathVariable("serviceId") serviceId: Int, @RequestBody lastUsageValue: Double) {
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