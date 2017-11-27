package com.antrakos.billing.repository

import com.antrakos.billing.models.*
import java.time.LocalDate

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
interface UserRepository: CrudRepository<User, Int> {
    fun findByUsername(username: String): User?
}
interface ServiceRepository: CrudRepository<Service, Int> {
    fun findAllEnabled(): List<Service>
}
interface UsageRepository: CrudRepository<Usage, Int> {
    fun find(serviceId: Int, customerId: Int, after: LocalDate): List<Usage>
    fun find(serviceId: Int, customerId: Int): List<Usage>
    fun findLastPaid(serviceId: Int, customerId: Int, date: LocalDate): Usage?
}
interface BillRepository: CrudRepository<Bill, Int> {
    fun find(serviceId: Int, customerId: Int): List<Bill>
    fun findLast(serviceId: Int, customerId: Int): Bill?
}
interface CustomerToServiceMappingRepository: CrudRepository<CustomerToServiceMapping, Int> {
    fun findActive(): List<CustomerToServiceMapping>
    fun find(serviceId: Int, customerId: Int): CustomerToServiceMapping
    fun exists(serviceId: Int, customerId: Int): Int?
    fun findServices(customerId: Int): List<Service>
    fun findCustomers(serviceId: Int): List<Customer>
}