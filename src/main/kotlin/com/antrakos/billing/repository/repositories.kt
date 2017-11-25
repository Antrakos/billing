package com.antrakos.billing.repository

import com.antrakos.billing.models.Bill
import com.antrakos.billing.models.Customer
import com.antrakos.billing.models.Service
import com.antrakos.billing.models.Usage

/**
 * @author Taras Zubrei
 */
interface CrudRepository<T, in ID> {
    fun save(entity: T): T
    fun findById(id: ID): T?
    fun findAll(): List<T>
    fun delete(entity: T)
    fun deleteById(id: ID)
}

interface CustomerRepository: CrudRepository<Customer, Int>
interface ServiceRepository: CrudRepository<Service, Int>
interface UsageRepository: CrudRepository<Usage, Int>
interface BillRepository: CrudRepository<Bill, Int>