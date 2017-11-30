package com.antrakos.billing.service

import com.antrakos.billing.models.*
import com.antrakos.billing.repository.CustomerToServiceMappingRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * @author Taras Zubrei
 */
interface BillService {
    fun lastBillDate(customer: Customer, service: Service): LocalDate?
    fun find(customer: Customer): List<Bill>
    fun find(customer: Customer, service: Service): List<Bill>
    fun createBill(customer: Customer, service: Service): List<Bill>
}

interface UsageService {
    fun getUsageReport(customer: Customer, service: Service, lastPaid: LocalDate?): UsageReport
    fun find(customer: Customer, service: Service): List<Usage>
    fun create(usage: Usage): Usage
}

interface ServiceService {
    fun create(service: Service): Service
    fun find(id: Int): Service
    fun findAllEnabled(): List<Service>
    fun disable(service: Service)
}

interface CustomerService {
    fun create(customer: Customer): Customer
    fun find(id: Int): Customer
    fun addService(customer: Customer, service: Service)
    fun findServices(customer: Customer): List<Service>
    fun stopService(customer: Customer, service: Service, lastUsage: Usage)
}

interface UserService : UserDetailsService {
    fun create(user: User):  User
}

@Component
open class BillGenerator(
        private val customerToServiceMappingRepository: CustomerToServiceMappingRepository,
        private val customerService: CustomerService,
        private val serviceService: ServiceService,
        private val billService: BillService) {
    @Scheduled(cron = "0 30 0 1 * *")
    fun generateBills() {
        customerToServiceMappingRepository.findActive().forEach {
            billService.createBill(customerService.find(it.customerId), serviceService.find(it.serviceId))
        }
    }
}
