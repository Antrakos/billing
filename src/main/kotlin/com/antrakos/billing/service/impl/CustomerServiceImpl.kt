package com.antrakos.billing.service.impl

import com.antrakos.billing.models.Customer
import com.antrakos.billing.repository.CustomerRepository
import com.antrakos.billing.service.CustomerService

/**
 * @author Taras Zubrei
 */
@org.springframework.stereotype.Service
open class CustomerServiceImpl(private val repository: CustomerRepository) : CustomerService {
    override fun create(customer: Customer) = repository.save(customer)

    override fun find(id: Int) = repository.findById(id) ?: throw IllegalStateException("No customer found for id=$id")
}