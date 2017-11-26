package com.antrakos.billing.service.impl

import com.antrakos.billing.models.Customer
import com.antrakos.billing.models.CustomerToServiceMapping
import com.antrakos.billing.models.Service
import com.antrakos.billing.models.Usage
import com.antrakos.billing.repository.CustomerRepository
import com.antrakos.billing.repository.CustomerToServiceMappingRepository
import com.antrakos.billing.service.BillService
import com.antrakos.billing.service.CustomerService
import com.antrakos.billing.service.UsageService

/**
 * @author Taras Zubrei
 */
@org.springframework.stereotype.Service
open class CustomerServiceImpl(
        private val repository: CustomerRepository,
        private val billService: BillService,
        private val usageService: UsageService,
        private val customerToServiceMappingRepository: CustomerToServiceMappingRepository) : CustomerService {

    override fun findServices(customer: Customer) = customerToServiceMappingRepository.findServices(customer.id!!)

    override fun addService(customer: Customer, service: Service) {
        if (customerToServiceMappingRepository.exists(serviceId = service.id!!, customerId = customer.id!!) != null)
            throw IllegalStateException("Customer[id=${customer.id}] has already activated service[id=${service.id}]")
        customerToServiceMappingRepository.save(CustomerToServiceMapping(serviceId = service.id, customerId = customer.id))
    }

    override fun stopService(customer: Customer, service: Service, lastUsage: Usage) {
        val mapping = customerToServiceMappingRepository.find(service.id!!, customer.id!!)
        if (!mapping.active)
            throw IllegalStateException("Customer[id=${customer.id}] has already stopped service[id=${service.id}]")
        usageService.create(lastUsage)
        billService.createBill(customer, service)
        customerToServiceMappingRepository.save(mapping.copy(active = false))
    }

    override fun create(customer: Customer) = repository.save(customer)

    override fun find(id: Int) = repository.findById(id) ?: throw IllegalStateException("No customer found for id=$id")
}